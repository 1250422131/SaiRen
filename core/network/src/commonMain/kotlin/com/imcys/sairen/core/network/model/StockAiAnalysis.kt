package com.imcys.sairen.core.network.model

import kotlinx.serialization.Serializable

@Serializable
/** 创建股票 AI 分析任务的请求体。
 *
 * 仅提交证券标识，行情、公司资料与 K 线均由服务端自行获取，避免客户端篡改分析依据。
 */
data class StockAiAnalysisRequest(
    /** 证券代码，例如 `600519`。 */
    val code: String,
    /** 东方财富市场号：`0` 为深市/北交所，`1` 为沪市。 */
    val marketCode: String,
)

@Serializable
/** AI 分析请求的服务端状态与结果。 */
data class StockAiAnalysisJob(
    /** 服务端生成的分析记录 ID。 */
    val id: String,
    /** 请求股票代码。 */
    val stockId: String,
    /** 任务状态：`analyzing`、`completed` 或 `failed`。 */
    val status: String,
    /** 是否命中一小时内的持久化结果。 */
    val cached: Boolean = false,
    /** 任务创建时间，ISO-8601 格式。 */
    val createdAt: String = "",
    /** 最后更新时间，ISO-8601 格式。 */
    val updatedAt: String = "",
    /** 完成时返回的结构化 AI 分析；生成中或失败时为 `null`。 */
    val analysis: StockAiAnalysisContent? = null,
    /** 失败原因；成功或生成中时为 `null`。 */
    val error: String? = null,
) : SRModel

@Serializable
/** AI 生成的、可直接映射到详情页卡片的分析内容。 */
data class StockAiAnalysisContent(
    /** 总体结论。 */
    val conclusion: StockAiConclusion,
    /** 买入观察区间及依据。 */
    val buySuggestion: StockAiSuggestion,
    /** 卖出观察区间及依据。 */
    val sellSuggestion: StockAiSuggestion,
    /** 趋势判断及依据。 */
    val trend: StockAiTrend,
    /** 风险等级与风险提醒。 */
    val risk: StockAiRisk,
    /** 行情信号解读，服务端保证至少三项。 */
    val signals: List<StockAiSignal>,
    /** 行情总结。 */
    val summary: String,
    /** 投资风险免责声明。 */
    val disclaimer: String,
) : SRModel

@Serializable
/** 总体结论的标题与说明。 */
data class StockAiConclusion(
    /** 结论标题。 */
    val headline: String,
    /** 结论说明。 */
    val description: String,
)

@Serializable
/** 买入或卖出观察区间及其分析依据。 */
data class StockAiSuggestion(
    /** 观察区间文本。 */
    val range: String,
    /** 给出该区间的理由。 */
    val rationale: String,
)

@Serializable
/** 趋势标签与判断依据。 */
data class StockAiTrend(
    /** 趋势标签，例如“震荡偏强”。 */
    val label: String,
    /** 趋势判断依据。 */
    val rationale: String,
)

@Serializable
/** 风险等级与对应提醒。 */
data class StockAiRisk(
    /** 风险等级：低、中低、中等、中高或高。 */
    val level: String,
    /** 风险提醒内容。 */
    val warning: String,
)

@Serializable
/** 单条行情信号及其解释。 */
data class StockAiSignal(
    /** 信号名称。 */
    val title: String,
    /** 信号说明。 */
    val description: String,
)
