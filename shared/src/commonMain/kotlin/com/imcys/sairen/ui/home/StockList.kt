package com.imcys.sairen.ui.home

import com.imcys.sairen.base.utils.changePercentBackgroundColor
import com.imcys.sairen.base.utils.changePercentColor
import com.imcys.sairen.base.utils.formatChangePercent
import com.imcys.sairen.core.common.ext.acquireRouterModule
import com.imcys.sairen.component.SRDivider
import com.imcys.sairen.component.SRSweepLightImage
import com.imcys.sairen.core.common.ext.toCommonAssets
import com.imcys.sairen.core.network.NetWorkResult
import com.imcys.sairen.core.network.emptyNetWorkResult
import com.imcys.sairen.core.network.model.Stock
import com.imcys.sairen.core.network.model.StockList
import com.imcys.sairen.core.network.service.AppApiService
import com.imcys.sairen.core.network.toKJSONObject
import com.imcys.sairen.theme.SRThemeColor
import com.imcys.sairen.theme.SRRadius
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.ViewRef
import com.tencent.kuikly.core.coroutines.launch
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.manager.PagerManager
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.reactive.handler.observableList
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

    private companion object {
        const val STOCK_PAGE_SIZE = 30
    }

    private var stockDataList by observableList<Stock>()
    private var stockResult by observable<NetWorkResult<StockList>>(emptyNetWorkResult())
    private lateinit var refreshRef: ViewRef<RefreshView>
    private var footerRefreshRef: ViewRef<FooterRefreshView>? = null
    private var currentPage = 1
    private var totalStockCount = 0
    private var isLoadingNextPage = false

    private var refreshText by observable("下拉刷新")
    private var footerRefreshText by observable("加载更多")
    private var redBlockStickOriginTop = 300f
    private var redBlockStickTop by observable(redBlockStickOriginTop)

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

    private fun loadStockList() {
        val ctx = this
        val pager = PagerManager.getPager(pagerId) as Pager
        pager.lifecycleScope.launch {
            AppApiService.getStockList(
                pager = pager,
                pn = 1,
                pz = STOCK_PAGE_SIZE,
                fs = attr.marketParams,
            ).collect { result ->
                stockResult = result
                when (result) {
                    is NetWorkResult.Success -> {
                        val stockList = result.data
                        ctx.currentPage = 1
                        ctx.totalStockCount = stockList?.total ?: 0
                        ctx.stockDataList.clear()
                        ctx.stockDataList.addAll(stockList?.diff ?: emptyList())
                        ctx.refreshRef.view?.endRefresh()
                        ctx.footerRefreshRef?.view?.resetRefreshState(
                            if (ctx.stockDataList.size >= ctx.totalStockCount) {
                                FooterRefreshState.NONE_MORE_DATA
                            } else {
                                FooterRefreshState.IDLE
                            },
                        )
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun loadNextPage() {
        if (isLoadingNextPage) {
            return
        }
        if (stockDataList.size >= totalStockCount) {
            footerRefreshRef?.view?.endRefresh(FooterRefreshEndState.NONE_MORE_DATA)
            return
        }

        isLoadingNextPage = true
        val nextPage = currentPage + 1
        val ctx = this
        val pager = PagerManager.getPager(pagerId) as Pager
        pager.lifecycleScope.launch {
            AppApiService.getStockList(
                pager = pager,
                pn = nextPage,
                pz = STOCK_PAGE_SIZE,
                fs = attr.marketParams,
            ).collect { result ->
                stockResult = result
                when (result) {
                    is NetWorkResult.Success -> {
                        val stockList = result.data
                        val nextStocks = stockList?.diff.orEmpty()
                        ctx.totalStockCount = stockList?.total ?: ctx.totalStockCount
                        if (nextStocks.isNotEmpty()) {
                            ctx.stockDataList.addAll(nextStocks)
                            ctx.currentPage = nextPage
                        }
                        ctx.isLoadingNextPage = false
                        ctx.footerRefreshRef?.view?.endRefresh(
                            if (nextStocks.isEmpty() || ctx.stockDataList.size >= ctx.totalStockCount) {
                                FooterRefreshEndState.NONE_MORE_DATA
                            } else {
                                FooterRefreshEndState.SUCCESS
                            },
                        )
                    }

                    is NetWorkResult.Error -> {
                        ctx.isLoadingNextPage = false
                        ctx.footerRefreshRef?.view?.endRefresh(FooterRefreshEndState.FAILURE)
                    }

                    else -> Unit
                }
            }
        }
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            val colors = ctx.SRThemeColor
            Column {

                Row {
                attr {
                    padding(top = 15f, bottom = 10f)
                }
                View {
                    attr {
                        flex(1.2f)
                        padding(left = 4f)
                        overflow(true)
                    }
                    Text {
                        attr {
                            text("名称")
                            color(colors.secondaryText)
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
                            color(colors.secondaryText)
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
                            color(colors.secondaryText)
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
                            color(colors.secondaryText)
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
                            color(colors.secondaryText)
                            fontSize(15f)
                            text(ctx.refreshText)
                        }
                    }
                    event {
                        refreshStateDidChange {
                            when (it) {
                                RefreshViewState.REFRESHING -> {
                                    ctx.refreshText = "正在刷新"
                                    ctx.loadStockList()
                                }

                                RefreshViewState.IDLE -> ctx.refreshText = "下拉刷新"
                                RefreshViewState.PULLING -> ctx.refreshText = "松手即可刷新"
                            }
                        }
                    }


                }
                // 列表项目
                vfor({ ctx.stockDataList }) { stock ->
                    Column {
                        Row {
                        attr {
                            alignItemsCenter()
                        }

                        event {
                            click {
                                ctx.acquireRouterModule()
                                    .openPage("stock_detail", stock.toKJSONObject())
                            }
                        }
                        View {
                            attr {
                                flex(1.2f)
                                padding(left = 4f, right = 8f, top = 10f, bottom = 10f)
                                overflow(true)
                            }
                            Text {
                                attr {
                                    text(stock.name)
                                    fontSize(15f)
                                    textOverFlowTail()
                                    flex(1f)
                                    fontWeightBold()
                                    color(colors.primaryText)
                                }
                            }
                            Text {
                                attr {
                                    text(stock.code)
                                    lines(1)
                                    fontSize(13f)
                                    textOverFlowTail()
                                    flex(1f)
                                    color(colors.secondaryText)
                                }
                            }
                        }
                        View {
                            attr {
                                flex(1f)
                                padding(left = 8f, right = 8f)
                                alignItemsFlexEnd()
                            }
                            Text {
                                attr {
                                    text(stock.latestPrice)
                                    lines(1)
                                    fontWeightBold()
                                    fontSize(15f)
                                    color(colors.primaryText)
                                }
                            }
                        }
                        View {
                            attr {
                                flex(1f)
                                padding(left = 8f, right = 8f)
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
                                    color(changePercentColor(stock.changeAmount, colors))
                                    fontWeightBold()
                                    fontSize(15f)
                                }
                            }

                        }
                        View {
                            attr {
                                flex(1f)
                                padding(left = 8f)
                                alignItemsFlexEnd()
                            }
                            View {
                                attr {
                                    width(75f)
                                    padding(top = 10f, bottom = 10f, right = 5f, left = 5f)
                                    backgroundColor(
                                        changePercentBackgroundColor(
                                            stock.changePercent,
                                            colors
                                        )
                                    )
                                    borderRadius(SRRadius.MEDIUM)
                                    alignItemsCenter()
                                    justifyContentCenter()
                                }
                                Text {
                                    attr {
                                        text(formatChangePercent(stock.changePercent))
                                        color(changePercentColor(stock.changePercent, colors))
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
                vif({ ctx.stockDataList.isNotEmpty() }) {
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
                                        ctx.loadNextPage()
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
}

internal class StockListViewEvent : ComposeEvent() {

}

internal fun ViewContainer<*, *>.StockList(init: StockListView.() -> Unit) {
    addChild(StockListView(), init)
}
