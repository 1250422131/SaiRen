package com.imcys.sairen.component

import com.imcys.sairen.theme.SRThemeColor
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.attr.ImageUri
import com.tencent.kuikly.core.views.Image

internal class SRSweepLightImageView :
    ComposeView<SRSweepLightImageAttr, SRSweepLightImageEvent>() {

    override fun createAttr(): SRSweepLightImageAttr = SRSweepLightImageAttr()

    override fun createEvent(): SRSweepLightImageEvent = SRSweepLightImageEvent()

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            Image {
                attr {
                    absolutePosition(left = 0f, top = 0f, right = 0f, bottom = 0f)
                    resizeContain()
                    ctx.attr.imageUri?.let(::src)
                    tintColor(Color(ctx.SRThemeColor.loadingTint))
                }
            }
        }
    }
}

internal class SRSweepLightImageAttr : ComposeAttr() {
    var imageUri: ImageUri? = null
}

internal class SRSweepLightImageEvent : ComposeEvent()

internal fun ViewContainer<*, *>.SRSweepLightImage(
    init: SRSweepLightImageView.() -> Unit = {},
) {
    addChild(SRSweepLightImageView(), init)
}
