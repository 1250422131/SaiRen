package com.imcys.sairen.component.screen.stock

import com.imcys.sairen.component.SRCard
import com.imcys.sairen.component.SRIcon
import com.imcys.sairen.core.common.ext.acquireBridgeModule
import com.imcys.sairen.core.common.ext.toCommonAssets
import com.imcys.sairen.core.common.ext.toPageAssets
import com.imcys.sairen.core.network.NetWorkResult
import com.imcys.sairen.core.network.emptyNetWorkResult
import com.imcys.sairen.core.network.model.StockCompanyProfile
import com.imcys.sairen.theme.SRThemeColor
import com.tencent.kuikly.core.base.Animation
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.Rotate
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Image
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.tencent.kuikly.core.views.layout.Column
import com.tencent.kuikly.core.views.layout.Row

internal class CorporateSummaryInfoView :
    ComposeView<CorporateSummaryInfoViewAttr, CorporateSummaryInfoViewEvent>() {

    private var isSummaryExpanded by observable(false)
    private var isBusinessScopeExpanded by observable(false)

    override fun createEvent(): CorporateSummaryInfoViewEvent {
        return CorporateSummaryInfoViewEvent()
    }

    override fun createAttr(): CorporateSummaryInfoViewAttr {
        return CorporateSummaryInfoViewAttr()
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                animate(Animation.easeInOut(EXPAND_ANIMATION_DURATION), ctx.isSummaryExpanded)
                animate(Animation.easeInOut(EXPAND_ANIMATION_DURATION), ctx.isBusinessScopeExpanded)
            }

            // 企业名牌
            vif({ !ctx.attr.details().data?.companyName.isNullOrBlank() }) {
                SRCard {
                    Row {
                        attr {
                            margin(10f)
                            alignItemsCenter()
                        }
                        View {
                            attr {
                                padding(8f)
                                borderRadius(30f)
                                backgroundColor(ctx.SRThemeColor.primaryContainer)
                            }
                            Image {
                                attr {
                                    size(24f, 24f)
                                    src("badge_24dp_fill.svg".toPageAssets())
                                    tintColor(Color(ctx.SRThemeColor.primary))
                                }
                            }
                        }
                        Column {
                            attr {
                                flex(1f)
                                marginLeft(10f)
                            }

                            Text {
                                attr {
                                    fontSize(16f)
                                    fontWeightBold()
                                    text(ctx.attr.details().data?.companyName ?: "-")
                                }
                            }
                            vif({ctx.attr.details().data?.industry != "-"}){
                                Scroller {
                                    attr {
                                        height(30f)
                                        flexDirectionRow()
                                        showScrollerIndicator(false)
                                    }
                                    Row {
                                        View {
                                            attr {
                                                alignSelfFlexStart()
                                                marginTop(4f)
                                                backgroundColor(ctx.SRThemeColor.primaryContainer)
                                                padding(top = 5f, bottom = 5f, left = 10f, right = 10f)
                                                borderRadius(30f)
                                            }
                                            Text {
                                                attr {
                                                    fontSize(12f)
                                                    fontWeightBold()
                                                    color(ctx.SRThemeColor.primary)
                                                    text(ctx.attr.details().data?.industry ?: "-")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Row {
                        attr {
                            alignItemsCenter()
                            justifyContentSpaceBetween()
                            backgroundColor(ctx.SRThemeColor.primaryContainer)
                            padding(top = 10f, bottom = 10f, left = 18f, right = 18f)
                        }
                        event {
                            click {
                                ctx.attr.details().data?.website
                                    ?.takeIf { it.isNotBlank() }
                                    ?.let { ctx.acquireBridgeModule().openLink(it) }
                            }
                        }
                        Image {
                            attr {
                                size(24f, 24f)
                                src("link_2_24dp.svg".toPageAssets())
                                tintColor(Color(ctx.SRThemeColor.primary))
                            }
                        }

                        Text {
                            attr {
                                flex(1f)
                                marginLeft(20f)
                                fontSize(13f)
                                text(ctx.attr.details().data?.website ?: "-")
                            }
                        }
                        Image {
                            attr {
                                size(24f, 24f)
                                src("chevron_right_24dp.svg".toCommonAssets())
                                tintColor(Color(ctx.SRThemeColor.secondaryText))
                            }
                        }

                    }
                }
            }

            // 企业介绍
            vif({ ctx.attr.details().data?.summary != "-" }) {
                ExpandableInfoCard {
                    attr {
                        marginTop(10f)
                        title = "公司简介"
                        icon = "apartment_24dp_fill.svg".toPageAssets()
                        content = { ctx.attr.details().data?.summary ?: "-" }
                        expanded = { ctx.isSummaryExpanded }
                    }
                    event {
                        onToggle = {
                            ctx.isSummaryExpanded = !ctx.isSummaryExpanded
                        }
                    }
                }
            }

            // 经营范围
            vif({ ctx.attr.details().data?.businessScope != "-" }) {
                ExpandableInfoCard {
                    attr {
                        marginTop(10f)
                        title = "经营范围"
                        icon = "business_center_24dp_fill.svg".toPageAssets()
                        content = { ctx.attr.details().data?.businessScope ?: "-" }
                        expanded = { ctx.isBusinessScopeExpanded }
                    }
                    event {
                        onToggle = {
                            ctx.isBusinessScopeExpanded = !ctx.isBusinessScopeExpanded
                        }
                    }
                }
            }

            // 主要人员
            vif({
                val data = ctx.attr.details().data
                !data?.chairman.isNullOrBlank() ||
                    !data?.legalRepresentative.isNullOrBlank() ||
                    !data?.generalManager.isNullOrBlank()
            }) {
                SRCard {
                    attr {
                        marginTop(10f)
                        padding(18f)
                    }

                   Row {
                       attr { alignItemsCenter() }
                       SRIcon {
                           attr {
                               src("groups_24dp_fill.svg".toPageAssets())
                               tintColor(Color(ctx.SRThemeColor.primary))
                               marginRight(5f)
                           }
                       }
                       Text {
                           attr {
                               fontSize(15f)
                               fontWeightBold()
                               color(ctx.SRThemeColor.primaryText)
                               text("主要人员")
                           }
                       }
                   }
                    vif({ !ctx.attr.details().data?.chairman.isNullOrBlank() }) {
                        Row {
                            attr {
                                marginTop(12f)
                            }
                            Text {
                                attr {
                                    width(80f)
                                    fontSize(13f)
                                    color(ctx.SRThemeColor.secondaryText)
                                    text("董事长")
                                }
                            }
                            Text {
                                attr {
                                    flex(1f)
                                    fontSize(13f)
                                    color(ctx.SRThemeColor.primaryText)
                                    text(ctx.attr.details().data?.chairman ?: "-")
                                }
                            }
                        }
                    }
                    vif({ !ctx.attr.details().data?.legalRepresentative.isNullOrBlank() }) {
                        Row {
                            attr {
                                marginTop(12f)
                            }
                            Text {
                                attr {
                                    width(80f)
                                    fontSize(13f)
                                    color(ctx.SRThemeColor.secondaryText)
                                    text("法定代表人")
                                }
                            }
                            Text {
                                attr {
                                    flex(1f)
                                    fontSize(13f)
                                    color(ctx.SRThemeColor.primaryText)
                                    text(ctx.attr.details().data?.legalRepresentative ?: "-")
                                }
                            }
                        }
                    }
                    vif({ !ctx.attr.details().data?.generalManager.isNullOrBlank() }) {
                        Row {
                            attr {
                                marginTop(12f)
                            }
                            Text {
                                attr {
                                    width(80f)
                                    fontSize(13f)
                                    color(ctx.SRThemeColor.secondaryText)
                                    text("总经理")
                                }
                            }
                            Text {
                                attr {
                                    flex(1f)
                                    fontSize(13f)
                                    color(ctx.SRThemeColor.primaryText)
                                    text(ctx.attr.details().data?.generalManager ?: "-")
                                }
                            }
                        }
                    }
                }
            }

            View {
                attr { height(40f) }
            }

        }
    }

    private companion object {
        const val EXPAND_ANIMATION_DURATION = 0.25f
    }
}

internal class CorporateSummaryInfoViewAttr : ComposeAttr() {
    var details: () -> NetWorkResult<StockCompanyProfile> = { emptyNetWorkResult() }
}

internal class CorporateSummaryInfoViewEvent : ComposeEvent() {
}

internal fun ViewContainer<*, *>.CorporateSummaryInfo(init: CorporateSummaryInfoView.() -> Unit) {
    addChild(CorporateSummaryInfoView(), init)
}
