package com.imcys.sairen.component

import com.imcys.sairen.theme.SRThemeColor
import com.tencent.kuikly.core.base.Animation
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ColorStop
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.Direction
import com.tencent.kuikly.core.base.Translate
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.ViewRef
import com.tencent.kuikly.core.base.attr.ImageUri
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.DivView
import com.tencent.kuikly.core.views.Image
import com.tencent.kuikly.core.views.MaskView
import com.tencent.kuikly.core.views.View

internal class SRSweepLightImageView :
    ComposeView<SRSweepLightImageAttr, SRSweepLightImageEvent>() {

    private var isMaskImageLoaded by observable(false)
    private var hasStartedSweep = false
    private var sweepLightRef: ViewRef<DivView>? = null

    override fun createAttr(): SRSweepLightImageAttr = SRSweepLightImageAttr()

    override fun createEvent(): SRSweepLightImageEvent = SRSweepLightImageEvent()

    override fun viewDidLayout() {
        super.viewDidLayout()
        if (hasStartedSweep) {
            return
        }
        hasStartedSweep = true
        sweepLightRef?.view?.animateToAttr(
            Animation.linear(1.8f).repeatForever(true),
            attrBlock = {
                transform(Translate(percentageX = 0f, percentageY = 1f))
            },
        )
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            val colors = ctx.SRThemeColor
            View {
                attr {
                    absolutePosition(left = 0f, top = 0f, right = 0f, bottom = 0f)
                    overflow(true)
                }

                Image {
                    attr {
                        absolutePosition(left = 0f, top = 0f, right = 0f, bottom = 0f)
                        resizeContain()
                        tintColor(Color(colors.loadingTint))
                        ctx.attr.imageUri?.let(::src)
                    }
                }

                addChild(MaskView()) {
                    attr {
                        absolutePosition(left = 0f, top = 0f, right = 0f, bottom = 0f)
                    }

                    View {
                        attr {
                            absolutePosition(left = 0f, top = 0f, right = 0f, bottom = 0f)
                            opacity(if (ctx.isMaskImageLoaded) 1f else 0.999f)
                        }
                        Image {
                            attr {
                                absolutePosition(left = 0f, top = 0f, right = 0f, bottom = 0f)
                                resizeContain()
                                ctx.attr.imageUri?.let(::src)
                            }
                            event {
                                loadSuccess {
                                    ctx.isMaskImageLoaded = true
                                }
                            }
                        }
                    }

                    View {
                        attr {
                            absolutePosition(left = 0f, top = 0f, right = 0f, bottom = 0f)
                        }
                        addChild(DivView()) {
                            ref {
                                ctx.sweepLightRef = it
                            }
                            attr {
                                absolutePosition(left = 0f, top = 0f, right = 0f, bottom = 0f)
                                backgroundLinearGradient(
                                    Direction.TO_BOTTOM,
                                    ColorStop(Color(colors.transparent), 0f),
                                    ColorStop(Color(colors.transparent), 0.2f),
                                    ColorStop(Color(colors.sweepHighlight), 0.5f),
                                    ColorStop(Color(colors.transparent), 0.8f),
                                    ColorStop(Color(colors.transparent), 1f),
                                )
                                transform(
                                    Translate(
                                        percentageX = 0f,
                                        percentageY = -1f,
                                    ),
                                )
                            }
                        }
                    }
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
