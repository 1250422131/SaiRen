package com.imcys.sairen.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 东方财富搜索建议接口返回的股票候选项。 */
@Serializable
data class StockSuggest(
    @SerialName("Code")
    val code: String = "",
    @SerialName("Name")
    val name: String = "",
    @SerialName("PinYin")
    val pinYin: String = "",
    @SerialName("ID")
    val id: String = "",
    @SerialName("JYS")
    val jys: String = "",
    @SerialName("Classify")
    val classify: String = "",
    @SerialName("MarketType")
    val marketType: String = "",
    @SerialName("SecurityTypeName")
    val securityTypeName: String = "",
    @SerialName("SecurityType")
    val securityType: String = "",
    @SerialName("MktNum")
    val marketNumber: String = "",
    @SerialName("TypeUS")
    val typeUs: String = "",
    @SerialName("QuoteID")
    val quoteId: String = "",
    @SerialName("UnifiedCode")
    val unifiedCode: String = "",
    @SerialName("InnerCode")
    val innerCode: String = "",
) : SRModel

/** 东方财富搜索建议接口的 `QuotationCodeTable` 数据。 */
@Serializable
data class StockSuggestTable(
    @SerialName("Data")
    val data: List<StockSuggest> = emptyList(),
    @SerialName("Status")
    val status: Int = 0,
    @SerialName("Message")
    val message: String = "",
    @SerialName("TotalCount")
    val totalCount: Int = 0,
    @SerialName("BizCode")
    val bizCode: String = "",
    @SerialName("BizMsg")
    val bizMessage: String = "",
) : SRModel
