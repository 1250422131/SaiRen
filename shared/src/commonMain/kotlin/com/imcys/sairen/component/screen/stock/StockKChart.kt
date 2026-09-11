package com.imcys.sairen.component.screen.stock

import com.imcys.sairen.component.SRDivider
import com.imcys.sairen.component.SREqualTabs
import com.imcys.sairen.core.chart.KLineLineChart
import com.imcys.sairen.core.chart.KLineLineChartConfig
import com.imcys.sairen.core.chart.KLineLinePoint
import com.imcys.sairen.core.chart.KLineLineSeries
import com.imcys.sairen.model.StockChartTab
import com.imcys.sairen.model.stockChartTabList
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

internal class StockKChartView : ComposeView<StockKChartViewAttr, StockKChartViewEvent>() {

    override fun createEvent(): StockKChartViewEvent {
        return StockKChartViewEvent()
    }

    override fun createAttr(): StockKChartViewAttr {
        return StockKChartViewAttr()
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            SREqualTabs<StockChartTab> {
                attr {
                    scrollMode()
                    dataList = stockChartTabList
                    defaultIndex = 0
                }
                event {
                    onTabItem = {
                        {
                            Text {
                                attr {
                                    marginBottom(7f)
                                    fontWeightBold()
                                    fontSize(15f)
                                    text(it.title)
                                }
                            }
                        }
                    }
                    onTabClick = { item ->
                        ctx.event.onTabClick(item)
                    }
                }

            }

            SRDivider {  }

            View {
                attr {
                    height(ctx.attr.chartHeight)
                    margin(top = 10f)
                }
                KLineLineChart {
                    attr {
                        absolutePositionAllZero()
                        points = { ctx.attr.trends() }
                        additionalLines = { ctx.attr.additionalLines() }
                        config = KLineLineChartConfig(
                            lineColor = Color(0xFF2B7FFF),
                            fillTopColor = Color(0x332B7FFF),
                            fillBottomColor = Color(0x002B7FFF),
                            showLegend = true,
                            primaryLineName = "价格"
                        )
                    }
                }
            }
        }
    }
}


internal class StockKChartViewAttr : ComposeAttr() {
    /** 图表容器高度，调用方可按页面空间覆盖。 */
    var chartHeight: Float = 220f
    var trends: () -> List<KLineLinePoint> = { emptyList() }
    var additionalLines: () -> List<KLineLineSeries> = { emptyList() }
}

internal class StockKChartViewEvent : ComposeEvent() {
    var onTabClick: (StockChartTab) -> Unit = {}
}

internal fun ViewContainer<*, *>.StockKChart(init: StockKChartView.() -> Unit) {
    addChild(StockKChartView(), init)
}
