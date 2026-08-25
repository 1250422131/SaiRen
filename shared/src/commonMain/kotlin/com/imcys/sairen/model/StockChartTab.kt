package com.imcys.sairen.model

import kotlinx.serialization.Serializable

/**
 * 图表 Tab（参照东方财富/自选股的周期选择惯例：范围由近到远，粒度随范围自动切换）。
 * 数据源为新浪财经：
 * - 分时：`CN_MinlineService.getMinlineData`（当日 1 分钟线，[scale] 为 null）；
 * - 其余：`CN_MarketData.getKLineData`，[scale] 单位为分钟——
 *   5=5 分钟线（五日）、240=日K、1200=周K（按周聚合）、7200=月K（按自然月聚合）。
 *   注意：该接口为不复权数据；周K/月K 的日期取该周期最后一个交易日。
 *
 * [datalen] 为请求根数（A股一年约 244 个交易日、约 52 周）：
 * 近1月≈22、近3月≈66、近6月≈122、近1年≈244 根日K；近3年=156 根周K；月K=120 根（约10年）。
 */
@Serializable
data class StockChartTab(
    val title: String,
    /** K 线周期（分钟）：null=分时（走 minline 接口），5/240/1200/7200=五日/日/周/月K */
    val scale: Int?,
    /** 请求根数（datalen）；分时接口无此参数 */
    val datalen: Int = 0,
    /** 分钟级数据（分时/五日）：时间标签显示 HH:mm；日/周/月K 显示 MM-dd */
    val intraday: Boolean = false,
)

val stockChartTabList = listOf(
    StockChartTab("分时", scale = null, datalen = 0, intraday = true),
    StockChartTab("五日", scale = 5, datalen = 240, intraday = true),
    StockChartTab("月K", scale = 7200, datalen = 120),
    StockChartTab("近1月", scale = 240, datalen = 22),
    StockChartTab("近3月", scale = 240, datalen = 66),
    StockChartTab("近6月", scale = 240, datalen = 122),
    StockChartTab("近1年", scale = 240, datalen = 244),
    StockChartTab("近2年", scale = 1200, datalen = 104),
    StockChartTab("近3年", scale = 1200, datalen = 156),
)
