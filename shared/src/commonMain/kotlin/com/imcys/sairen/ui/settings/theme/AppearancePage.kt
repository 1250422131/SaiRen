package com.imcys.sairen.ui.settings.theme

import com.imcys.sairen.base.BasePager
import com.imcys.sairen.component.SRNavigationBar
import com.imcys.sairen.core.common.ext.toObservableList
import com.imcys.sairen.theme.SRRadius
import com.imcys.sairen.theme.SRThemeMode
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Border
import com.tencent.kuikly.core.base.BorderStyle
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.reactive.collection.ObservableList
import com.tencent.kuikly.core.views.DivView
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.tencent.kuikly.core.views.layout.Column
import com.tencent.kuikly.core.views.layout.Row

/**
 * 外观设置：深浅色策略。
 *
 * 交互约定：改动只作用于「草稿」，页面内即时反馈；点「应用主题」才落盘并全局生效。
 */
@Page("appearance")
internal class AppearancePage : BasePager() {

    private lateinit var store: AppearanceStore

    private val modeOptions: ObservableList<ModeOption> = DEFAULT_MODE_OPTIONS.toObservableList()

    override fun created() {
        super.created()
        store = AppearanceStore(this)
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
                        title = "外观"
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
                            title = "深浅色模式",
                            summary = "决定APP整体的明暗观感",
                        )

                        AppearanceCard {
                            ModeSegmentedControl(ctx, ctx.store, ctx.modeOptions)
                        }

                        // ---------- 应用 ----------
                        ApplyThemeButton(ctx, ctx.store)
                    }
                }
            }
        }
    }
}

private data class ModeOption(
    val mode: SRThemeMode,
    val label: String,
)

private val DEFAULT_MODE_OPTIONS = listOf(
    ModeOption(SRThemeMode.SYSTEM, "跟随系统"),
    ModeOption(SRThemeMode.LIGHT, "浅色"),
    ModeOption(SRThemeMode.DARK, "深色"),
)

/** 分组标题；颜色在 attr 作用域内读取，换肤后能同步刷新 */
private fun ViewContainer<*, *>.SectionHeader(
    title: String,
    summary: String,
    marginTop: Float = 0f,
) {
    val pager = getPager() as BasePager
    Column {
        attr {
            margin(top = marginTop)
        }
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

/**
 * 外观页自用的卡片容器。
 *
 * 不复用 SRCard：它的描边色是在 body 构建期一次性捕获的，换肤后不会刷新；
 * 这里把取值放进 attr 作用域内。
 */
private fun ViewContainer<*, *>.AppearanceCard(init: () -> Unit) {
    val pager = getPager() as BasePager
    View {
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

/** 深浅色三选一分段控件 */
private fun ViewContainer<*, *>.ModeSegmentedControl(
    pager: BasePager,
    store: AppearanceStore,
    options: ObservableList<ModeOption>,
) {
    View {
        attr {
            flexDirectionRow()
            margin(12f)
            padding(4f)
            borderRadius(SRRadius.MEDIUM + 2f)
            backgroundColor(Color(pager.SRThemeColor.neutralChangeBackground))
        }

        vfor({ options }) { option ->
            View {
                attr {
                    flex(1f)
                    allCenter()
                    padding(top = 9f, bottom = 9f)
                    borderRadius(SRRadius.MEDIUM + 2f)
                    val selected = store.draftMode == option.mode
                    backgroundColor(
                        Color(
                            if (selected) {
                                pager.SRThemeColor.primaryContainer
                            } else {
                                pager.SRThemeColor.transparent
                            }
                        )
                    )
                }
                event {
                    click {
                        store.dispatch(AppearanceIntent.SelectMode(option.mode))
                    }
                }
                Text {
                    attr {
                        fontSize(14f)
                        fontWeightBold()
                        text(option.label)
                        color(
                            Color(
                                if (store.draftMode == option.mode) {
                                    pager.SRThemeColor.primary
                                } else {
                                    pager.SRThemeColor.secondaryText
                                }
                            )
                        )
                    }
                }
            }
        }
    }
}

/** 底部应用按钮：草稿与已应用一致时置灰禁用 */
private fun ViewContainer<*, *>.ApplyThemeButton(
    pager: BasePager,
    store: AppearanceStore,
) {
    View {
        attr {
            marginTop(24f)
            height(46f)
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
                    store.dispatch(AppearanceIntent.Apply)
                }
            }
        }
        Text {
            attr {
                fontSize(16f)
                fontWeightBold()
                text(if (store.isDirty) "应用主题" else "已是当前主题")
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
