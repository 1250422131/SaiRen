package com.imcys.sairen.ui.settings

import com.imcys.sairen.base.BasePager
import com.imcys.sairen.component.SRCard
import com.imcys.sairen.component.SRDivider
import com.imcys.sairen.component.SRIcon
import com.imcys.sairen.component.SRNavigationBar
import com.imcys.sairen.core.common.ext.toCommonAssets
import com.imcys.sairen.core.common.ext.toPageAssets
import com.imcys.sairen.theme.SRRadius
import com.imcys.sairen.theme.SRThemeColor
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.attr.ImageUri
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.views.Image
import com.tencent.kuikly.core.views.Modal
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.tencent.kuikly.core.views.layout.Column
import com.tencent.kuikly.core.views.layout.Row
import com.tencent.kuikly.core.views.layout.RowView

@Page("settings")
internal class SettingsPage : BasePager() {

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
                            AppearanceSettingRow(
                                icon = "sunny_24dp.svg".toPageAssets(),
                                title = "外观",
                                summary = "个性化的APP外观",
                                init = {
                                }
                            ) {}

                            SRDivider {
                                attr { margin(left = 30f, right = 30f) }
                            }

                            AppearanceSettingRow(
                                icon = "build_24dp.svg".toPageAssets(),
                                title = "配置",
                                summary = "调整APP内接口行为",
                                init = {
                                }
                            ) {}
                        }
                    }
                }
            }
        }
    }
}

private fun ViewContainer<*, *>.AppearanceSettingRow(
    icon: ImageUri? = null,
    title: String,
    summary: String,
    init: RowView.() -> Unit = {},
    preview: ViewContainer<*, *>.() -> Unit = {},
) {
    val pager = getPager() as BasePager
    Row {
        attr {
            alignItemsCenter()
            padding(left = 16f, top = 10f, right = 12f, bottom = 10f)
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
                    text(summary)
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
