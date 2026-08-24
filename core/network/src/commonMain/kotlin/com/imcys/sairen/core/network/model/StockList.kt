package com.imcys.sairen.core.network.model

import com.imcys.sairen.core.network.json
import com.imcys.sairen.core.network.serializer.FlexStringSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

@Serializable
data class StockList(
    /** 当前查询条件下返回的股票列表。 */
    val diff: List<Stock>,
    /** 当前查询条件下的股票总数。 */
    val total: Int
): SRModel {
}
@Serializable
data class Stock(
    /** 股票代码，例如：600519。 */
    @SerialName("f12")
    val code: String,

    /** 东方财富市场号，用于生成详情接口的 `secid`。 */
    @SerialName("f13")
    @Serializable(with = FlexStringSerializer::class)
    val eastMoneyMarketCode: String,

    /** 股票名称，例如：贵州茅台。 */
    @SerialName("f14")
    val name: String,

    /** 最新成交价；停牌或无数据时接口可能返回非数字值。 */
    @SerialName("f2")
    @Serializable(with = FlexStringSerializer::class)
    val latestPrice: String,

    /** 涨跌幅，单位为百分比；正数表示上涨，负数表示下跌。 */
    @SerialName("f3")
    @Serializable(with = FlexStringSerializer::class)
    val changePercent: String,

    /** 涨跌额，即最新价相对昨收价的变动金额。 */
    @SerialName("f4")
    @Serializable(with = FlexStringSerializer::class)
    val changeAmount: String,

    /** 成交量，通常以手为单位，具体取决于行情接口。 */
    @SerialName("f5")
    @Serializable(with = FlexStringSerializer::class)
    val volume: String,

    /** 成交额，单位通常为元。 */
    @SerialName("f6")
    @Serializable(with = FlexStringSerializer::class)
    val amount: String,
) : SRModel {
    /** 根据东方财富市场号及证券代码识别的交易所市场。 */
    val market: StockMarket?
        get() = StockMarket.fromEastMoneyMarketCode(eastMoneyMarketCode, code)

    /** 股票所属的业务市场分类，例如 A 股、港股或美股。 */
    val marketCategory: StockCategory?
        get() = market?.category(code)
}

