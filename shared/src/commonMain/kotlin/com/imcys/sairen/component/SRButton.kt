package com.imcys.sairen.component

import com.imcys.sairen.theme.SRRadius
import com.imcys.sairen.theme.SRThemeColor
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

internal class SRButtonView: ComposeView<SRButtonViewAttr, SRButtonViewEvent>() {
    
    override fun createEvent(): SRButtonViewEvent {
        return SRButtonViewEvent()
    }

    override fun createAttr(): SRButtonViewAttr {
        return SRButtonViewAttr()
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    padding(left = 10f, right = 10f, top = 8f, bottom = 8f)
                    borderRadius(SRRadius.MEDIUM)
                    backgroundColor(ctx.SRThemeColor.primary)
                }

                Text {
                    attr {
                        color(Color.WHITE)
                        text("点点滴滴")
                    }
                }

            }
        }
    }
}


internal class SRButtonViewAttr : ComposeAttr() {

}

internal class SRButtonViewEvent : ComposeEvent() {
    
}

internal fun ViewContainer<*, *>.SRButton(init: SRButtonView.() -> Unit) {
    addChild(SRButtonView(), init)
}