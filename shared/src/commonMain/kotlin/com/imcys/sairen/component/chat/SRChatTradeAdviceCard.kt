package com.imcys.sairen.component.chat

import com.imcys.sairen.base.BasePager
import com.imcys.sairen.component.SRCard
import com.imcys.sairen.core.network.model.ChatTradeTiming
import com.imcys.sairen.core.network.model.ChatTradeTimingCard
import com.imcys.sairen.theme.SRRadius
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

/** 聊天中的点位建议；价格区间和依据由后端分析提供，内容高度自适应。 */
internal fun ViewContainer<*, *>.SRChatTradeAdviceCard(card: ChatTradeTimingCard) {
    val colors = (getPager() as BasePager).SRThemeColor
    SRCard {
        attr {
            alignSelfStretch()
            marginBottom(12f)
            padding(16f)
            backgroundColor(Color(colors.stockInfoCard))
        }
        Text {
            attr {
                text("买入卖出点位建议")
                fontSize(17f)
                fontWeightBold()
                color(colors.primaryText)
            }
        }
        Text {
            attr {
                text(listOf(card.stock.name, card.stock.code, card.stock.marketName, card.stock.currency)
                    .filter { it.isNotBlank() }.joinToString(" · "))
                marginTop(5f)
                fontSize(12f)
                lines(Int.MAX_VALUE)
                color(colors.secondaryText)
            }
        }
        AdvicePriceSection("买入观察区间", card.buy, colors.rise, colors.riseBackground)
        AdvicePriceSection("卖出 / 减仓区间", card.sell, colors.fall, colors.fallBackground)
        if (card.risk.isNotBlank()) {
            View {
                attr {
                    marginTop(12f)
                    padding(12f)
                    borderRadius(SRRadius.MEDIUM)
                    backgroundColor(Color(colors.stockTagBackground))
                }
                Text { attr { text("风险与失效条件"); fontSize(12f); fontWeightBold(); color(colors.primaryText) } }
                Text { attr { text(card.risk); marginTop(5f); fontSize(13f); lines(Int.MAX_VALUE); color(colors.secondaryText) } }
            }
        }
        Text {
            attr {
                text(card.disclaimer.ifBlank { "仅供信息参考，不构成投资建议。" })
                marginTop(10f)
                fontSize(11f)
                lines(Int.MAX_VALUE)
                color(colors.secondaryText)
            }
        }
    }
}

private fun ViewContainer<*, *>.AdvicePriceSection(
    title: String,
    advice: ChatTradeTiming,
    accent: Long,
    background: Long,
) {
    val colors = (getPager() as BasePager).SRThemeColor
    View {
        attr {
            marginTop(12f)
            padding(14f)
            borderRadius(SRRadius.MEDIUM)
            backgroundColor(Color(background))
        }
        Text { attr { text(title); fontSize(12f); fontWeightMedium(); color(accent) } }
        Text {
            attr {
                text(advice.range.ifBlank { "暂不提供点位" })
                marginTop(6f)
                fontSize(20f)
                fontWeightBold()
                lines(Int.MAX_VALUE)
                color(accent)
            }
        }
        Text { attr { text(advice.rationale); marginTop(7f); fontSize(13f); lines(Int.MAX_VALUE); color(colors.primaryText) } }
    }
}
