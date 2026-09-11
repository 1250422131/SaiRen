package com.imcys.sairen.ui.search

import com.imcys.sairen.base.BasePager
import com.imcys.sairen.component.SRCard
import com.imcys.sairen.component.SRNavigationBar
import com.imcys.sairen.core.common.ext.acquireRouterModule
import com.imcys.sairen.core.network.model.Stock
import com.imcys.sairen.core.network.toKJSONObject
import com.imcys.sairen.core.common.ext.acquireRouterModule
import com.imcys.sairen.theme.SRRadius
import com.tencent.kuikly.core.base.Border
import com.tencent.kuikly.core.base.BorderStyle
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.views.Input
import com.tencent.kuikly.core.views.List
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.tencent.kuikly.core.views.layout.Column
import com.tencent.kuikly.core.views.layout.Row

@Page("search")
internal class SearchPage : BasePager() {
    private val store = SearchStore(this)

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr { flex(1f); backgroundColor(Color(ctx.SRThemeColor.pageBackground)) }
            SRNavigationBar {
                attr { title = "搜索"; enabledBack = true }
            }
            View {
                attr {
                    margin(left = 20f, right = 20f, top = 8f, bottom = 12f)
                    padding(left = 12f, right = 12f)
                    height(46f)
                    borderRadius(SRRadius.LARGE)
                    border(Border(1f, BorderStyle.SOLID, Color(ctx.SRThemeColor.divider)))
                    backgroundColor(Color(ctx.SRThemeColor.stockInfoCard))
                }
                Input {
                    attr {
                        flex(1f); height(44f); fontSize(15f)
                        color(Color(ctx.SRThemeColor.primaryText))
                        placeholder("输入股票名称、代码或拼音")
                        placeholderColor(Color(ctx.SRThemeColor.secondaryText))
                    }
                    event { textDidChange { ctx.store.changeKeyword(it.text) } }
                }
            }
            List {
                attr {
                    flex(1f);
                    showScrollerIndicator(false)
                    margin(left = 20f, right = 20f)
                }
                vfor({ ctx.store.state.suggestions }) { item ->
                    SRCard {
                        attr {
                            margin(bottom = 10f)
                            padding(left = 16f, right = 16f, top = 13f, bottom = 13f)
                            backgroundColor(Color(ctx.SRThemeColor.stockInfoCard))
                        }
                        event {
                            click {
                                ctx.acquireRouterModule().openPage(
                                    "stock_detail",
                                    Stock(
                                        code = item.code.ifBlank { item.unifiedCode },
                                        eastMoneyMarketCode = item.marketNumber,
                                        name = item.name,
                                        latestPrice = "", changePercent = "", changeAmount = "",
                                        volume = "", amount = "",
                                    ).toKJSONObject(),
                                )
                            }
                        }
                        Row {
                            attr { alignItemsCenter() }
                            Column {
                                attr { flex(1f) }
                                Text {
                                    attr {
                                        text(item.name); fontSize(17f); fontWeightBold(); color(
                                        ctx.SRThemeColor.primaryText
                                    )
                                    }
                                }
                                Row {
                                    attr { margin(top = 5f); alignItemsCenter() }
                                    Text {
                                        attr {
                                            text(item.code)
                                            fontSize(13f)
                                            color(ctx.SRThemeColor.secondaryText)
                                        }
                                    }
                                }
                            }

                            View {
                                attr {
                                    margin(left = 8f)
                                    padding(left = 7f, right = 7f, top = 3f, bottom = 3f)
                                    borderRadius(SRRadius.MEDIUM)
                                    backgroundColor(Color(ctx.SRThemeColor.stockTagBackground))
                                }
                                Text {
                                    attr {
                                        text(item.securityTypeName)
                                        fontSize(12f)
                                        color(Color(ctx.SRThemeColor.primary))
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }
}
