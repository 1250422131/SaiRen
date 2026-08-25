package com.imcys.sairen.component.screen.stock

import com.imcys.sairen.component.SRDivider
import com.imcys.sairen.component.SREqualTabs
import com.imcys.sairen.core.chart.KLineLineChart
import com.imcys.sairen.core.chart.KLineLineChartConfig
import com.imcys.sairen.core.chart.KLineLinePoint
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
                    height(220f)
                    margin(top = 16f, bottom = 16f)
                }
                KLineLineChart {
                    attr {

                        absolutePositionAllZero()
                        points = { ctx.attr.trends() }
                        config = KLineLineChartConfig(
                            lineColor = Color(0xFF2B7FFF),
                            fillTopColor = Color(0x332B7FFF),
                            fillBottomColor = Color(0x002B7FFF)
                        )
                    }
                }
            }
        }
    }
}


internal class StockKChartViewAttr : ComposeAttr() {
    var trends: () -> List<KLineLinePoint> = { emptyList() }
}

internal class StockKChartViewEvent : ComposeEvent() {
    var onTabClick: (StockChartTab) -> Unit = {}
}

internal fun ViewContainer<*, *>.StockKChart(init: StockKChartView.() -> Unit) {
    addChild(StockKChartView(), init)
}
