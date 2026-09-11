package com.imcys.sairen.component.chat

import com.imcys.sairen.theme.SRRadius
import com.imcys.sairen.theme.SRThemeColor
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.ViewRef
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Input
import com.tencent.kuikly.core.views.InputView
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.tencent.kuikly.core.views.layout.Row

internal class SRChatComposerView : ComposeView<SRChatComposerAttr, SRChatComposerEvent>() {
    private var inputText by observable("")
    private lateinit var inputRef: ViewRef<InputView>

    override fun createAttr() = SRChatComposerAttr()
    override fun createEvent() = SRChatComposerEvent()

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            Row {
                attr {
                    alignItemsCenter()
                    padding(left = 12f, right = 12f, top = 10f, bottom = 10f)
                    backgroundColor(Color(ctx.SRThemeColor.pageBackground))
                }
                View {
                    attr {
                        flex (1f)
                        backgroundColor(Color(ctx.SRThemeColor.stockTagBackground))
                        borderRadius(SRRadius.LARGE)
                    }
                    Input {
                        ref { ctx.inputRef = it }
                        attr {
                            margin(left = 10f)
                            flex(1f)
                            height(42f)
                            fontSize(15f)
                            returnKeyTypeSend()
                            color(Color(ctx.SRThemeColor.primaryText))
                            tintColor(Color(ctx.SRThemeColor.primary))
                            placeholder("输入消息")
                            placeholderColor(Color(ctx.SRThemeColor.secondaryText))
                            enablesReturnKeyAutomatically(true)
                            autoHideKeyboardOnImeAction(false)
                            editable(!ctx.attr.disabled())
                        }
                        event {
                            textDidChange { ctx.inputText = it.text }
                            inputReturn { ctx.send() }
                        }
                    }
                }
                View {
                    attr {
                        minWidth(58f)
                        height(42f)
                        marginLeft(8f)
                        allCenter()
                        borderRadius(SRRadius.LARGE)
                        backgroundColor(Color(if (ctx.attr.disabled()) ctx.SRThemeColor.divider else ctx.SRThemeColor.primary))
                    }
                    event { click { ctx.send() } }
                    Text {
                        attr {
                            text("发送")
                            fontSize(14f)
                            fontWeightMedium()
                            color(Color.WHITE)
                        }
                    }
                }
            }
        }
    }

    private fun send() {
        val content = inputText.trim()
        if (content.isEmpty() || attr.disabled()) return
        inputText = ""
        inputRef.view?.setText("")
        event.onSend(content)
    }
}

internal class SRChatComposerAttr : ComposeAttr() {
    var disabled: () -> Boolean = { false }
}

internal class SRChatComposerEvent : ComposeEvent() {
    var onSend: (String) -> Unit = {}
}

internal fun ViewContainer<*, *>.SRChatComposer(init: SRChatComposerView.() -> Unit) {
    addChild(SRChatComposerView(), init)
}
