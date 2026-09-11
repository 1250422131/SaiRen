package com.imcys.sairen.ui.home

import com.imcys.sairen.core.common.ext.toObservableList
import com.imcys.sairen.core.network.NetWorkResult
import com.imcys.sairen.core.network.emptyNetWorkResult
import com.imcys.sairen.core.network.model.Stock
import com.imcys.sairen.core.network.model.StockList
import com.imcys.sairen.core.network.service.AppApiService
import com.imcys.sairen.model.Market
import com.imcys.sairen.model.marketList
import com.tencent.kuikly.core.coroutines.launch
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.ObservableThreadSafetyMode
import com.tencent.kuikly.core.reactive.collection.ObservableList
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.FooterRefreshEndState

internal sealed interface HomeScreenIntent {
    data class SelectMarket(val index: Int) : HomeScreenIntent
    data class RefreshStockList(
        val marketParams: String,
        val onFinished: () -> Unit = {},
    ) : HomeScreenIntent
    data class LoadNextStockPage(
        val marketParams: String,
        val onFinished: (FooterRefreshEndState) -> Unit = {},
    ) : HomeScreenIntent
    data class OpenStockDetail(val stock: Stock) : HomeScreenIntent
}

internal data class StockListState(
    val stockDataList: ObservableList<Stock> = ObservableList(),
    val stockResult: NetWorkResult<StockList> = emptyNetWorkResult(),
    val currentPage: Int = 1,
    val totalStockCount: Int = 0,
    val isRefreshing: Boolean = false,
    val isLoadingNextPage: Boolean = false,
    val loadNextPageEndState: FooterRefreshEndState = FooterRefreshEndState.SUCCESS,
)

internal data class HomeScreenState(
    val markets: List<Market> = marketList,
    val selectedMarketIndex: Int = 0,
    val stockListStateMap: Map<String, StockListState> = emptyMap(),
)

internal class HomeScreenStore(private val pager: Pager) {
    var state by pager.observable(ObservableThreadSafetyMode.NONE, HomeScreenState())
        private set

    val markets: List<Market>
        get() = state.markets

    val selectedMarketIndex: Int
        get() = state.selectedMarketIndex

    fun stockListState(marketParams: String): StockListState {
        return state.stockListStateMap[marketParams] ?: StockListState()
    }

    fun dispatch(intent: HomeScreenIntent) {
        when (intent) {
            is HomeScreenIntent.SelectMarket -> reduce { copy(selectedMarketIndex = intent.index) }
            is HomeScreenIntent.RefreshStockList -> refreshStockList(intent)
            is HomeScreenIntent.LoadNextStockPage -> loadNextStockPage(intent)
            is HomeScreenIntent.OpenStockDetail -> Unit
        }
    }

    private fun refreshStockList(intent: HomeScreenIntent.RefreshStockList) {
        val marketParams = intent.marketParams
        if (stockListState(marketParams).isRefreshing) return

        reduceStockListState(marketParams) { copy(isRefreshing = true) }
        pager.lifecycleScope.launch {
            AppApiService.getStockList(
                pager = pager,
                pn = 1,
                pz = STOCK_PAGE_SIZE,
                fs = marketParams,
            ).collect { result ->
                if (result is NetWorkResult.Success) {
                    val stockList = result.data
                    reduceStockListState(marketParams) {
                        copy(
                            stockDataList = stockList?.diff.orEmpty().toObservableList(),
                            stockResult = result,
                            currentPage = 1,
                            totalStockCount = stockList?.total ?: 0,
                            isRefreshing = false,
                        )
                    }
                    intent.onFinished()
                } else {
                    reduceStockListState(marketParams) {
                        copy(
                            stockResult = result,
                            isRefreshing = result is NetWorkResult.Loading,
                        )
                    }
                    if (result is NetWorkResult.Error) {
                        intent.onFinished()
                    }
                }
            }
        }
    }

    private fun loadNextStockPage(intent: HomeScreenIntent.LoadNextStockPage) {
        val marketParams = intent.marketParams
        val stockListState = stockListState(marketParams)
        if (stockListState.isLoadingNextPage) return

        if (stockListState.stockDataList.size >= stockListState.totalStockCount) {
            reduceStockListState(marketParams) {
                copy(
                    isLoadingNextPage = false,
                    loadNextPageEndState = FooterRefreshEndState.NONE_MORE_DATA,
                )
            }
            intent.onFinished(FooterRefreshEndState.NONE_MORE_DATA)
            return
        }

        reduceStockListState(marketParams) { copy(isLoadingNextPage = true) }
        val nextPage = stockListState.currentPage + 1
        pager.lifecycleScope.launch {
            AppApiService.getStockList(
                pager = pager,
                pn = nextPage,
                pz = STOCK_PAGE_SIZE,
                fs = marketParams,
            ).collect { result ->
                when (result) {
                    is NetWorkResult.Success -> {
                        val stockList = result.data
                        val nextStocks = stockList?.diff.orEmpty()
                        val endState = if (
                            nextStocks.isEmpty() ||
                            stockListState.stockDataList.size + nextStocks.size >=
                                (stockList?.total ?: stockListState.totalStockCount)
                        ) {
                            FooterRefreshEndState.NONE_MORE_DATA
                        } else {
                            FooterRefreshEndState.SUCCESS
                        }
                        reduceStockListState(marketParams) {
                            val updatedStockDataList = stockDataList + nextStocks
                            val updatedTotalStockCount = stockList?.total ?: totalStockCount
                            copy(
                                stockDataList = updatedStockDataList.toObservableList(),
                                stockResult = result,
                                currentPage = if (nextStocks.isNotEmpty()) nextPage else currentPage,
                                totalStockCount = updatedTotalStockCount,
                                isLoadingNextPage = false,
                                loadNextPageEndState = endState,
                            )
                        }
                        intent.onFinished(endState)
                    }

                    is NetWorkResult.Error -> {
                        reduceStockListState(marketParams) {
                            copy(
                                stockResult = result,
                                isLoadingNextPage = false,
                                loadNextPageEndState = FooterRefreshEndState.FAILURE,
                            )
                        }
                        intent.onFinished(FooterRefreshEndState.FAILURE)
                    }

                    else -> reduceStockListState(marketParams) { copy(stockResult = result) }
                }
            }
        }
    }

    private fun reduce(transform: HomeScreenState.() -> HomeScreenState) {
        state = state.transform()
    }

    private fun reduceStockListState(
        marketParams: String,
        transform: StockListState.() -> StockListState,
    ) {
        reduce {
            val stockListState = stockListStateMap[marketParams] ?: StockListState()
            copy(stockListStateMap = stockListStateMap + (marketParams to stockListState.transform()))
        }
    }


    private companion object {
        const val STOCK_PAGE_SIZE = 30
    }
}
