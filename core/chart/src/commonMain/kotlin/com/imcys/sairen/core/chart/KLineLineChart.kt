package com.imcys.sairen.core.chart

import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.timer.setTimeout
import com.tencent.kuikly.core.views.Canvas
import com.tencent.kuikly.core.views.CanvasContext
import com.tencent.kuikly.core.views.FontStyle
import com.tencent.kuikly.core.views.FontWeight
import com.tencent.kuikly.core.views.TextAlign
import kotlin.math.abs
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

/** K 线收盘价折线图的绘制参数。 */
data class KLineLineChartConfig(
    val contentPaddingStart: Float = 12f,
    val contentPaddingTop: Float = 12f,
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
        require(lineWidth > 0f)
        require(valuePaddingRatio >= 0f)
        require(maxXAxisLabelCount >= 2)
        require(maxYAxisLabelCount >= 2)
        require(yAxisLabelWidth >= 0f)
        require(xAxisLabelHeight >= 0f)
        require(labelFontSize > 0f)
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

    override fun createAttr(): KLineLineChartAttr = KLineLineChartAttr()

    override fun createEvent(): ComposeEvent = ComposeEvent()

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            Canvas(
                {
                    attr {
                        absolutePositionAllZero()
                    }
                }
            ) { context, width, height ->
                // 在绘制回调内读取数据，确保 points lambda 引用的 observable 更新后重绘
                val points = ctx.attr.points()
                if (points != ctx.lastPoints) {
                    val visiblePoints = interpolateChartPoints(
                        ctx.animationStartPoints,
                        ctx.animationEndPoints,
                        ctx.animProgress
                    )
                    // 保存快照，避免外部集合原地更新时丢失上一帧曲线。
                    ctx.lastPoints = points.toList()
                    ctx.animationStartPoints = resamplePointsForTransition(visiblePoints, points)
                    ctx.animationEndPoints = points
                    if (!ctx.animPending) {
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
                val linePoints = interpolateChartPoints(
                    ctx.animationStartPoints,
                    ctx.animationEndPoints,
                    easeOutCubic(progress.coerceIn(0f, 1f))
                )
                drawKLineLineChart(context, width, height, points, linePoints, ctx.attr.config)
            }
        }
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

private fun drawKLineLineChart(
    context: CanvasContext,
    width: Float,
    height: Float,
    points: List<KLineLinePoint>,
    linePoints: List<KLineLinePoint>,
    config: KLineLineChartConfig
) {
    if (points.isEmpty() || linePoints.isEmpty()) return

    val left = config.contentPaddingStart + if (config.showYAxis) config.yAxisLabelWidth else 0f
    val right = max(left, width - config.contentPaddingEnd)
    val top = config.contentPaddingTop
    val bottom = max(top, height - config.contentPaddingBottom - if (config.showXAxis) config.xAxisLabelHeight else 0f)
    // 动画帧还可能保留旧数据的价格；必须一并参与范围计算，避免旧曲线映射到坐标轴外。
    val values = points.map(KLineLinePoint::close) + linePoints.map(KLineLinePoint::close)
    val rawMin = values.minOrNull() ?: return
    val rawMax = values.maxOrNull() ?: return
    val rawRange = rawMax - rawMin
    val padding = if (rawRange == 0f) max(1f, rawMax * config.valuePaddingRatio) else rawRange * config.valuePaddingRatio
    val valueMin = rawMin - padding
    val valueMax = rawMax + padding
    val valueRange = max(1f, valueMax - valueMin)
    // x 步长按全量数据计算，保证动画期间折线从左往右扫过而不是整体压缩拉伸
    val xStep = if (points.size == 1) 0f else (right - left) / (points.size - 1)

    fun pointX(index: Int) = left + index * xStep
    fun pointY(point: KLineLinePoint) = bottom - (point.close - valueMin) / valueRange * (bottom - top)

    val displayPoints = linePoints

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

    fun drawAxes() {
    if (config.showYAxis) {
        val yTicks = min(config.maxYAxisLabelCount, points.size)
        for (tick in 0 until yTicks) {
            val fraction = if (yTicks == 1) 0f else tick.toFloat() / (yTicks - 1)
            val y = bottom - fraction * (bottom - top)
            drawDashedLine(left, y, right, y, config.gridColor)
            context.fillStyle(config.labelColor)
            context.font(FontStyle.NORMAL, FontWeight.NORMAL, config.labelFontSize)
            context.textAlign(TextAlign.RIGHT)
            context.fillText(config.yAxisLabelFormatter(valueMin + fraction * valueRange), left - 6f, y + config.labelFontSize / 2f)
        }
    }
    if (config.showXAxis) {
        val tickIndices = selectXAxisTickIndices(points, config.maxXAxisLabelCount)
        // 两端纵轴原点不放 label，所有 label 在内部槽位中等距分布。
        val tickCount = tickIndices.size
        fun tickX(slot: Int): Float {
            if (tickCount == 0) return left
            return left + (right - left) * (slot + 1) / (tickCount + 1).toFloat()
        }
        // 竖向网格线：从每个刻度位置向上贯穿绘图区，连接标签与横向坐标轴
        tickIndices.forEachIndexed { slot, _ ->
            val x = tickX(slot)
            drawDashedLine(x, top, x, bottom, config.gridColor)
        }
        tickIndices.forEachIndexed { pos, index ->
            val x = tickX(pos)
            context.fillStyle(config.labelColor)
            context.font(FontStyle.NORMAL, FontWeight.NORMAL, config.labelFontSize)
            context.textAlign(TextAlign.CENTER)
            context.fillText(config.xAxisLabelFormatter(points[index]), x, height - config.contentPaddingBottom)
        }
    }

    drawDashedLine(left, top, left, bottom, config.axisColor)
    drawDashedLine(left, bottom, right, bottom, config.axisColor)
    }

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
    drawAxes()

    context.beginPath()
    context.moveTo(pointX(0), pointY(displayPoints.first()))
    for (index in 1 until displayPoints.size) {
        context.lineTo(pointX(index), pointY(displayPoints[index]))
    }
    context.strokeStyle(config.lineColor)
    context.lineWidth(config.lineWidth)
    context.lineCapRound()
    context.stroke()

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
