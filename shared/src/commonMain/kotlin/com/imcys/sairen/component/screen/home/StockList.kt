package com.imcys.sairen.component.screen.home

import com.imcys.sairen.base.utils.changePercentBackgroundColor
import com.imcys.sairen.base.utils.changePercentColor
import com.imcys.sairen.base.utils.formatChangePercent
import com.imcys.sairen.component.SRDivider
import com.imcys.sairen.component.SRSweepLightImage
import com.imcys.sairen.core.common.ext.toCommonAssets
import com.imcys.sairen.theme.SRRadius
import com.imcys.sairen.theme.SRThemeColor
import com.imcys.sairen.ui.home.HomeScreenIntent
import com.imcys.sairen.ui.home.HomeScreenStore
import com.imcys.sairen.ui.home.StockListState
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.ViewRef
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.FooterRefresh
import com.tencent.kuikly.core.views.FooterRefreshEndState
import com.tencent.kuikly.core.views.FooterRefreshState
import com.tencent.kuikly.core.views.FooterRefreshView
import com.tencent.kuikly.core.views.List
import com.tencent.kuikly.core.views.Refresh
import com.tencent.kuikly.core.views.RefreshView
import com.tencent.kuikly.core.views.RefreshViewState
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.tencent.kuikly.core.views.layout.Column
import com.tencent.kuikly.core.views.layout.Row


internal class StockListView : ComposeView<StockListViewAttr, StockListViewEvent>() {

    private val store: HomeScreenStore
        get() = attr.store ?: error("HomeScreenStore is required")
    private val stockListState: StockListState
        get() = store.stockListState(attr.marketParams)
    private lateinit var refreshRef: ViewRef<RefreshView>
    private var footerRefreshRef: ViewRef<FooterRefreshView>? = null

    private var refreshText by observable("下拉刷新")
    private var footerRefreshText by observable("加载更多")

    override fun createEvent(): StockListViewEvent {
        return StockListViewEvent()
    }

    override fun createAttr(): StockListViewAttr {
        return StockListViewAttr()
    }

    override fun didInit() {
        super.didInit()
        refreshRef.view?.beginRefresh()
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            Column {

                Row {
                    attr {
                        padding(top = 15f, bottom = 10f)
                    }
                    View {
                        attr {
                            flex(1.4f)
                            padding(left = 4f)
                            overflow(true)
                        }
                        Text {
                            attr {
                                text("名称")
                                color(ctx.SRThemeColor.secondaryText)
                                fontSize(14f)
                                flex(1f)
                            }
                        }
                    }
                    View {
                        attr {
                            flex(1f)
                            padding(left = 8f)
                            alignItemsFlexEnd()

                        }
                        Text {
                            attr {
                                text("最新价")
                                fontSize(14f)
                                color(ctx.SRThemeColor.secondaryText)
                                lines(1)
                            }
                        }
                    }
                    View {
                        attr {
                            flex(1f)
                            padding(left = 8f)
                            alignItemsFlexEnd()

                        }
                        Text {
                            attr {
                                text("涨跌额")
                                color(ctx.SRThemeColor.secondaryText)
                                lines(1)
                                fontSize(14f)
                            }
                        }
                    }
                    View {
                        attr {
                            flex(1f)
                            padding(left = 8f)
                            alignItemsFlexEnd()

                        }
                        Text {
                            attr {
                                text("涨跌幅")
                                fontSize(14f)
                                color(ctx.SRThemeColor.secondaryText)
                                lines(1)
                            }
                        }
                    }
                }
                SRDivider()
            }
            List {
                attr {
                    flex(1f)
                    showScrollerIndicator(false)
                }
                // 刷新
                Refresh {
                    ref {
                        ctx.refreshRef = it
                    }
                    attr {
                        alignItemsCenter()
                    }

                    SRSweepLightImage {
                        attr {
                            size(60f, 60f)
                            imageUri = "refresh.png".toCommonAssets()
                        }
                    }
                    Text {
                        attr {
                            margin(bottom = 20f)
                            color(ctx.SRThemeColor.secondaryText)
                            fontSize(15f)
                            text(ctx.refreshText)
                        }
                    }
                    event {
                        refreshStateDidChange {
                            when (it) {
                                RefreshViewState.REFRESHING -> {
                                    ctx.refreshText = "正在刷新"
                                    ctx.event.onIntent(
                                        HomeScreenIntent.RefreshStockList(ctx.attr.marketParams) {
                                            ctx.refreshRef.view?.endRefresh()
                                            ctx.footerRefreshRef?.view?.resetRefreshState()
                                        },
                                    )
                                }

                                RefreshViewState.IDLE -> ctx.refreshText = "下拉刷新"
                                RefreshViewState.PULLING -> ctx.refreshText = "松手即可刷新"
                            }
                        }
                    }


                }
                // 列表项目
                vfor({ ctx.stockListState.stockDataList }) { stock ->
                    Column {
                        Row {
                            attr {
                                alignItemsCenter()
                            }

                            event {
                                click {
                                    ctx.event.onIntent(HomeScreenIntent.OpenStockDetail(stock))
                                }
                            }
                            Column {
                                attr {
                                    flex(1.4f)
                                    padding(left = 4f, right = 8f, top = 10f, bottom = 10f)
                                    overflow(true)
                                    justifyContentCenter()
                                    alignItemsFlexStart()
                                }
                                Text {
                                    attr {
                                        text(stock.name)
                                        fontSize(15f)
                                        textOverFlowTail()
                                        fontWeightBold()
                                        color(ctx.SRThemeColor.primaryText)
                                    }
                                }
                                Text {
                                    attr {
                                        text(stock.code)
                                        lines(1)
                                        fontSize(13f)
                                        textOverFlowTail()
                                        color(ctx.SRThemeColor.secondaryText)
                                    }
                                }
                            }
                            Column {
                                attr {
                                    flex(1f)
                                    padding(left = 8f, right = 8f)
                                    justifyContentCenter()
                                    alignItemsFlexEnd()
                                }
                                Text {
                                    attr {
                                        text(stock.latestPrice)
                                        lines(1)
                                        fontWeightBold()
                                        fontSize(15f)
                                        color(ctx.SRThemeColor.primaryText)
                                    }
                                }
                            }
                            Column {
                                attr {
                                    flex(1f)
                                    padding(left = 8f, right = 8f)
                                    justifyContentCenter()
                                    alignItemsFlexEnd()
                                }
                                Text {
                                    attr {
                                        lines(1)
                                        text(
                                            formatChangePercent(
                                                stock.changeAmount,
                                                appendPercentSign = false
                                            )
                                        )
                                        color(changePercentColor(stock.changeAmount, ctx.SRThemeColor))
                                        fontWeightBold()
                                        fontSize(15f)
                                    }
                                }

                            }
                            Column {
                                attr {
                                    flex(1f)
                                    padding(left = 8f)
                                    justifyContentCenter()
                                    alignItemsFlexEnd()                                }
                                View {
                                    attr {
                                        width(75f)
                                        padding(top = 10f, bottom = 10f, right = 5f, left = 5f)
                                        backgroundColor(
                                            changePercentBackgroundColor(
                                                stock.changePercent,
                                                ctx.SRThemeColor
                                            )
                                        )
                                        borderRadius(SRRadius.MEDIUM)
                                        alignItemsCenter()
                                        justifyContentCenter()
                                    }
                                    Text {
                                        attr {
                                            text(formatChangePercent(stock.changePercent))
                                            color(changePercentColor(stock.changePercent, ctx.SRThemeColor))
                                            lines(1)
                                            fontWeightBold()
                                            fontSize(15f)
                                        }
                                    }
                                }
                            }
                        }
                        SRDivider()
                    }
                }
                // 加载
                vif({ ctx.stockListState.stockDataList.isNotEmpty() }) {
                    FooterRefresh {
                        ref {
                            ctx.footerRefreshRef = it
                        }
                        attr {
                            preloadDistance(600f)
                            allCenter()
                        }

                        SRSweepLightImage {
                            attr {
                                width(153f * 1.3f)
                                height(102f * 1.3f)
                                margin(top = 10f)
                                imageUri = "loading.png".toCommonAssets()
                            }
                        }
                        event {
                            refreshStateDidChange {
                                when (it) {
                                    FooterRefreshState.REFRESHING -> {
                                        ctx.footerRefreshText = "加载更多中.."
                                        ctx.event.onIntent(
                                            HomeScreenIntent.LoadNextStockPage(ctx.attr.marketParams) { endState ->
                                                ctx.footerRefreshRef?.view?.endRefresh(endState)
                                            },
                                        )
                                    }

                                    FooterRefreshState.IDLE -> ctx.footerRefreshText = "加载更多"
                                    FooterRefreshState.NONE_MORE_DATA -> ctx.footerRefreshText =
                                        "无更多数据"

                                    FooterRefreshState.FAILURE -> ctx.footerRefreshText =
                                        "点击重试加载更多"
                                }
                            }
                        }

                    }
                }
            }
        }
    }

}


internal class StockListViewAttr : ComposeAttr() {
    var marketParams: String = "m:0+t:6,m:0+t:80,m:1+t:2,m:1+t:23"
    var store: HomeScreenStore? = null
}

internal class StockListViewEvent : ComposeEvent() {
    var onIntent: (HomeScreenIntent) -> Unit = {}
}

internal fun ViewContainer<*, *>.StockList(init: StockListView.() -> Unit) {
    addChild(StockListView(), init)
}
