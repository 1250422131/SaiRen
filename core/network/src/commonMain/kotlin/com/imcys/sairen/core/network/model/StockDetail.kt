package com.imcys.sairen.core.network.model

import com.imcys.sairen.core.network.serializer.FlexStringSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 东财股票详情接口 `data` 字段的数据模型。
 */
@Serializable
data class StockDetail(
    /** 股票代码，例如：600519。 */
    @SerialName("f57")
    val code: String,

    /** 股票名称，例如：贵州茅台。 */
    @SerialName("f58")
    val name: String,

    /** 东方财富市场号；上证为 1，深证和北交所为 0。 */
    @SerialName("f107")
    @Serializable(with = FlexStringSerializer::class)
    val eastMoneyMarketCode: String,

    /** 最新价，单位为元。 */
    @SerialName("f43")
    @Serializable(with = FlexStringSerializer::class)
    val latestPrice: String,

    /** 最高价，单位为元。 */
    @SerialName("f44")
    @Serializable(with = FlexStringSerializer::class)
    val highestPrice: String,

    /** 最低价，单位为元。 */
    @SerialName("f45")
    @Serializable(with = FlexStringSerializer::class)
    val lowestPrice: String,

    /** 今开价，单位为元。 */
    @SerialName("f46")
    @Serializable(with = FlexStringSerializer::class)
    val openingPrice: String,

    /** 成交量，通常以手为单位。 */
    @SerialName("f47")
    @Serializable(with = FlexStringSerializer::class)
    val volume: String,

    /** 成交额，通常以元为单位。 */
    @SerialName("f48")
    @Serializable(with = FlexStringSerializer::class)
    val amount: String,

    /** 昨收价，单位为元。 */
    @SerialName("f60")
    @Serializable(with = FlexStringSerializer::class)
    val previousClosePrice: String,

    /** 总市值，通常以元为单位。 */
    @SerialName("f116")
    @Serializable(with = FlexStringSerializer::class)
    val totalMarketValue: String,

    /** 流通市值，通常以元为单位。 */
    @SerialName("f117")
    @Serializable(with = FlexStringSerializer::class)
    val circulatingMarketValue: String,

    /** 换手率，单位为百分比。 */
    @SerialName("f168")
    @Serializable(with = FlexStringSerializer::class)
    val turnoverRate: String,

    /** 涨跌额，单位为元。 */
    @SerialName("f169")
    @Serializable(with = FlexStringSerializer::class)
    val changeAmount: String,

    /** 涨跌幅，单位为百分比。 */
    @SerialName("f170")
    @Serializable(with = FlexStringSerializer::class)
    val changePercent: String,
) : SRModel {
    /** 根据东方财富市场号及证券代码识别的交易所市场。 */
    val market: StockMarket?
        get() = StockMarket.fromEastMoneyMarketCode(eastMoneyMarketCode, code)

    /** 股票所属的业务市场分类，例如 A 股、港股或美股。 */
    val marketCategory: StockCategory?
        get() = market?.category(code)
}
