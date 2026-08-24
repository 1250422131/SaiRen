package com.imcys.sairen.component.screen.stock

import com.imcys.sairen.base.utils.changePercentColor
import com.imcys.sairen.base.utils.formatChangePercent
import com.imcys.sairen.core.network.NetWorkResult
import com.imcys.sairen.core.network.emptyNetWorkResult
import com.imcys.sairen.core.network.model.StockDetail
import com.imcys.sairen.theme.SRRadius
import com.imcys.sairen.theme.SRThemeColor
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.layout.Column
import com.tencent.kuikly.core.views.layout.Row

internal class StockDetailTopInfoView :
    ComposeView<StockDetailTopInfoViewAttr, StockDetailTopInfoViewEvent>() {

    override fun createEvent(): StockDetailTopInfoViewEvent {
        return StockDetailTopInfoViewEvent()
    }

    override fun createAttr(): StockDetailTopInfoViewAttr {
        return StockDetailTopInfoViewAttr()
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            Row {
                attr {
                    alignItemsCenter()
                }
                // 基本信息
                Column {
                    attr {
                        flex(1f)
                    }
                    Text {
                        attr {
                            fontSize(24f)
                            fontWeightBold()
                            text(ctx.attr.details().data?.name ?: "")
                            color(ctx.SRThemeColor.primaryText)
                        }
                    }

                    Row {
                        attr {
                            margin(top = 8f)
                            alignItemsCenter()
                        }
                        Text {
                            attr {
                                fontSize(14f)
                                text(ctx.attr.details().data?.code ?: "")
                                color(ctx.SRThemeColor.secondaryText)
                            }
                        }


                        // 股市分类
                        Column {
                            attr {
                                margin(left = 8f)
                                backgroundColor(ctx.SRThemeColor.stockTagBackground)
                                padding(top = 4f, bottom = 4f, left = 8f, right = 8f)
                                borderRadius(SRRadius.MEDIUM)
                            }
                            Text {
                                attr {
                                    fontSize(12f)
                                    text(ctx.attr.details().data?.marketCategory?.displayName ?: "")
                                    color(ctx.SRThemeColor.secondaryText)
                                }
                            }
                        }

                        // 股市类型
                        Column {
                            attr {
                                margin(left = 6f)
                                backgroundColor(ctx.SRThemeColor.stockTagBackground)
                                padding(top = 4f, bottom = 4f, left = 8f, right = 8f)
                                borderRadius(SRRadius.MEDIUM)
                            }
                            Text {
                                attr {
                                    fontSize(12f)
                                    text(ctx.attr.details().data?.market?.displayName ?: "")
                                    color(ctx.SRThemeColor.secondaryText)
                                }
                            }
                        }

                    }
                }

                // 涨跌
                Column {
                    attr {
                        alignItemsFlexEnd()
                    }
                    Text {
                        attr {
                            fontSize(23f)
                            fontWeightBold()
                            text(ctx.attr.details().data?.latestPrice ?: "")
                            color(changePercentColor(ctx.attr.details().data?.changeAmount ?: "", ctx.SRThemeColor))

                        }
                    }
                    Row {
                        attr {
                            marginTop(5f)
                        }
                        Text {
                            attr {
                                fontSize(17f)
                                fontWeightBold()
                                text(
                                    formatChangePercent(
                                        ctx.attr.details().data?.changeAmount ?: "",
                                        appendPercentSign = false
                                    )
                                )
                                color(changePercentColor(ctx.attr.details().data?.changeAmount ?: "", ctx.SRThemeColor))
                            }
                        }
                        Text {
                            attr {
                                margin(left = 10f)
                                fontSize(17f)
                                fontWeightBold()
                                text(formatChangePercent(ctx.attr.details().data?.changePercent ?: ""))
                                color(changePercentColor(ctx.attr.details().data?.changePercent ?: "", ctx.SRThemeColor))
                            }
                        }
                    }
                }
            }

        }

    }
}


internal class StockDetailTopInfoViewAttr : ComposeAttr() {
    var details: () -> NetWorkResult<StockDetail> = { emptyNetWorkResult() }
}

internal class StockDetailTopInfoViewEvent : ComposeEvent() {

}

internal fun ViewContainer<*, *>.StockDetailTopInfo(init: StockDetailTopInfoView.() -> Unit) {
    addChild(StockDetailTopInfoView(), init)
}
