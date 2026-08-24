package com.imcys.sairen.component

import com.imcys.sairen.core.common.ext.acquireRouterModule
import com.imcys.sairen.core.common.ext.toCommonAssets
import com.imcys.sairen.theme.SRThemeColor
import com.imcys.sairen.theme.SRRadius
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.attr.ImageUri
import com.tencent.kuikly.core.views.Image
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

internal class SRNavigationBarView :
    ComposeView<SRNavigationBarViewAttr, SRNavigationBarViewEvent>() {

    override fun createEvent(): SRNavigationBarViewEvent {
        return SRNavigationBarViewEvent()
    }

    override fun createAttr(): SRNavigationBarViewAttr {
        return SRNavigationBarViewAttr()
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    paddingTop(ctx.pagerData.statusBarHeight)
                    backgroundColor(Color(ctx.SRThemeColor.pageBackground))
                }

                View {
                    attr {
                        flexDirectionRow()
                        padding(left = 20f, right = 20f, top = 10f, bottom = 10f)


                        justifyContentSpaceBetween()
                    }

                    View {
                        attr {
                            flexDirectionRow()
                            alignItemsCenter()
                        }
                        if (ctx.attr.enabledBack){
                            Image {
                                attr {
                                    margin(right = 10f)
                                    size(25f, 25f)
                                    src("arrow_back_24dp.png".toCommonAssets())
                                    tintColor(Color(ctx.SRThemeColor.primaryText))
                                }
                                event {
                                    click {
                                        ctx.acquireRouterModule().closePage()
                                    }
                                }
                            }
                        } else {
                            ctx.attr.icon?.let {
                                Image {
                                    attr {
                                        margin(right = 5f)
                                        size(30f, 30f)
                                        src(it)
                                        borderRadius(SRRadius.MEDIUM)
                                    }
                                }
                            }
                        }
                        Text {
                            attr {
                                fontWeightBold()
                                fontSize(if (ctx.attr.enabledBack) 22f else 25f)
                                text(ctx.attr.title)
                                color(ctx.SRThemeColor.primaryText)
                            }
                        }
                    }

                    View {
                        attr {

                        }
                        ctx.attr.action?.let { action ->
                            action()
                        }
                    }


                }

            }
        }
    }
}


internal class SRNavigationBarViewAttr : ComposeAttr() {
    var title = ""
    var action: (ViewContainer<*, *>.() -> Unit)? = null

    var icon: ImageUri? = null

    var enabledBack: Boolean = false

}


internal class SRNavigationBarViewEvent : ComposeEvent() {

}

internal fun ViewContainer<*, *>.SRNavigationBar(init: SRNavigationBarView.() -> Unit) {
    addChild(SRNavigationBarView(), init)
}
