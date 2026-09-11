package com.imcys.sairen.core.network.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

@Serializable
data class ChatMessage(
    val id: Long,
    val requestId: String,
    val role: String,
    val contents: List<ChatMessageContent> = emptyList(),
    val status: String,
    val createdAt: String = "",
    val updatedAt: String = "",
) : SRModel

@Serializable
data class ChatMessageContent(
    val type: String,
    val data: JsonElement = JsonNull,
)

@Serializable
data class ChatHistoryPage(
    val messages: List<ChatMessage> = emptyList(),
    val hasMore: Boolean = false,
    val nextBeforeId: Long? = null,
) : SRModel

@Serializable
data class SendChatMessageRequest(
    val content: String,
    val requestId: String,
)

@Serializable
data class SendChatMessageResult(
    val messages: List<ChatMessage> = emptyList(),
    val repeated: Boolean = false,
) : SRModel

@Serializable
data class ChatStockBasic(
    val code: String = "",
    val marketCode: String = "",
    val marketName: String = "",
    val currency: String = "",
    val name: String = "",
    val latestPrice: String = "",
    val highestPrice: String = "",
    val lowestPrice: String = "",
    val openingPrice: String = "",
    val previousClosePrice: String = "",
    val volume: String = "",
    val amount: String = "",
    val turnoverRate: String = "",
    val changeAmount: String = "",
    val changePercent: String = "",
    val totalMarketValue: String = "",
    val circulatingMarketValue: String = "",
)

@Serializable
data class ChatStockKLineCard(
    val stock: ChatStockBasic = ChatStockBasic(),
    val kLines: List<SinaKLinePoint> = emptyList(),
)

@Serializable
data class ChatStockCompanyCard(
    val stock: ChatStockBasic = ChatStockBasic(),
    val company: ChatStockCompany = ChatStockCompany(),
)

@Serializable
data class ChatStockCompany(
    val companyName: String = "",
    val industry: String = "",
    val exchange: String = "",
    val summary: String = "",
    val businessScope: String = "",
    val chairman: String = "",
    val legalRepresentative: String = "",
    val generalManager: String = "",
    val website: String = "",
)

@Serializable
data class ChatTradeTimingCard(
    val stock: ChatStockBasic = ChatStockBasic(),
    val buy: ChatTradeTiming = ChatTradeTiming(),
    val sell: ChatTradeTiming = ChatTradeTiming(),
    val risk: String = "",
    val disclaimer: String = "",
)

@Serializable
data class ChatTradeTiming(
    val range: String = "",
    val rationale: String = "",
)
