package com.imcys.sairen.component

import com.imcys.sairen.theme.SRThemeColor
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.views.View

internal enum class SRDividerDirection {
    HORIZONTAL,
    VERTICAL,
}

/** 使用主题分割线颜色的横向或纵向分割线；横向分割线始终铺满父容器。 */
internal class SRDividerView : ComposeView<SRDividerViewAttr, SRDividerViewEvent>() {

    override fun createAttr(): SRDividerViewAttr = SRDividerViewAttr()

    override fun createEvent(): SRDividerViewEvent = SRDividerViewEvent()

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    if (ctx.attr.direction == SRDividerDirection.HORIZONTAL) {
                        height(ctx.attr.thickness)
                    } else {
                        width(ctx.attr.thickness)
                        ctx.attr.verticalLength?.let(::height)
                    }
                    backgroundColor(ctx.SRThemeColor.divider)
                }
            }
        }
    }
}

internal class SRDividerViewAttr : ComposeAttr() {
    var direction = SRDividerDirection.HORIZONTAL
    var thickness = 1f
    var verticalLength: Float? = null
}

internal class SRDividerViewEvent : ComposeEvent()

internal fun ViewContainer<*, *>.SRDivider(init: SRDividerView.() -> Unit = {}) {
    addChild(SRDividerView(), init)
}
