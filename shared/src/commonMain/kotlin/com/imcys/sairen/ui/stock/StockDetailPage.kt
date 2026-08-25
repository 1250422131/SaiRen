package com.imcys.sairen.ui.stock

import com.imcys.sairen.base.BasePager
import com.imcys.sairen.component.SRNavigationBar
import com.imcys.sairen.component.screen.stock.OHLCVInfo
import com.imcys.sairen.component.screen.stock.StockDetailTopInfo
import com.imcys.sairen.component.screen.stock.StockKChart
import com.imcys.sairen.core.chart.KLineLinePoint
import com.imcys.sairen.core.network.NetWorkResult
import com.imcys.sairen.core.network.emptyNetWorkResult
import com.imcys.sairen.core.network.model.SinaKLinePoint
import com.imcys.sairen.core.network.model.SinaMinlinePoint
import com.imcys.sairen.core.network.model.Stock
import com.imcys.sairen.core.network.model.StockDetail
import com.imcys.sairen.core.network.model.toSinaSymbol
import com.imcys.sairen.core.network.service.AppApiService
import com.imcys.sairen.core.network.toModel
import com.imcys.sairen.model.StockChartTab
import com.imcys.sairen.model.stockChartTabList
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.coroutines.launch
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.List
import com.tencent.kuikly.core.views.View
import kotlinx.datetime.LocalDate

@Page("stock_detail")
internal class StockDetailPage : BasePager() {

    private companion object {
        const val TAG = "StockDetailPage"
    }

    private lateinit var baseData: Stock
    private var detailData by observable<NetWorkResult<StockDetail>>(emptyNetWorkResult())
    private var trendPoints by observable<List<KLineLinePoint>>(emptyList())
    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(Color(ctx.SRThemeColor.pageBackground))
                }

                SRNavigationBar {
                    attr {
                        enabledBack = true
                    }
                }

                // 内容
                List {
                    attr {
                        flex(1f)
                        margin(left = 20f, right = 20f, top = 10f)
                    }
                    attr {
                        flex(1f)
                    }
                    StockDetailTopInfo {
                        attr {
                            details = { ctx.detailData }
                        }
                    }
                    OHLCVInfo {
                        attr {
                            marginTop(30f)
                            details = { ctx.detailData }
                        }
                    }
                    vif({ ctx.detailData is NetWorkResult.Success }) {
                        StockKChart {
                            attr {
                                marginTop(15f)
                                trends = { ctx.trendPoints }
                            }
                            event {
                                onTabClick = { tab -> ctx.lifecycleScope.launch { ctx.loadChart(tab) } }
                            }
                        }
                    }


                }


            }
        }
    }

    override fun created() {
        super.created()
        baseData = pagerData.params.toModel<Stock>()
        loadDetails()
    }


    private fun loadDetails() {
        lifecycleScope.launch {
            AppApiService.getStockDetail(this@StockDetailPage, baseData).collect {
                detailData = it
            }
            loadChart(stockChartTabList.first())
        }
    }

    private suspend fun loadChart(tab: StockChartTab) {
        val symbol = baseData.toSinaSymbol()
        if (tab.scale == null) {
            AppApiService.getStockMinline(this, symbol).collect { result ->
                if (result is NetWorkResult.Success) {
                    val points = buildMinlinePoints(result.data.orEmpty())
                    updateTrendPoints(points)
                }
            }
        } else {
            AppApiService.getStockKLine(this, symbol, tab.scale, datalen = tab.datalen).collect { result ->
                if (result is NetWorkResult.Success) {
                    val points = buildKLinePoints(result.data.orEmpty(), tab)
                    updateTrendPoints(points)
                }
            }
        }
    }

    private fun updateTrendPoints(points: List<KLineLinePoint>) {
        trendPoints = points
    }

    /**
     * 当日分时点（新浪 minline，时间为当日 "HH:mm:ss"）：过滤 09:25 集合竞价点，标签 HH:mm。
     */
    private fun buildMinlinePoints(minuteData: List<SinaMinlinePoint>): List<KLineLinePoint> {
        val points = ArrayList<KLineLinePoint>(minuteData.size)
        minuteData.forEachIndexed { index, item ->
            val close = item.p?.toDoubleOrNull() ?: return@forEachIndexed
            val time = item.m
            if (time < "09:30:00") return@forEachIndexed
            points.add(
                KLineLinePoint(
                    index.toLong(),
                    close.toFloat(),
                    time.substring(0, 5)
                )
            )
        }
        return points
    }

    /**
     * 原始 K 线点转图表点：
     * - 五日（5 分钟线，"yyyy-MM-dd HH:mm:ss"）：标签 HH:mm；
     * - 日K/周K/月K：标签日期。
     */
    private fun buildKLinePoints(klines: List<SinaKLinePoint>, tab: StockChartTab): List<KLineLinePoint> {
        val points = ArrayList<KLineLinePoint>(klines.size)
        klines.forEachIndexed { index, kline ->
            val close = kline.close?.toDoubleOrNull() ?: return@forEachIndexed
            val time = kline.day.trim()
            if (tab.intraday) {
                val date = LocalDate.parse(time.substringBefore(' '))
                val label = "${date.year}-${date.monthNumber}-${date.dayOfMonth}"
                points.add(KLineLinePoint(index.toLong(), close.toFloat(), label))
            } else {
                val date = LocalDate.parse(time.substringBefore(' '))
                val label = "${date.year}-${date.monthNumber}-${date.dayOfMonth}"
                points.add(KLineLinePoint(index.toLong(), close.toFloat(), label))
            }
        }
        return points
    }

}
