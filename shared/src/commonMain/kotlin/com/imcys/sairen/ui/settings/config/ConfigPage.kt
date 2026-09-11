package com.imcys.sairen.ui.settings.config

import com.imcys.sairen.base.BasePager
import com.imcys.sairen.component.SRNavigationBar
import com.imcys.sairen.core.common.config.ServerPreferences
import com.imcys.sairen.theme.SRRadius
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Border
import com.tencent.kuikly.core.base.BorderStyle
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.views.Input
import com.tencent.kuikly.core.views.LengthLimitType
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.TextConst
import com.tencent.kuikly.core.views.View
import com.tencent.kuikly.core.views.layout.Column
import com.tencent.kuikly.core.views.layout.ColumnView

/**
 * 配置页：塞壬后台地址等接口行为的设置入口。
 */
@Page("config")
internal class ConfigPage : BasePager() {

    private lateinit var store: ConfigStore

    override fun created() {
        super.created()
        store = ConfigStore(this)
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flex(1f)
                    backgroundColor(Color(ctx.SRThemeColor.pageBackground))
                }

                SRNavigationBar {
                    attr {
                        title = "配置"
                        enabledBack = true
                    }
                }

                Scroller {
                    attr {
                        flex(1f)
                        showScrollerIndicator(false)
                    }

                    Column {
                        attr {
                            padding(left = 20f, top = 6f, right = 20f, bottom = 30f)
                        }

                        SectionHeader(
                            title = "塞壬后台",
                            summary = "AI 分析、登录、会话等接口的后台地址",
                        )

                        ConfigCard {
                            ServerUrlInput(ctx.store)
                            SaveButton(ctx.store)
                            ResetButton(ctx.store)
                        }
                    }
                }
            }
        }
    }
}

/** 分组标题；颜色在 attr 作用域内读取，换肤后能同步刷新 */
private fun ViewContainer<*, *>.SectionHeader(
    title: String,
    summary: String,
) {
    val pager = getPager() as BasePager
    Column {
        attr {}
        Text {
            attr {
                fontSize(15f)
                fontWeightBold()
                color(Color(pager.SRThemeColor.primaryText))
                text(title)
            }
        }
        Text {
            attr {
                marginTop(3f)
                fontSize(12f)
                color(Color(pager.SRThemeColor.secondaryText))
                text(summary)
            }
        }
    }
}

/** 配置页自用卡片：描边色在 attr 作用域内取值，换肤后能同步刷新 */
private fun ViewContainer<*, *>.ConfigCard(init: ColumnView.() -> Unit = {}) {
    val pager = getPager() as BasePager
    Column {
        attr {
            marginTop(10f)
            border(
                Border(
                    lineWidth = 1f,
                    lineStyle = BorderStyle.SOLID,
                    color = Color(pager.SRThemeColor.divider),
                )
            )
            borderRadius(SRRadius.LARGE)
        }
        init()
    }
}

/** 塞壬后台地址输入框 */
private fun ColumnView.ServerUrlInput(store: ConfigStore) {
    val pager = getPager() as BasePager
    attr {
        padding(14f)
    }

    Text {
        attr {
            fontSize(13f)
            color(Color(pager.SRThemeColor.secondaryText))
            text("地址")
        }
    }

    View {
        attr {
            marginTop(8f)
            padding(left = 10f, right = 10f)
            borderRadius(SRRadius.MEDIUM)
            border(
                Border(
                    lineWidth = 1f,
                    lineStyle = BorderStyle.SOLID,
                    color = Color(pager.SRThemeColor.divider),
                )
            )
        }
        Input {
            attr {
                height(46f)
                fontSize(15f)
                color(Color(pager.SRThemeColor.primaryText))
                placeholderColor(Color(pager.SRThemeColor.secondaryText))
                placeholder(ServerPreferences.DEFAULT_BASE_URL)
                text(store.draft)
                maxTextLength(200, LengthLimitType.CHARACTER)
                returnKeyTypeDone()
                autoHideKeyboardOnImeAction(true)
            }
            event {
                textDidChange(isSyncEdit = true) {
                    // 原生输入已更新，先同步缓存，避免 draft 回写文本时将光标移到末尾。
                    this@Input.getViewAttr().updatePropCache(TextConst.VALUE, it.text)
                    store.dispatch(ConfigIntent.ChangeServerUrl(it.text))
                }
                inputReturn { store.dispatch(ConfigIntent.Save) }
            }
        }
    }

    Text {
        attr {
            marginTop(8f)
            fontSize(12f)
            color(Color(pager.SRThemeColor.secondaryText))
            text("当前生效：${store.saved}（保存后立即生效）")
        }
    }
}

/** 保存按钮：草稿没有变化时置灰 */
private fun ColumnView.SaveButton(store: ConfigStore) {
    val pager = getPager() as BasePager
    View {
        attr {
            margin(top = 10f)
            height(44f)
            allCenter()
            borderRadius(SRRadius.MEDIUM + 3f)
            backgroundColor(
                Color(
                    if (store.isDirty) {
                        pager.SRThemeColor.primary
                    } else {
                        pager.SRThemeColor.neutralChangeBackground
                    }
                )
            )
        }
        event {
            click {
                if (store.isDirty) {
                    store.dispatch(ConfigIntent.Save)
                }
            }
        }
        Text {
            attr {
                fontSize(15f)
                fontWeightBold()
                text(if (store.isDirty) "保存" else "已保存")
                color(
                    Color(
                        if (store.isDirty) {
                            0xFFFFFFFF
                        } else {
                            pager.SRThemeColor.secondaryText
                        }
                    )
                )
            }
        }
    }
}

/** 恢复默认地址 */
private fun ColumnView.ResetButton(store: ConfigStore) {
    val pager = getPager() as BasePager
    View {
        attr {
            margin(top = 10f)
            height(40f)
            allCenter()
            borderRadius(SRRadius.MEDIUM + 3f)
            backgroundColor(Color(pager.SRThemeColor.neutralChangeBackground))
        }
        event {
            click {
                if (store.saved != ServerPreferences.DEFAULT_BASE_URL) {
                    store.dispatch(ConfigIntent.ResetToDefault)
                }
            }
        }
        Text {
            attr {
                fontSize(14f)
                text("恢复默认地址")
                color(Color(pager.SRThemeColor.secondaryText))
            }
        }
    }
}
