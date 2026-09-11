package com.imcys.sairen.component.screen.stock

import com.imcys.sairen.component.SRButton
import com.imcys.sairen.component.SRCard
import com.imcys.sairen.core.common.ext.toPageAssets
import com.imcys.sairen.core.network.model.StockAiAnalysisContent
import com.imcys.sairen.theme.SRRadius
import com.imcys.sairen.theme.SRThemeColor
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.directives.velseif
import com.tencent.kuikly.core.directives.velse
import com.tencent.kuikly.core.views.Image
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.tencent.kuikly.core.views.layout.Column
import com.tencent.kuikly.core.views.layout.Row

internal class StockAIProposalView :
    ComposeView<StockAIProposalViewAttr, StockAIProposalViewEvent>() {

    override fun createEvent(): StockAIProposalViewEvent {
        return StockAIProposalViewEvent()
    }

    override fun createAttr(): StockAIProposalViewAttr {
        return StockAIProposalViewAttr()
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            SRCard {
                vif({ ctx.attr.status() == StockAIAnalysisStatus.ANALYZING }) {
                    Column {
                        attr {
                            padding(top = 28f, left = 18f, right = 18f, bottom = 28f)
                            alignItemsCenter()
                        }
                        Image {
                            attr {
                                width(88f)
                                height(88f * 662f / 904f)
                                src("ai_message.png".toPageAssets())
                                tintColor(Color(ctx.SRThemeColor.primary))
                            }
                        }
                        Text {
                            attr {
                                marginTop(12f)
                                text("正在聆听塞壬之声")
                                fontSize(17f)
                                fontWeightBold()
                                color(ctx.SRThemeColor.primaryText)
                            }
                        }
                        Text {
                            attr {
                                marginTop(7f)
                                text("AI 正在读取行情、K线与企业信息，请稍候。")
                                fontSize(13f)
                                lineHeight(19f)
                                color(ctx.SRThemeColor.secondaryText)
                            }
                        }
                    }
                }
                velseif({ ctx.attr.status() == StockAIAnalysisStatus.FAILED }) {
                    Column {
                        attr {
                            padding(top = 24f, left = 18f, right = 18f, bottom = 24f)
                            alignItemsCenter()
                        }
                        Text {
                            attr {
                                text("AI 分析暂不可用")
                                fontSize(16f)
                                fontWeightBold()
                                color(ctx.SRThemeColor.primaryText)
                            }
                        }
                        Text {
                            attr {
                                marginTop(7f)
                                text(ctx.attr.errorMessage().ifBlank { "请稍后重试。" })
                                fontSize(13f)
                                lineHeight(19f)
                                color(ctx.SRThemeColor.secondaryText)
                            }
                        }

                        SRButton {
                            attr {
                                marginTop(10f)
                            }
                            event {
                                click {
                                    ctx.event.onRetry()
                                }
                            }
                        }


                    }
                }
                velse {
                    Column {
                        attr {
                            padding(top = 18f, left = 18f, right = 18f, bottom = 18f)
                        }

                        Row {
                            attr {
                                alignItemsCenter()
                            }

                            Image {
                                attr {
                                    width(42f)
                                    height(42f * 662f / 904f)
                                    src("ai_message.png".toPageAssets())
                                    tintColor(Color(ctx.SRThemeColor.primary))
                                }
                            }

                            Column {
                                attr {
                                    flex(1f)
                                    marginLeft(10f)
                                }
                                Text {
                                    attr {
                                        text("AI分析与解读")
                                        fontSize(16f)
                                        fontWeightBold()
                                        color(ctx.SRThemeColor.primaryText)
                                    }
                                }
                                Text {
                                    attr {
                                        marginTop(3f)
                                        text("行情、K线与成交数据综合研判")
                                        fontSize(12f)
                                        color(ctx.SRThemeColor.secondaryText)
                                    }
                                }
                            }

                            Column {
                                attr {
                                    backgroundColor(ctx.SRThemeColor.primaryContainer)
                                    borderRadius(SRRadius.MEDIUM)
                                    padding(top = 4f, bottom = 4f, left = 8f, right = 8f)
                                }
                                Text {
                                    attr {
                                        text("AI 解读")
                                        fontSize(12f)
                                        fontWeightBold()
                                        color(ctx.SRThemeColor.primary)
                                    }
                                }
                            }
                        }

                        View {
                            attr {
                                marginTop(14f)
                                height(1f)
                                backgroundColor(ctx.SRThemeColor.divider)
                            }
                        }

                        Row {
                            attr {
                                marginTop(14f)
                                alignItemsCenter()
                                justifyContentFlexStart()
                            }
                            View {
                                attr {
                                    width(3f)
                                    height(12f)
                                    marginRight(5f)
                                    borderRadius(3f)
                                    backgroundColor(ctx.SRThemeColor.primary)
                                }
                            }
                            Text {
                                attr {
                                    text("分析结论")
                                    fontSize(12f)
                                    fontWeightMedium()
                                    color(ctx.SRThemeColor.primary)
                                }
                            }
                        }
                        Text {
                            attr {
                                marginTop(5f)
                                text(ctx.attr.analysis()?.conclusion?.headline.orEmpty())
                                fontSize(17f)
                                fontWeightBold()
                                color(ctx.SRThemeColor.primaryText)
                            }
                        }
                        Text {
                            attr {
                                marginTop(7f)
                                text(ctx.attr.analysis()?.conclusion?.description.orEmpty())
                                fontSize(13f)
                                lineHeight(19f)
                                color(ctx.SRThemeColor.secondaryText)
                            }
                        }

                        Row {
                            attr {
                                marginTop(16f)
                            }

                            Column {
                                attr {
                                    flex(1f)
                                    backgroundColor(ctx.SRThemeColor.primaryContainer)
                                    borderRadius(SRRadius.MEDIUM)
                                    padding(top = 11f, bottom = 11f, left = 12f, right = 12f)
                                }
                                Text {
                                    attr {
                                        text("买入建议位")
                                        fontSize(12f)
                                        color(ctx.SRThemeColor.secondaryText)
                                    }
                                }
                                Text {
                                    attr {
                                        marginTop(5f)
                                        text(ctx.attr.analysis()?.buySuggestion?.range.orEmpty())
                                        fontSize(15f)
                                        fontWeightBold()
                                        color(ctx.SRThemeColor.primary)
                                    }
                                }
                            }

                            Column {
                                attr {
                                    flex(1f)
                                    marginLeft(10f)
                                    backgroundColor(ctx.SRThemeColor.primaryContainer)
                                    borderRadius(SRRadius.MEDIUM)
                                    padding(top = 11f, bottom = 11f, left = 12f, right = 12f)
                                }
                                Text {
                                    attr {
                                        text("卖出建议位")
                                        fontSize(12f)
                                        color(ctx.SRThemeColor.secondaryText)
                                    }
                                }
                                Text {
                                    attr {
                                        marginTop(5f)
                                        text(ctx.attr.analysis()?.sellSuggestion?.range.orEmpty())
                                        fontSize(15f)
                                        fontWeightBold()
                                        color(ctx.SRThemeColor.primary)
                                    }
                                }
                            }
                        }
                        Row {
                            attr {
                                marginTop(18f)
                                alignItemsCenter()
                                justifyContentFlexStart()
                            }
                            View {
                                attr {
                                    width(3f)
                                    height(14f)
                                    marginRight(5f)
                                    borderRadius(3f)
                                    backgroundColor(ctx.SRThemeColor.primary)
                                }
                            }

                            Text {
                                attr {
                                    text("趋势与风险")
                                    fontSize(14f)
                                    fontWeightBold()
                                    color(ctx.SRThemeColor.primaryText)
                                }
                            }
                        }



                        Row {
                            attr {
                                marginTop(9f)
                                alignItemsCenter()
                            }

                            Column {
                                attr {
                                    backgroundColor(ctx.SRThemeColor.primaryContainer)
                                    borderRadius(SRRadius.MEDIUM)
                                    padding(top = 5f, bottom = 5f, left = 8f, right = 8f)
                                }
                                Text {
                                    attr {
                                        text("趋势判断 · ${ctx.attr.analysis()?.trend?.label.orEmpty()}")
                                        fontSize(12f)
                                        fontWeightMedium()
                                        color(ctx.SRThemeColor.primary)
                                    }
                                }
                            }

                            Column {
                                attr {
                                    marginLeft(8f)
                                    backgroundColor(ctx.riskColors().background)
                                    borderRadius(SRRadius.MEDIUM)
                                    padding(top = 5f, bottom = 5f, left = 8f, right = 8f)
                                }
                                Text {
                                    attr {
                                        text("风险等级 · ${ctx.attr.analysis()?.risk?.level.orEmpty()}")
                                        fontSize(12f)
                                        fontWeightMedium()
                                        color(ctx.riskColors().text)
                                    }
                                }
                            }
                        }

                        Column {
                            attr {
                                marginTop(10f)
                                backgroundColor(ctx.SRThemeColor.errorBackground)
                                borderRadius(SRRadius.MEDIUM)
                                padding(top = 10f, bottom = 10f, left = 12f, right = 12f)
                            }
                            Text {
                                attr {
                                    text("风险提醒")
                                    fontSize(12f)
                                    fontWeightBold()
                                    color(ctx.SRThemeColor.error)
                                }
                            }
                            Text {
                                attr {
                                    marginTop(4f)
                                    text(ctx.attr.analysis()?.risk?.warning.orEmpty())
                                    fontSize(13f)
                                    lineHeight(19f)
                                    color(ctx.SRThemeColor.secondaryText)
                                }
                            }
                        }
                        Row {
                            attr {
                                marginTop(18f)
                                alignItemsCenter()
                                justifyContentFlexStart()
                            }
                            View {
                                attr {
                                    width(3f)
                                    height(14f)
                                    marginRight(5f)
                                    borderRadius(3f)
                                    backgroundColor(ctx.SRThemeColor.primary)
                                }
                            }
                            Text {
                                attr {
                                    text("信号解读")
                                    fontSize(14f)
                                    fontWeightBold()
                                    color(ctx.SRThemeColor.primaryText)
                                }
                            }
                        }

                        Row {
                            attr {
                                marginTop(10f)
                                alignItemsCenter()
                            }
                            View {
                                attr {
                                    size(5f, 5f)
                                    borderRadius(3f)
                                    backgroundColor(ctx.SRThemeColor.primary)
                                }
                            }
                            Column {
                                attr {
                                    flex(1f)
                                    marginLeft(8f)
                                }
                                Text {
                                    attr {
                                        text(ctx.attr.analysis()?.signals?.getOrNull(0)?.title.orEmpty())
                                        fontSize(13f)
                                        fontWeightMedium()
                                        color(ctx.SRThemeColor.primaryText)
                                    }
                                }
                                Text {
                                    attr {
                                        marginTop(2f)
                                        text(ctx.attr.analysis()?.signals?.getOrNull(0)?.description.orEmpty())
                                        fontSize(12f)
                                        lineHeight(18f)
                                        color(ctx.SRThemeColor.secondaryText)
                                    }
                                }
                            }
                        }

                        Row {
                            attr {
                                marginTop(10f)
                                alignItemsCenter()
                            }
                            View {
                                attr {
                                    size(5f, 5f)
                                    borderRadius(3f)
                                    backgroundColor(ctx.SRThemeColor.primary)
                                }
                            }
                            Column {
                                attr {
                                    flex(1f)
                                    marginLeft(8f)
                                }
                                Text {
                                    attr {
                                        text(ctx.attr.analysis()?.signals?.getOrNull(1)?.title.orEmpty())
                                        fontSize(13f)
                                        fontWeightMedium()
                                        color(ctx.SRThemeColor.primaryText)
                                    }
                                }
                                Text {
                                    attr {
                                        marginTop(2f)
                                        text(ctx.attr.analysis()?.signals?.getOrNull(1)?.description.orEmpty())
                                        fontSize(12f)
                                        lineHeight(18f)
                                        color(ctx.SRThemeColor.secondaryText)
                                    }
                                }
                            }
                        }

                        Row {
                            attr {
                                marginTop(10f)
                                alignItemsCenter()
                            }
                            View {
                                attr {
                                    size(5f, 5f)
                                    borderRadius(3f)
                                    backgroundColor(ctx.SRThemeColor.primary)
                                }
                            }
                            Column {
                                attr {
                                    flex(1f)
                                    marginLeft(8f)
                                }
                                Text {
                                    attr {
                                        text(ctx.attr.analysis()?.signals?.getOrNull(2)?.title.orEmpty())
                                        fontSize(13f)
                                        fontWeightMedium()
                                        color(ctx.SRThemeColor.primaryText)
                                    }
                                }
                                Text {
                                    attr {
                                        marginTop(2f)
                                        text(ctx.attr.analysis()?.signals?.getOrNull(2)?.description.orEmpty())
                                        fontSize(12f)
                                        lineHeight(18f)
                                        color(ctx.SRThemeColor.secondaryText)
                                    }
                                }
                            }
                        }

                        View {
                            attr {
                                marginTop(16f)
                                height(1f)
                                backgroundColor(ctx.SRThemeColor.divider)
                            }
                        }
                        Row {
                            attr {
                                marginTop(14f)
                                alignItemsCenter()
                                justifyContentFlexStart()
                            }
                            View {
                                attr {
                                    width(3f)
                                    height(14f)
                                    marginRight(5f)
                                    borderRadius(3f)
                                    backgroundColor(ctx.SRThemeColor.primary)
                                }
                            }

                            Text {
                                attr {
                                    text("行情总结")
                                    fontSize(14f)
                                    fontWeightBold()
                                    color(ctx.SRThemeColor.primaryText)
                                }
                            }
                        }
                        Text {
                            attr {
                                marginTop(6f)
                                text(
                                    listOf(
                                        ctx.attr.analysis()?.summary,
                                        ctx.attr.analysis()?.disclaimer
                                    )
                                        .filterNotNull()
                                        .joinToString("\n")
                                )
                                fontSize(12f)
                                lineHeight(18f)
                                color(ctx.SRThemeColor.secondaryText)
                            }
                        }
                        View {
                            attr {
                                marginTop(14f)
                                height(1f)
                                backgroundColor(ctx.SRThemeColor.divider)
                            }
                        }
                        Text {
                            attr {
                                marginTop(10f)
                                text("信息更新日期 · ${ctx.formatUpdatedAt(ctx.attr.updatedAt())}")
                                fontSize(12f)
                                color(ctx.SRThemeColor.secondaryText)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun formatUpdatedAt(value: String): String = value
        .replace('T', ' ')
        .removeSuffix("Z")
        .substringBefore('.')
        .ifBlank { "-" }

    private fun riskColors(): RiskColors = when (attr.analysis()?.risk?.level?.trim()) {
        "低" -> RiskColors(SRThemeColor.lowRisk, SRThemeColor.lowRiskBackground)
        "中低" -> RiskColors(SRThemeColor.mediumLowRisk, SRThemeColor.mediumLowRiskBackground)
        "中等" -> RiskColors(SRThemeColor.mediumRisk, SRThemeColor.mediumRiskBackground)
        "中高" -> RiskColors(SRThemeColor.mediumHighRisk, SRThemeColor.mediumHighRiskBackground)
        "高" -> RiskColors(SRThemeColor.error, SRThemeColor.errorBackground)
        else -> RiskColors(SRThemeColor.neutralChange, SRThemeColor.neutralChangeBackground)
    }

}

internal enum class StockAIAnalysisStatus {
    ANALYZING,
    COMPLETED,
    FAILED,
}

internal class StockAIProposalViewAttr : ComposeAttr() {
    var status: () -> StockAIAnalysisStatus = { StockAIAnalysisStatus.COMPLETED }
    var analysis: () -> StockAiAnalysisContent? = { null }
    var errorMessage: () -> String = { "" }
    var updatedAt: () -> String = { "" }
}

internal class StockAIProposalViewEvent : ComposeEvent() {
    var onRetry: () -> Unit = {}
}

internal fun ViewContainer<*, *>.StockAIProposal(init: StockAIProposalView.() -> Unit) {
    addChild(StockAIProposalView(), init)
}

private data class RiskColors(
    val text: Long,
    val background: Long,
)
