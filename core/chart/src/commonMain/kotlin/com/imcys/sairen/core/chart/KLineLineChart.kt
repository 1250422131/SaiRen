package com.imcys.sairen.core.chart

import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.timer.setTimeout
import com.tencent.kuikly.core.views.Canvas
import com.tencent.kuikly.core.views.CanvasContext
import com.tencent.kuikly.core.views.FontStyle
import com.tencent.kuikly.core.views.FontWeight
import com.tencent.kuikly.core.views.TextAlign
import com.tencent.kuikly.core.views.View
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/** 单根 K 线对应的收盘价数据。 */
data class KLineLinePoint(
    val timestamp: Long,
    val close: Float,
    val label: String = timestamp.toString()
)

/** 折线图的附加数据线，可用于均价线等与主线共用坐标轴的数据。 */
data class KLineLineSeries(
    val points: List<KLineLinePoint>,
    val name: String = "",
    val color: Color,
    val lineWidth: Float = 1f,
    val smooth: Boolean = false,
) {
    init {
        require(lineWidth > 0f)
    }
}

/** K 线收盘价折线图的绘制参数。 */
data class KLineLineChartConfig(
    val contentPaddingStart: Float = 12f,
    val contentPaddingTop: Float = 0f,
    val contentPaddingEnd: Float = 12f,
    val contentPaddingBottom: Float = 12f,
    val lineColor: Color = Color(0xFF2B7FFF),
    val lineWidth: Float = 1f,
    val fillTopColor: Color = Color(0x332B7FFF),
    val fillBottomColor: Color = Color(0x002B7FFF),
    val valuePaddingRatio: Float = 0.08f,
    val showXAxis: Boolean = true,
    val showYAxis: Boolean = true,
    val maxXAxisLabelCount: Int = 4,
    val maxYAxisLabelCount: Int = 6,
    val yAxisLabelWidth: Float = 46f,
    val xAxisLabelHeight: Float = 20f,
    val axisColor: Color = Color(0xFFB7C0CC),
    val gridColor: Color = Color(0xFFE7EBF0),
    val labelColor: Color = Color(0xFF7A8491),
    val labelFontSize: Float = 10f,
    /** 是否显示数据线图示；关闭时不占用绘图区高度。 */
    val showLegend: Boolean = false,
    val primaryLineName: String = "",
    /** 图示与绘图区的垂直间距。 */
    val legendChartSpacing: Float = 4f,
    val selectionColor: Color = lineColor,
    val selectionLabelTextColor: Color = Color.WHITE,
    val selectionLineWidth: Float = 1f,
    val selectionPointRadius: Float = 4f,
    val xAxisLabelFormatter: (KLineLinePoint) -> String = { it.label },
    val yAxisLabelFormatter: (Float) -> String = { formatPriceLabel(it) },
    /** 数据变化时的点位过渡动画时长（毫秒），0 表示关闭动画。 */
    val animationDuration: Int = 400,
    /** 动画帧间隔（毫秒），16ms ≈ 60fps。 */
    val animationFrameInterval: Int = 16
) {
    init {
        require(contentPaddingStart >= 0f)
        require(contentPaddingTop >= 0f)
        require(contentPaddingEnd >= 0f)
        require(contentPaddingBottom >= 0f)
        require(legendChartSpacing >= 0f)
        require(lineWidth > 0f)
        require(valuePaddingRatio >= 0f)
        require(maxXAxisLabelCount >= 2)
        require(maxYAxisLabelCount >= 2)
        require(yAxisLabelWidth >= 0f)
        require(xAxisLabelHeight >= 0f)
        require(labelFontSize > 0f)
        require(selectionLineWidth > 0f)
        require(selectionPointRadius >= 0f)
        require(animationDuration >= 0)
        require(animationFrameInterval > 0)
    }
}

/**
 * K 线收盘价折线图组件的属性。
 *
 * [points] 为惰性求值 lambda：在 Canvas 绘制回调内调用，从而被
 * CanvasView 的 ReactiveObserver 追踪——lambda 内读取的外部
 * observable 状态变化时会自动触发重绘。
 */
class KLineLineChartAttr : ComposeAttr() {
    var points: () -> List<KLineLinePoint> = { emptyList() }
    var additionalLines: () -> List<KLineLineSeries> = { emptyList() }
    var config: KLineLineChartConfig = KLineLineChartConfig()
}

/**
 * K 线收盘价折线图组件。
 *
 * 对齐官方组合组件模式（参考 CouponBackgroundView / WeatherRainFallNodeCanvas）：
 * ComposeView + ComposeAttr + 顶层扩展函数，Canvas 在 body() 内创建并
 * 通过 absolutePositionAllZero 铺满自身；绘制回调内读取 [KLineLineChartAttr.points]
 * 以获得响应式重绘能力。
 */
class KLineLineChartView : ComposeView<KLineLineChartAttr, ComposeEvent>() {

    /** Android 首次创建原生 Canvas 的开销较高，先让页面其他内容完成首帧。 */
    private var canvasMounted by observable(!shouldDeferInitialChartMount())
    private var canvasMountScheduled = false

    /** 动画进度（0~1），响应式字段——drawCallback 内读取以接通重绘链路。 */
    private var animProgress by observable(0f)
    /** 数据已变化、等待异步启动动画的标记（普通字段，可在 drawCallback 内安全写入）。 */
    private var animPending = false
    /** 代际计数：+1 即作废所有旧动画帧链（快速切数据/视图销毁时防止旧链继续跑）。 */
    private var animGeneration = 0
    private var animStartMark: TimeMark? = null
    private var lastPoints: List<KLineLinePoint> = emptyList()
    private var animationStartPoints: List<KLineLinePoint> = emptyList()
    private var animationEndPoints: List<KLineLinePoint> = emptyList()
    /** 首批数据直接静态绘制，避免冷启动 Canvas 时叠加动画帧；后续更新仍使用点位插值。 */
    private var isInitialReveal = false
    /** 当前触摸选中的原始数据点索引；-1 表示尚未选择。 */
    private var selectedPointIndex by observable(-1)

    override fun createAttr(): KLineLineChartAttr = KLineLineChartAttr()

    override fun createEvent(): ComposeEvent = ComposeEvent()

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            ctx.scheduleCanvasMountIfNeeded()
            vif({ ctx.canvasMounted }) {
                Canvas(
                    {
                        attr {
                            absolutePositionAllZero()
                        }
                    }
                ) { context, width, height ->
                // 在绘制回调内读取数据，确保 points lambda 引用的 observable 更新后重绘
                val points = ctx.attr.points()
                val additionalLines = ctx.attr.additionalLines()
                if (points != ctx.lastPoints) {
                    val hasPreviousData = ctx.lastPoints.isNotEmpty()
                    ctx.isInitialReveal = false
                    val visiblePoints = interpolateChartPoints(
                        ctx.animationStartPoints,
                        ctx.animationEndPoints,
                        ctx.animProgress
                    )
                    // 保存快照，避免外部集合原地更新时丢失上一帧曲线。
                    ctx.lastPoints = points.toList()
                    ctx.animationStartPoints = resamplePointsForTransition(visiblePoints, points)
                    ctx.animationEndPoints = points
                    if (hasPreviousData && !ctx.animPending) {
                        ctx.animPending = true
                        // 异步启动动画：依赖收集期间写过的属性会被剔除追踪，故推迟到收集期外
                        ctx.setTimeout(0) {
                            ctx.animPending = false
                            ctx.beginAnimation()
                        }
                    }
                }
                // animProgress 必须无条件读取以维持响应式追踪
                val rawProgress = ctx.animProgress
                val progress = if (ctx.animPending) 0f else rawProgress
                val easedProgress = easeOutCubic(progress.coerceIn(0f, 1f))
                val linePoints = interpolateChartPoints(
                    ctx.animationStartPoints,
                    ctx.animationEndPoints,
                    easedProgress
                )
                drawKLineLineChart(
                    context = context,
                    width = width,
                    height = height,
                    points = points,
                    linePoints = linePoints,
                    additionalLines = additionalLines,
                    config = ctx.attr.config,
                    selectedPointIndex = ctx.selectedPointIndex,
                    initialRevealProgress = if (ctx.isInitialReveal) easedProgress else 1f
                )
                }
                // KRCanvasView 本身不分发基础触摸事件，使用同尺寸透明 View 承接手势。
                View {
                    attr {
                        absolutePositionAllZero()
                    }
                    event {
                        touchDown(isSync = true) { ctx.selectPointAtX(it.x) }
                        touchMove(isSync = true) { ctx.selectPointAtX(it.x) }
                        touchUp(isSync = true) { ctx.clearSelectedPoint() }
                        touchCancel(isSync = true) { ctx.clearSelectedPoint() }
                    }
                }
            }
        }
    }

    private fun scheduleCanvasMountIfNeeded() {
        if (canvasMounted || canvasMountScheduled) return
        canvasMountScheduled = true
        setTimeout(INITIAL_CANVAS_MOUNT_DELAY_MS) {
            canvasMounted = true
        }
    }

    private fun selectPointAtX(touchX: Float) {
        val points = attr.points()
        if (points.isEmpty()) return
        // 图表左右边界由配置决定；按横向位置吸附，使任意按下位置都能选中一个数据点。
        val left = attr.config.contentPaddingStart +
            if (attr.config.showYAxis) attr.config.yAxisLabelWidth else 0f
        val chartWidth = max(0f, flexNode.layoutFrame.width - attr.config.contentPaddingEnd - left)
        selectedPointIndex = if (points.size == 1 || chartWidth == 0f) {
            0
        } else {
            ((touchX - left) / chartWidth * points.lastIndex + 0.5f)
                .toInt()
                .coerceIn(0, points.lastIndex)
        }
    }

    private fun clearSelectedPoint() {
        selectedPointIndex = -1
    }

    override fun viewDestroyed() {
        animGeneration++
        super.viewDestroyed()
    }

    /** 启动一次点位过渡动画。必须在依赖收集期之外调用（如 setTimeout 回调里）。 */
    private fun beginAnimation() {
        animGeneration++
        val duration = attr.config.animationDuration
        if (duration <= 0 || animationEndPoints.size < 2) {
            animProgress = 1f
            return
        }
        animStartMark = TimeSource.Monotonic.markNow()
        animProgress = 0f
        tickAnimation(animGeneration)
    }

    private fun tickAnimation(generation: Int) {
        if (generation != animGeneration) return
        val mark = animStartMark ?: return
        val duration = attr.config.animationDuration
        val elapsed = mark.elapsedNow().inWholeMilliseconds
        val progress = if (duration <= 0) 1f else (elapsed.toFloat() / duration).coerceIn(0f, 1f)
        animProgress = progress
        if (progress < 1f) {
            setTimeout(attr.config.animationFrameInterval) {
                tickAnimation(generation)
            }
        }
    }
}

/** 创建 K 线收盘价折线图。用法参见官方组合组件（如 CouponBackground）。 */
fun ViewContainer<*, *>.KLineLineChart(init: KLineLineChartView.() -> Unit) {
    addChild(KLineLineChartView(), init)
}

private const val INITIAL_CANVAS_MOUNT_DELAY_MS = 160

private fun drawKLineLineChart(
    context: CanvasContext,
    width: Float,
    height: Float,
    points: List<KLineLinePoint>,
    linePoints: List<KLineLinePoint>,
    additionalLines: List<KLineLineSeries>,
    config: KLineLineChartConfig,
    selectedPointIndex: Int,
    initialRevealProgress: Float,
) {
    val left = config.contentPaddingStart + if (config.showYAxis) config.yAxisLabelWidth else 0f
    val right = max(left, width - config.contentPaddingEnd)
    val legendItems = buildList {
        if (config.primaryLineName.isNotEmpty()) {
            add(ChartLegendItem(config.primaryLineName, config.lineColor))
        }
        additionalLines.filter { it.name.isNotEmpty() }
            .forEach { add(ChartLegendItem(it.name, it.color)) }
    }
    val legendHeight = if (config.showLegend && legendItems.isNotEmpty()) config.labelFontSize + 8f else 0f
    val top = if (legendHeight > 0f) {
        max(config.contentPaddingTop, legendHeight + config.legendChartSpacing)
    } else {
        config.contentPaddingTop
    }
    val bottom = max(top, height - config.contentPaddingBottom - if (config.showXAxis) config.xAxisLabelHeight else 0f)

    // Canvas 的 iOS 端无法用空 dash 数组恢复实线；用短实线段绘制网格，避免污染折线状态。
    fun drawDashedLine(startX: Float, startY: Float, endX: Float, endY: Float, color: Color) {
        val length = max(abs(endX - startX), abs(endY - startY))
        if (length == 0f) return
        val unitX = (endX - startX) / length
        val unitY = (endY - startY) / length
        var distance = 0f
        while (distance < length) {
            val segmentEnd = min(distance + 4f, length)
            context.beginPath()
            context.moveTo(startX + unitX * distance, startY + unitY * distance)
            context.lineTo(startX + unitX * segmentEnd, startY + unitY * segmentEnd)
            context.strokeStyle(color)
            context.lineWidth(1f)
            context.stroke()
            distance += 8f
        }
    }

    fun drawAxes(valueMin: Float? = null, valueRange: Float? = null) {
        if (config.showYAxis) {
            val yTicks = if (points.isEmpty()) {
                config.maxYAxisLabelCount
            } else {
                min(config.maxYAxisLabelCount, points.size)
            }
            for (tick in 0 until yTicks) {
                val fraction = if (yTicks == 1) 0f else tick.toFloat() / (yTicks - 1)
                val y = bottom - fraction * (bottom - top)
                drawDashedLine(left, y, right, y, config.gridColor)
                if (valueMin != null && valueRange != null) {
                    context.fillStyle(config.labelColor)
                    context.font(FontStyle.NORMAL, FontWeight.NORMAL, config.labelFontSize)
                    context.textAlign(TextAlign.RIGHT)
                    context.fillText(
                        config.yAxisLabelFormatter(valueMin + fraction * valueRange),
                        left - 6f,
                        y + config.labelFontSize / 2f
                    )
                }
            }
        }
        if (config.showXAxis) {
            val tickIndices = selectXAxisTickIndices(points, config.maxXAxisLabelCount)
            // 空数据阶段也先画出固定网格；有数据后再补充对应标签。
            val tickCount = if (points.isEmpty()) config.maxXAxisLabelCount else tickIndices.size
            fun tickX(slot: Int): Float {
                if (tickCount == 0) return left
                return left + (right - left) * (slot + 1) / (tickCount + 1).toFloat()
            }
            for (slot in 0 until tickCount) {
                val x = tickX(slot)
                drawDashedLine(x, top, x, bottom, config.gridColor)
            }
            tickIndices.forEachIndexed { pos, index ->
                val x = tickX(pos)
                context.fillStyle(config.labelColor)
                context.font(FontStyle.NORMAL, FontWeight.NORMAL, config.labelFontSize)
                context.textAlign(TextAlign.CENTER)
                context.fillText(
                    config.xAxisLabelFormatter(points[index]),
                    x,
                    height - config.contentPaddingBottom
                )
            }
        }

        drawDashedLine(left, top, left, bottom, config.axisColor)
        drawDashedLine(left, bottom, right, bottom, config.axisColor)
    }

    if (points.isEmpty() || linePoints.isEmpty()) {
        drawAxes()
        if (legendHeight > 0f) {
            drawChartLegend(context, legendItems, config.contentPaddingStart, right, 0f, legendHeight, config)
        }
        return
    }

    // 动画帧还可能保留旧数据的价格；必须一并参与范围计算，避免旧曲线映射到坐标轴外。
    var rawMin = Float.POSITIVE_INFINITY
    var rawMax = Float.NEGATIVE_INFINITY
    fun includeRange(rangePoints: List<KLineLinePoint>) {
        rangePoints.forEach { point ->
            rawMin = min(rawMin, point.close)
            rawMax = max(rawMax, point.close)
        }
    }
    includeRange(points)
    includeRange(linePoints)
    additionalLines.forEach { includeRange(it.points) }
    if (!rawMin.isFinite() || !rawMax.isFinite()) return
    val rawRange = rawMax - rawMin
    val padding = if (rawRange == 0f) max(1f, rawMax * config.valuePaddingRatio) else rawRange * config.valuePaddingRatio
    val valueMin = rawMin - padding
    val valueMax = rawMax + padding
    val valueRange = max(1f, valueMax - valueMin)
    // x 步长始终按全量数据计算，逐段展开时已出现的点不会横向位移。
    val xStep = if (points.size == 1) 0f else (right - left) / (points.size - 1)

    fun pointX(index: Int) = left + index * xStep
    fun pointY(point: KLineLinePoint) = bottom - (point.close - valueMin) / valueRange * (bottom - top)

    val displayPoints = revealChartPoints(linePoints, initialRevealProgress)
    val displayAdditionalLines = if (initialRevealProgress >= 1f) {
        additionalLines
    } else {
        additionalLines.map { line ->
            line.copy(points = revealChartPoints(line.points, initialRevealProgress))
        }
    }

    if (displayPoints.isNotEmpty()) {
        context.beginPath()
        context.moveTo(pointX(0), pointY(displayPoints.first()))
        for (index in 1 until displayPoints.size) {
            context.lineTo(pointX(index), pointY(displayPoints[index]))
        }
        context.lineTo(pointX(displayPoints.lastIndex), bottom)
        context.lineTo(pointX(0), bottom)
        context.closePath()
        context.createLinearGradient(0f, top, 0f, bottom).also { gradient ->
            gradient.addColorStop(0f, config.fillTopColor)
            gradient.addColorStop(1f, config.fillBottomColor)
            context.fillStyle(gradient)
        }
        context.fill()
    }
    drawAxes(valueMin, valueRange)

    drawChartLine(
        context = context,
        points = displayPoints,
        pointX = ::pointX,
        pointY = ::pointY,
        color = config.lineColor,
        lineWidth = config.lineWidth,
        smooth = false
    )
    displayAdditionalLines.forEach { line ->
        drawChartLine(
            context = context,
            points = line.points,
            pointX = ::pointX,
            pointY = ::pointY,
            color = line.color,
            lineWidth = line.lineWidth,
            smooth = line.smooth
        )
    }
    if (legendHeight > 0f) {
        drawChartLegend(
            context = context,
            items = legendItems,
            left = config.contentPaddingStart,
            right = right,
            top = 0f,
            height = legendHeight,
            config = config
        )
    }

    val selectedIndex = selectedPointIndex.takeIf { it in displayPoints.indices } ?: return
    val selectedPoint = points[selectedIndex]
    val selectedX = pointX(selectedIndex)
    val selectedY = pointY(selectedPoint)
    drawSelectionIndicator(
        context = context,
        height = height,
        left = left,
        right = right,
        top = top,
        bottom = bottom,
        x = selectedX,
        y = selectedY,
        point = selectedPoint,
        config = config
    )

}

private data class ChartLegendItem(
    val name: String,
    val color: Color,
)

private fun drawChartLegend(
    context: CanvasContext,
    items: List<ChartLegendItem>,
    left: Float,
    right: Float,
    top: Float,
    height: Float,
    config: KLineLineChartConfig,
) {
    var currentX = left
    val lineWidth = 12f
    val itemSpacing = 12f
    val centerY = top + height / 2f
    context.font(FontStyle.NORMAL, FontWeight.NORMAL, config.labelFontSize)
    context.textAlign(TextAlign.LEFT)
    items.forEach { item ->
        val textWidth = context.measureText(item.name).width
        val itemWidth = lineWidth + 4f + textWidth + itemSpacing
        if (currentX + itemWidth > right && currentX > left) return@forEach
        context.beginPath()
        context.moveTo(currentX, centerY)
        context.lineTo(currentX + lineWidth, centerY)
        context.strokeStyle(item.color)
        context.lineWidth(2f)
        context.lineCapRound()
        context.stroke()
        context.fillStyle(config.labelColor)
        context.fillText(item.name, currentX + lineWidth + 4f, centerY + config.labelFontSize * 0.30f)
        currentX += itemWidth
    }
}

private fun drawChartLine(
    context: CanvasContext,
    points: List<KLineLinePoint>,
    pointX: (Int) -> Float,
    pointY: (KLineLinePoint) -> Float,
    color: Color,
    lineWidth: Float,
    smooth: Boolean,
) {
    if (points.isEmpty()) return
    context.beginPath()
    context.moveTo(pointX(0), pointY(points.first()))
    if (smooth && points.size > 2) {
        for (index in 1 until points.lastIndex) {
            val next = points[index + 1]
            context.quadraticCurveTo(
                pointX(index),
                pointY(points[index]),
                (pointX(index) + pointX(index + 1)) / 2f,
                (pointY(points[index]) + pointY(next)) / 2f
            )
        }
    }
    val startIndex = if (smooth && points.size > 2) points.lastIndex else 1
    for (index in startIndex until points.size) {
        context.lineTo(pointX(index), pointY(points[index]))
    }
    context.strokeStyle(color)
    context.lineWidth(lineWidth)
    context.lineCapRound()
    context.stroke()
}

private fun drawSelectionIndicator(
    context: CanvasContext,
    height: Float,
    left: Float,
    right: Float,
    top: Float,
    bottom: Float,
    x: Float,
    y: Float,
    point: KLineLinePoint,
    config: KLineLineChartConfig
) {
    context.strokeStyle(config.selectionColor)
    context.lineWidth(config.selectionLineWidth)
    context.beginPath()
    context.moveTo(x, top)
    context.lineTo(x, bottom)
    context.moveTo(left, y)
    context.lineTo(right, y)
    context.stroke()

    if (config.selectionPointRadius > 0f) {
        context.beginPath()
        context.arc(x, y, config.selectionPointRadius, 0f, (2f * kotlin.math.PI).toFloat(), false)
        context.strokeStyle(config.selectionColor)
        context.lineWidth(config.selectionLineWidth)
        context.stroke()
    }

    context.font(FontStyle.NORMAL, FontWeight.NORMAL, config.labelFontSize)
    val labelHeight = config.labelFontSize + 6f
    if (config.showYAxis) {
        val yLabel = config.yAxisLabelFormatter(point.close)
        context.textAlign(TextAlign.RIGHT)
        val yLabelWidth = context.measureText(yLabel).width + 8f
        val yLabelRight = max(0f, left - 2f)
        val yLabelLeft = max(0f, yLabelRight - yLabelWidth)
        val yLabelTop = (y - labelHeight / 2f).coerceIn(0f, height - labelHeight)
        drawRoundedRect(
            context = context,
            left = yLabelLeft,
            top = yLabelTop,
            right = yLabelRight,
            bottom = yLabelTop + labelHeight,
            radius = 3f,
            color = config.selectionColor
        )
        context.fillStyle(config.selectionLabelTextColor)
        context.fillText(yLabel, left - 6f, selectionLabelBaseline(yLabelTop, labelHeight, config.labelFontSize))
    }

    if (config.showXAxis) {
        val xLabel = config.xAxisLabelFormatter(point)
        context.textAlign(TextAlign.CENTER)
        val xLabelWidth = context.measureText(xLabel).width + 8f
        val minCenter = min(left + xLabelWidth / 2f, right - xLabelWidth / 2f)
        val maxCenter = max(left + xLabelWidth / 2f, right - xLabelWidth / 2f)
        val xLabelCenter = x.coerceIn(minCenter, maxCenter)
        val xLabelTop = bottom + 2f
        drawRoundedRect(
            context = context,
            left = xLabelCenter - xLabelWidth / 2f,
            top = xLabelTop,
            right = xLabelCenter + xLabelWidth / 2f,
            bottom = min(height - config.contentPaddingBottom, xLabelTop + labelHeight),
            radius = 3f,
            color = config.selectionColor
        )
        context.fillStyle(config.selectionLabelTextColor)
        context.fillText(
            xLabel,
            xLabelCenter,
            selectionLabelBaseline(xLabelTop, labelHeight, config.labelFontSize)
        )
    }
}

/** Canvas 文本以基线定位；0.30 倍字号能使 Android/iOS 的字形在标签中视觉居中。 */
private fun selectionLabelBaseline(top: Float, height: Float, fontSize: Float): Float {
    return top + height / 2f + fontSize * 0.30f
}

private fun drawRoundedRect(
    context: CanvasContext,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    radius: Float,
    color: Color
) {
    val cornerRadius = min(radius, min((right - left) / 2f, (bottom - top) / 2f))
    context.beginPath()
    context.moveTo(left + cornerRadius, top)
    context.lineTo(right - cornerRadius, top)
    context.quadraticCurveTo(right, top, right, top + cornerRadius)
    context.lineTo(right, bottom - cornerRadius)
    context.quadraticCurveTo(right, bottom, right - cornerRadius, bottom)
    context.lineTo(left + cornerRadius, bottom)
    context.quadraticCurveTo(left, bottom, left, bottom - cornerRadius)
    context.lineTo(left, top + cornerRadius)
    context.quadraticCurveTo(left, top, left + cornerRadius, top)
    context.closePath()
    context.fillStyle(color)
    context.fill()
}

private fun resamplePointsForTransition(
    source: List<KLineLinePoint>,
    target: List<KLineLinePoint>
): List<KLineLinePoint> {
    if (target.isEmpty()) return emptyList()
    if (source.isEmpty()) return target.map { it.copy(close = it.close) }
    return target.indices.map { index ->
        val sourceIndex = if (target.size == 1) 0 else {
            (index.toFloat() * (source.lastIndex) / target.lastIndex).toInt()
        }
        source[sourceIndex].copy(timestamp = target[index].timestamp, label = target[index].label)
    }
}

/** 首次展示时仅返回当前进度覆盖的点，避免首帧一次性提交整条路径。 */
private fun revealChartPoints(
    points: List<KLineLinePoint>,
    progress: Float,
): List<KLineLinePoint> {
    if (points.isEmpty() || progress <= 0f) return emptyList()
    if (progress >= 1f) return points
    val visibleCount = ceil(points.size * progress.coerceIn(0f, 1f))
        .toInt()
        .coerceIn(1, points.size)
    return points.take(visibleCount)
}

private fun interpolateChartPoints(
    start: List<KLineLinePoint>,
    end: List<KLineLinePoint>,
    progress: Float
): List<KLineLinePoint> {
    if (end.isEmpty()) return emptyList()
    if (start.size != end.size) return end
    val fraction = progress.coerceIn(0f, 1f)
    return end.indices.map { index ->
        val from = start[index]
        val to = end[index]
        to.copy(close = from.close + (to.close - from.close) * fraction)
    }
}

/** ease-out 三次曲线。 */
private fun easeOutCubic(t: Float): Float {
    val inv = 1f - t
    return 1f - inv * inv * inv
}

/**
 * 选取 x 轴刻度索引。左右两端不占用 label 槽位，索引按内部槽位等分。
 */
private fun selectXAxisTickIndices(points: List<KLineLinePoint>, maxCount: Int): List<Int> {
    if (points.isEmpty()) return emptyList()
    val lastIndex = points.lastIndex
    val count = min(maxCount, lastIndex + 1)
    if (count <= 0) return emptyList()
    if (lastIndex < 2) return points.indices.toList()
    return (0 until count).map { slot ->
        (lastIndex.toFloat() * (slot + 1) / (count + 1) + 0.5f)
            .toInt()
            .coerceIn(1, lastIndex - 1)
    }.distinct()
}

/** Y 轴价格标签：固定两位小数（commonMain 无 String.format，手工拼串）。 */
private fun formatPriceLabel(value: Float): String {
    val cents = kotlin.math.abs(value * 100f).let { if (it - it.toInt() >= 0.5f) it.toInt() + 1 else it.toInt() }
    val units = cents / 100
    val remainder = (cents % 100).toString().padStart(2, '0')
    return (if (value < 0 && cents != 0) "-" else "") + units + "." + remainder
}
