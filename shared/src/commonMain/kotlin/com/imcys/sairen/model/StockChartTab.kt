package com.imcys.sairen.model

enum class StockChartApiType {
    TRENDS,
    KLINE,
}

data class StockChartTab(
    val title: String,
    val apiType: StockChartApiType,
    val period: Int,
)

val stockChartTabList = listOf(
    StockChartTab("分时", StockChartApiType.TRENDS, 1),
    StockChartTab("五日", StockChartApiType.TRENDS, 5),
    StockChartTab("日K", StockChartApiType.KLINE, 101),
    StockChartTab("周K", StockChartApiType.KLINE, 102),
    StockChartTab("月K", StockChartApiType.KLINE, 103),
)
