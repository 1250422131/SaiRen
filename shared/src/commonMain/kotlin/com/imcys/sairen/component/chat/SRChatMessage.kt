package com.imcys.sairen.component.chat

import com.imcys.sairen.base.BasePager
import com.imcys.sairen.core.network.json
import com.imcys.sairen.core.network.model.ChatMessage
import com.imcys.sairen.core.network.model.ChatMessageContent
import com.imcys.sairen.core.network.model.ChatStockBasic
import com.imcys.sairen.core.network.model.ChatStockCompanyCard
import com.imcys.sairen.core.network.model.ChatStockKLineCard
import com.imcys.sairen.core.network.model.ChatTradeTimingCard
import com.imcys.sairen.theme.SRRadius
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.tencent.kuikly.core.views.layout.Column
import com.tencent.kuiklybase.KuiklyMarkdown
import com.tencent.kuiklybase.config.MarkdownConfig
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement

internal class SRChatMessageView : ComposeView<SRChatMessageViewAttr, ComposeEvent>() {
    override fun createEvent() = ComposeEvent()
    override fun createAttr() = SRChatMessageViewAttr()

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            Column {
                attr { margin(left = 16f, right = 16f, top = 10f, bottom = 10f) }
                if (ctx.attr.message.role == "user") {
                    UserMessageBubble(ctx.attr.message)
                } else {
                    AiMessageContent(ctx.attr.message)
                }
            }
        }
    }
}

private fun ViewContainer<*, *>.UserMessageBubble(message: ChatMessage) {
        val colors = (getPager() as BasePager).SRThemeColor
        View {
            attr {
                alignSelfFlexEnd()
                maxWidth(getPager().pageData.pageViewWidth * 0.76f)
                padding(left = 16f, right = 16f, top = 12f, bottom = 12f)
                backgroundColor(Color(colors.primary))
                borderRadius(topLeft = SRRadius.LARGE, topRight = 0f, bottomLeft = SRRadius.LARGE, bottomRight = SRRadius.LARGE)
            }
            Text {
                attr {
                    text(message.textContent())
                    color(Color.WHITE)
                    fontSize(15f)
                    lines(Int.MAX_VALUE)
                }
            }
        }
    }

private fun ViewContainer<*, *>.AiMessageContent(message: ChatMessage) {
        val colors = (getPager() as BasePager).SRThemeColor
        Text {
            attr {
                text("塞壬")
                color(colors.secondaryText)
                fontSize(13f)
                margin(left = 2f, bottom = 8f)
            }
        }
        message.contents.forEach { content ->
            when (content.type) {
                "text" -> content.stringData()?.let { AiTextBubble(it) }
                "markdown" -> content.stringData()?.let { AiMarkdownBubble(it) }
                "stock_basic" -> decodeData<ChatStockBasic>(content.data)?.let { ChatStockBasicCard(it) }
                "stock_kline" -> decodeData<ChatStockKLineCard>(content.data)?.let { ChatStockKLineCard(it) }
                "stock_trade_timing" -> decodeData<ChatTradeTimingCard>(content.data)?.let { SRChatTradeAdviceCard(it) }
                "stock_company" -> decodeData<ChatStockCompanyCard>(content.data)?.let { ChatStockCompanyCard(it) }
            }
        }
    }

private fun ViewContainer<*, *>.AiTextBubble(content: String) {
        val colors = (getPager() as BasePager).SRThemeColor
        View {
            attr {
                alignSelfFlexStart()
                maxWidth(getPager().pageData.pageViewWidth - 64f)
                margin(right = 16f, bottom = 10f)
                padding(left = 16f, right = 16f, top = 12f, bottom = 12f)
                backgroundColor(Color(colors.stockTagBackground))
                borderRadius(topLeft = 0f, topRight = SRRadius.LARGE, bottomLeft = SRRadius.LARGE, bottomRight = SRRadius.LARGE)
            }
            Text {
                attr {
                    text(content)
                    color(colors.primaryText)
                    fontSize(15f)
                    lines(Int.MAX_VALUE)
                }
            }
        }
    }

private fun ViewContainer<*, *>.AiMarkdownBubble(content: String) {
        val colors = (getPager() as BasePager).SRThemeColor
        View {
            attr {
                alignSelfFlexStart()
                maxWidth(getPager().pageData.pageViewWidth - 64f)
                margin(right = 16f, bottom = 10f)
                padding(left = 16f, right = 16f, top = 12f, bottom = 12f)
                backgroundColor(Color(colors.stockTagBackground))
                borderRadius(topLeft = 0f, topRight = SRRadius.LARGE, bottomLeft = SRRadius.LARGE, bottomRight = SRRadius.LARGE)
            }
            KuiklyMarkdown(content = content, config = MarkdownConfig.Default)
        }
    }

internal class SRChatMessageViewAttr : ComposeAttr() {
    lateinit var message: ChatMessage
}

internal fun ViewContainer<*, *>.SRChatMessage(init: SRChatMessageView.() -> Unit) {
    addChild(SRChatMessageView(), init)
}

internal fun ViewContainer<*, *>.SRChatThinkingMessage() {
    val colors = (getPager() as BasePager).SRThemeColor
    View {
        attr {
            alignSelfFlexStart()
            margin(left = 16f, right = 16f, top = 8f, bottom = 8f)
            padding(left = 16f, right = 16f, top = 12f, bottom = 12f)
            backgroundColor(Color(colors.stockTagBackground))
            borderRadius(topLeft = 0f, topRight = SRRadius.LARGE, bottomLeft = SRRadius.LARGE, bottomRight = SRRadius.LARGE)
        }
        Text {
            attr {
                text("正在思考...")
                color(colors.secondaryText)
                fontSize(14f)
            }
        }
    }
}

private fun ChatMessage.textContent(): String = contents.mapNotNull { it.stringData() }.joinToString("\n")

private fun ChatMessageContent.stringData(): String? = (data as? JsonPrimitive)?.content

private inline fun <reified T> decodeData(element: JsonElement): T? =
    runCatching { json.decodeFromJsonElement<T>(element) }.getOrNull()
