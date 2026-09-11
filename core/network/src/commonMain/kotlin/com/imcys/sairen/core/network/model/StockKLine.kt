package com.imcys.sairen.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 新浪 `CN_MarketData.getKLineData` 返回的单根 K 线。
 * 字段均为字符串数字；该接口为不复权数据。
 */
@Serializable
data class SinaKLinePoint(
    /** 时间：分钟级为 "yyyy-MM-dd HH:mm:ss"；日/周/月K 为 "yyyy-MM-dd"（周/月K 取该周期最后一个交易日） */
    val day: String,
    /** 开盘价 */
    val open: String? = null,
    /** 最高价 */
    val high: String? = null,
    /** 最低价 */
    val low: String? = null,
    /** 收盘价 */
    val close: String? = null,
    /** 成交量 */
    val volume: String? = null,
):SRModel

/**
 * 新浪 `CN_MinlineService.getMinlineData` 返回的当日分时点（1 分钟线）。
 */
@Serializable
data class SinaMinlinePoint(
    /** 时间 "HH:mm:ss"（当日，含 09:25 集合竞价点） */
    val m: String,
    /** 成交量 */
    val v: String? = null,
    /** 成交价 */
    val p: String? = null,
    /** 均价 */
    @SerialName("avg_p")
    val avgPrice: String? = null,
):SRModel
