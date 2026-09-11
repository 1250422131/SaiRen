package com.imcys.sairen.ui.agent

import com.imcys.sairen.base.BasePager
import com.imcys.sairen.component.SRNavigationBar
import com.imcys.sairen.component.chat.SRChatComposer
import com.imcys.sairen.component.chat.SRChatMessage
import com.imcys.sairen.component.chat.SRChatThinkingMessage
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewRef
import com.tencent.kuikly.core.directives.scrollToPosition
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.timer.setTimeout
import com.tencent.kuikly.core.views.List
import com.tencent.kuikly.core.views.ListView
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

@Page("agent_chat")
internal class AgentChatPager : BasePager() {
    private lateinit var listRef: ViewRef<ListView<*, *>>
    private val store by lazy(LazyThreadSafetyMode.NONE) {
        AgentChatStore(this, ::scrollToBottom)
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                flex(1f)
                backgroundColor(Color(ctx.SRThemeColor.pageBackground))
            }
            SRNavigationBar {
                attr {
                    title = "塞壬之声"
                    enabledBack = true
                }
            }
            List {
                ref { ctx.listRef = it }
                attr {
                    flex(1f)
                    showScrollerIndicator(false)
                }

                vif({ ctx.store.state.hasMoreHistory || ctx.store.state.isLoadingHistory }) {
                    View {
                        attr {
                            minHeight(44f)
                            allCenter()
                        }
                        event {
                            click {
                                if (ctx.store.state.hasMoreHistory && !ctx.store.state.isLoadingHistory) {
                                    ctx.store.dispatch(AgentChatIntent.LoadOlderHistory)
                                }
                            }
                        }
                        Text {
                            attr {
                                text(if (ctx.store.state.isLoadingHistory) "正在加载..." else "加载更早消息")
                                fontSize(13f)
                                color(ctx.SRThemeColor.secondaryText)
                            }
                        }
                    }
                }

                vfor({ ctx.store.messages }) { message ->
                    SRChatMessage {
                        attr { this.message = message }
                    }
                }

                vif({ ctx.store.state.isSending }) {
                    SRChatThinkingMessage()
                }

                vif({ ctx.store.messages.isEmpty() && !ctx.store.state.isLoadingHistory }) {
                    View {
                        attr {
                            minHeight(180f)
                            padding(left = 32f, right = 32f)
                            allCenter()
                        }
                        Text {
                            attr {
                                text(
                                    ctx.store.state.errorMessage.ifBlank {
                                        "可以问我股票行情、日 K、企业信息或买卖观察时机"
                                    }
                                )
                                fontSize(14f)
                                lines(Int.MAX_VALUE)
                                textAlignCenter()
                                color(ctx.SRThemeColor.secondaryText)
                            }
                        }
                    }
                }
            }
            SRChatComposer {
                attr { disabled = { ctx.store.state.isSending } }
                event { onSend = { ctx.store.dispatch(AgentChatIntent.SendMessage(it)) } }
            }
            View {
                attr { height(ctx.pageData.safeAreaInsets.bottom) }
            }
        }
    }

    override fun pageDidAppear() {
        super.pageDidAppear()
        store.dispatch(AgentChatIntent.LoadHistory)
    }

    private fun scrollToBottom() {
        setTimeout(50) {
            val historyHeaderCount =
                if (store.state.hasMoreHistory || store.state.isLoadingHistory) 1 else 0
            val itemCount = historyHeaderCount + store.messages.size + if (store.state.isSending) 1 else 0
            if (itemCount > 0 && ::listRef.isInitialized) {
                listRef.view?.scrollToPosition(itemCount - 1, animate = true)
            }
        }
    }
}
