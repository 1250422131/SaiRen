package com.imcys.sairen.component.screen.stock

import com.imcys.sairen.component.SRCard
import com.imcys.sairen.component.SRIcon
import com.imcys.sairen.core.common.ext.toPageAssets
import com.imcys.sairen.theme.SRThemeColor
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.Rotate
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.attr.ImageUri
import com.tencent.kuikly.core.views.Image
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.layout.Row

class ExpandableInfoCardView :
    ComposeView<ExpandableInfoCardViewAttr, ExpandableInfoCardViewEvent>() {

    override fun createEvent(): ExpandableInfoCardViewEvent = ExpandableInfoCardViewEvent()

    override fun createAttr(): ExpandableInfoCardViewAttr = ExpandableInfoCardViewAttr()

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            SRCard {
                Row {
                    attr {
                        alignItemsCenter()
                        justifyContentSpaceBetween()
                        padding(top = 18f, left = 18f, right = 18f)
                    }
                    event {
                        click {
                            ctx.event.onToggle()
                        }
                    }
                  Row {
                      attr {
                          alignItemsCenter()
                      }
                      ctx.attr.icon?.let {
                          SRIcon {
                              attr {
                                  src(it)
                                  tintColor(Color(ctx.SRThemeColor.primary))
                                  marginRight(5f)
                              }
                          }
                      }
                      Text {
                          attr {
                              fontSize(15f)
                              fontWeightBold()
                              color(ctx.SRThemeColor.primaryText)
                              text(ctx.attr.title)
                          }
                      }
                  }
                    Image {
                        attr {
                            size(24f, 24f)
                            src("expand_more_24dp.svg".toPageAssets())
                            tintColor(Color(ctx.SRThemeColor.secondaryText))
                            transform(Rotate(if (ctx.attr.expanded()) 180f else 0f))
                        }
                    }
                }
                Text {
                    attr {
                        margin(left = 18f, top = 12f, right = 18f, bottom = 18f)
                        fontSize(13f)
                        lineHeight(20f)
                        lines(if (ctx.attr.expanded()) 0 else COLLAPSED_LINE_COUNT)
                        textOverFlowTail()
                        color(ctx.SRThemeColor.secondaryText)
                        text(ctx.attr.content())
                    }
                }
            }
        }
    }

    private companion object {
        const val COLLAPSED_LINE_COUNT = 3
    }
}

class ExpandableInfoCardViewAttr : ComposeAttr() {
    var title = ""
    var icon : ImageUri? = null
    var content: () -> String = { "" }
    var expanded: () -> Boolean = { false }
}

class ExpandableInfoCardViewEvent : ComposeEvent() {
    var onToggle: () -> Unit = {}
}

fun ViewContainer<*, *>.ExpandableInfoCard(init: ExpandableInfoCardView.() -> Unit) {
    addChild(ExpandableInfoCardView(), init)
}

