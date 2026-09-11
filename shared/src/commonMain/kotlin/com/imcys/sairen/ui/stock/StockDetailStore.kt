package com.imcys.sairen.ui.stock

import com.imcys.sairen.core.chart.KLineLinePoint
import com.imcys.sairen.core.network.NetWorkResult
import com.imcys.sairen.core.network.emptyNetWorkResult
import com.imcys.sairen.core.network.model.SinaKLinePoint
import com.imcys.sairen.core.network.model.SinaMinlinePoint
import com.imcys.sairen.core.network.model.StockCompanyProfile
import com.imcys.sairen.core.network.model.StockDetail
import com.imcys.sairen.core.network.model.StockAiAnalysisJob
import com.imcys.sairen.core.network.model.Stock
import com.imcys.sairen.core.network.model.toEastMoneyF10Code
import com.imcys.sairen.core.network.model.toSinaSymbol
import com.imcys.sairen.core.network.model.toEastMoneyF10Code
import com.imcys.sairen.core.network.model.toSinaSymbol
import com.imcys.sairen.core.network.service.AppApiService
import com.imcys.sairen.model.StockChartTab
import com.imcys.sairen.model.stockChartTabList
import com.tencent.kuikly.core.coroutines.launch
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.ObservableThreadSafetyMode
import com.tencent.kuikly.core.reactive.handler.observable
import kotlinx.datetime.LocalDate
import kotlinx.coroutines.delay

internal sealed interface StockDetailIntent {
    data object Load : StockDetailIntent
    data class SelectChart(val tab: StockChartTab) : StockDetailIntent

    data object LoadAIInfo : StockDetailIntent

}

internal data class StockDetailState(
    val detailResult: NetWorkResult<StockDetail> = emptyNetWorkResult(),
    val selectedChartTab: StockChartTab = stockChartTabList.first(),
    val trendPoints: List<KLineLinePoint> = emptyList(),
    val averageTrendPoints: List<KLineLinePoint> = emptyList(),
    var stockCompanyInfo: NetWorkResult<StockCompanyProfile> = emptyNetWorkResult(),
    val aiAnalysisResult: NetWorkResult<StockAiAnalysisJob> = emptyNetWorkResult(),
)

internal class StockDetailStore(
    private val pager: Pager,
    private val stock: Stock,
) {
    var state by pager.observable(ObservableThreadSafetyMode.NONE, StockDetailState())
        private set

    fun dispatch(intent: StockDetailIntent) {
        when (intent) {
            StockDetailIntent.Load -> { loadDetail() }
            is StockDetailIntent.SelectChart -> loadChart(intent.tab)
            StockDetailIntent.LoadAIInfo -> { maybeRequestAiAnalysis() }
        }
    }

    private fun loadDetail() {
        pager.lifecycleScope.launch {
            AppApiService.getStockDetail(pager, stock).collect { result ->
                reduce { copy(detailResult = result) }
                if (result is NetWorkResult.Success) {
                    val f10Code = stock.toEastMoneyF10Code().orEmpty()
                    if (f10Code.isNotBlank()) {
                        AppApiService.getStockCompanyProfile(pager, f10Code).collect {
                            reduce { copy(stockCompanyInfo = it) }
                        }
                    }
                    loadChart(stockChartTabList.first())
                    maybeRequestAiAnalysis()
                }
            }
        }
    }

    private fun maybeRequestAiAnalysis() {
        if (state.detailResult !is NetWorkResult.Success) return
        pager.lifecycleScope.launch {
            AppApiService.createStockAiAnalysis(
                pager = pager,
                stock = stock,
            ).collect(::handleAiAnalysisResult)
        }
    }

    private fun handleAiAnalysisResult(result: NetWorkResult<StockAiAnalysisJob>) {
        reduce { copy(aiAnalysisResult = result) }
        val job = result.data ?: return
        if (result is NetWorkResult.Success && job.status == "analyzing") {
            pager.lifecycleScope.launch {
                delay(AI_ANALYSIS_POLL_INTERVAL_MS)
                AppApiService.createStockAiAnalysis(pager, stock).collect(::handleAiAnalysisResult)
            }
        }
    }

    private fun loadChart(tab: StockChartTab) {
        reduce { copy(selectedChartTab = tab) }
        pager.lifecycleScope.launch {
            val symbol = stock.toSinaSymbol()
            if (tab.scale == null) {
                AppApiService.getStockMinline(pager, symbol).collect { result ->
                    if (result is NetWorkResult.Success) {
                        val (points, averagePoints) = buildMinlinePoints(result.data.orEmpty())
                        state = state.copy(
                            trendPoints = points,
                            averageTrendPoints = averagePoints,
                        )
                    }
                }
            } else {
                AppApiService.getStockKLine(pager, symbol, tab.scale, tab.datalen)
                    .collect { result ->
                        if (result is NetWorkResult.Success) {
                            state = state.copy(
                                trendPoints = buildKLinePoints(result.data.orEmpty()),
                                averageTrendPoints = emptyList(),
                            )
                        }
                    }
            }
        }
    }

    private fun reduce(transform: StockDetailState.() -> StockDetailState) {
        state = state.transform()
    }

    private fun buildMinlinePoints(
        minuteData: List<SinaMinlinePoint>,
    ): Pair<List<KLineLinePoint>, List<KLineLinePoint>> {
        val points = ArrayList<KLineLinePoint>(minuteData.size)
        val averagePoints = ArrayList<KLineLinePoint>(minuteData.size)
        minuteData.forEach { item ->
            val close = item.p?.toDoubleOrNull() ?: return@forEach
            val time = item.m
            if (time < "09:30:00") return@forEach
            val chartIndex = points.size.toLong()
            val label = time.substring(0, 5)
            points.add(KLineLinePoint(chartIndex, close.toFloat(), label))
            val average = item.avgPrice?.toFloatOrNull() ?: close.toFloat()
            averagePoints.add(KLineLinePoint(chartIndex, average, label))
        }
        return points to averagePoints
    }

    private fun buildKLinePoints(klines: List<SinaKLinePoint>): List<KLineLinePoint> {
        val points = ArrayList<KLineLinePoint>(klines.size)
        klines.forEachIndexed { index, kline ->
            val close = kline.close?.toDoubleOrNull() ?: return@forEachIndexed
            val date = LocalDate.parse(kline.day.trim().substringBefore(' '))
            val label = "${date.year}-${date.monthNumber}-${date.dayOfMonth}"
            points.add(KLineLinePoint(index.toLong(), close.toFloat(), label))
        }
        return points
    }

    private companion object {
        const val AI_ANALYSIS_POLL_INTERVAL_MS = 2_000L
    }
}
