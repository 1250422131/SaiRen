package com.imcys.sairen.ui.settings

import com.imcys.sairen.base.BasePager
import com.imcys.sairen.component.SRCard
import com.imcys.sairen.component.SRDivider
import com.imcys.sairen.component.SRIcon
import com.imcys.sairen.component.SRNavigationBar
import com.imcys.sairen.core.common.ext.acquireRouterModule
import com.imcys.sairen.core.common.ext.toCommonAssets
import com.imcys.sairen.core.common.ext.toPageAssets
import com.imcys.sairen.theme.SRThemeColor
import com.imcys.sairen.theme.SRThemeMode
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.attr.ImageUri
import com.tencent.kuikly.core.views.Image
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.tencent.kuikly.core.views.layout.Column
import com.tencent.kuikly.core.views.layout.Row
import com.tencent.kuikly.core.views.layout.RowView

@Page("settings")
internal class SettingsPage : BasePager() {

    private lateinit var store: SettingsStore

    override fun created() {
        super.created()
        store = SettingsStore(this)
        store.dispatch(SettingsIntent.Refresh)
    }

    override fun pageDidAppear() {
        super.pageDidAppear()
        store.dispatch(SettingsIntent.Refresh)
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
                        title = "设置"
                        enabledBack = true
                    }
                }

                Scroller {
                    attr {
                        flex(1f)
                    }
                    Column {
                        attr {
                            padding(left = 20f, top = 10f, right = 20f)
                        }

                        Text {
                            attr {
                                fontSize(15f)
                                fontWeightBold()
                                color(ctx.SRThemeColor.primaryText)
                                text("个人")
                            }
                        }

                        SRCard {
                            attr {
                                marginTop(12f)
                            }
                            SettingRow(
                                icon = "sunny_24dp.svg".toPageAssets(),
                                title = "外观",
                                summary = { ctx.appearanceSummary() },
                                onClick = {
                                    ctx.acquireRouterModule().openPage("appearance")
                                },
                            )

                            SRDivider {
                                attr { margin(left = 30f, right = 30f) }
                            }

                            SettingRow(
                                icon = "build_24dp.svg".toPageAssets(),
                                title = "配置",
                                summary = { ctx.serverConfigSummary() },
                                onClick = {
                                    ctx.acquireRouterModule().openPage("config")
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    /** 当前主题摘要，如「跟随系统」 */
    private fun appearanceSummary(): String {
        return when (store.themeMode) {
            SRThemeMode.SYSTEM -> "跟随系统"
            SRThemeMode.LIGHT -> "浅色"
            SRThemeMode.DARK -> "深色"
        }
    }

    /** 配置行摘要：当前生效的塞壬后台地址 */
    private fun serverConfigSummary(): String {
        return store.serverUrl
    }
}

private fun ViewContainer<*, *>.SettingRow(
    icon: ImageUri? = null,
    title: String,
    summary: () -> String,
    init: RowView.() -> Unit = {},
    preview: ViewContainer<*, *>.() -> Unit = {},
    onClick: () -> Unit,
) {
    val pager = getPager() as BasePager
    Row {
        attr {
            alignItemsCenter()
            padding(left = 16f, top = 10f, right = 12f, bottom = 10f)
        }
        event {
            click { onClick() }
        }
        init()

        icon?.let {
            SRIcon {
                attr {
                    marginRight(15f)
                    size(20f,20f)
                    src(icon)
                    tintColor(Color(pager.SRThemeColor.primaryText))
                }
            }
        }

        Column {
            attr {
                flex(1f)
            }
            Text {
                attr {
                    fontSize(16f)
                    color(pager.SRThemeColor.primaryText)
                    text(title)
                }
            }
            Text {
                attr {
                    marginTop(4f)
                    fontSize(13f)
                    color(pager.SRThemeColor.secondaryText)
                    text(summary())
                }
            }
        }

        Row {
            attr {
                alignItemsCenter()
            }
            preview()
            Image {
                attr {
                    marginLeft(12f)
                    size(20f, 20f)
                    src("chevron_right_24dp.svg".toCommonAssets())
                    tintColor(Color(pager.SRThemeColor.secondaryText))
                }
            }
        }
    }
}
