package com.imcys.sairen.component.screen.stock

import com.imcys.sairen.component.SRDivider
import com.imcys.sairen.component.SREqualTabs
import com.imcys.sairen.model.stockChartTabList
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.views.Text

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
            SREqualTabs {
                attr {
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

                    }
                }

            }
            SRDivider {  }
        }
    }
}


internal class StockKChartViewAttr : ComposeAttr() {

}

internal class StockKChartViewEvent : ComposeEvent() {

}

internal fun ViewContainer<*, *>.StockKChart(init: StockKChartView.() -> Unit) {
    addChild(StockKChartView(), init)
}
