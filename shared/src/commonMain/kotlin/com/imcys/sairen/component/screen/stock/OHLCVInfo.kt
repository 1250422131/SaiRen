package com.imcys.sairen.component.screen.stock

import com.imcys.sairen.core.network.NetWorkResult
import com.imcys.sairen.core.network.emptyNetWorkResult
import com.imcys.sairen.core.network.model.StockDetail
import com.imcys.sairen.base.utils.formatVolume
import com.imcys.sairen.base.utils.priceColor
import com.imcys.sairen.component.SRDivider
import com.imcys.sairen.component.SRDividerDirection
import com.imcys.sairen.theme.SRThemeColor
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.tencent.kuikly.core.views.layout.Column
import com.tencent.kuikly.core.views.layout.Row

/**
 * 盘口数据
 */
internal class OHLCVInfoView : ComposeView<OHLCVInfoViewAttr, OHLCVInfoViewEvent>() {

    override fun createEvent(): OHLCVInfoViewEvent {
        return OHLCVInfoViewEvent()
    }

    override fun createAttr(): OHLCVInfoViewAttr {
        return OHLCVInfoViewAttr()
    }

    override fun body(): ViewBuilder {
        val ctx = this
        val colors = ctx.SRThemeColor
        return {
            Row {
                attr {
                    alignItemsCenter()
                }
                Column {
                    attr {
                        flex(1f)
                        alignItemsCenter()

                    }
                    Text {
                        attr {
                            fontSize(16f)
                            fontWeightBold()
                            text("最高价")
                        }
                    }
                    Text {
                        attr {
                            fontSize(14f)
                            margin(top = 7f)
                            fontWeightBold()
                            text(ctx.attr.details().data?.highestPrice ?: "")
                            color(
                                priceColor(
                                    price = ctx.attr.details().data?.highestPrice ?: "",
                                    previousClosePrice = ctx.attr.details().data?.previousClosePrice ?: "",
                                    colors = colors,
                                )
                            )
                        }
                    }
                }
                SRDivider {
                    attr {
                        direction = SRDividerDirection.VERTICAL
                        verticalLength = 32f
                        margin(top = 5f, bottom = 5f)
                    }
                }
                Column {
                    attr {
                        flex(1f)
                        alignItemsCenter()
                    }
                    Text {
                        attr {
                            fontSize(16f)
                            fontWeightBold()
                            text("最低价")
                        }
                    }
                    Text {
                        attr {
                            fontSize(14f)
                            margin(top = 7f)
                            fontWeightBold()
                            text(ctx.attr.details().data?.lowestPrice ?: "")
                            color(
                                priceColor(
                                    price = ctx.attr.details().data?.lowestPrice ?: "",
                                    previousClosePrice = ctx.attr.details().data?.previousClosePrice ?: "",
                                    colors = colors,
                                )
                            )
                        }
                    }
                }
                SRDivider {
                    attr {
                        direction = SRDividerDirection.VERTICAL
                        verticalLength = 32f
                        margin(top = 5f, bottom = 5f)
                    }
                }
                Column {
                    attr {
                        flex(1f)
                        alignItemsCenter()
                    }
                    Text {
                        attr {
                            fontSize(16f)
                            fontWeightBold()
                            text("成交量")
                        }
                    }
                    Text {
                        attr {
                            margin(top = 7f)
                            fontWeightBold()
                            text(formatVolume(ctx.attr.details().data?.volume ?: ""))
                            fontSize(14f)
                        }
                    }
                }
            }
            View {
                attr { marginTop(10f) }
            }
        }
    }
}


internal class OHLCVInfoViewAttr : ComposeAttr() {
    var details: () -> NetWorkResult<StockDetail> = { emptyNetWorkResult() }
}

internal class OHLCVInfoViewEvent : ComposeEvent() {

}

internal fun ViewContainer<*, *>.OHLCVInfo(init: OHLCVInfoView.() -> Unit) {
    addChild(OHLCVInfoView(), init)
}
