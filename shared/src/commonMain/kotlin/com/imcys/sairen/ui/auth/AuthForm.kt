package com.imcys.sairen.ui.auth

import com.imcys.sairen.base.BasePager
import com.imcys.sairen.theme.SRRadius
import com.tencent.kuikly.core.base.Border
import com.tencent.kuikly.core.base.BorderStyle
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.views.Input
import com.tencent.kuikly.core.views.LengthLimitType
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.tencent.kuikly.core.views.layout.Row

internal fun ViewContainer<*, *>.AuthForm(
    pager: BasePager,
    title: String,
    submitText: String,
    submittingText: String,
    footerText: String,
    footerActionText: String,
    state: () -> AuthState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onFooterAction: () -> Unit,
) {
    Scroller {
        attr {
            flex(1f)
            showScrollerIndicator(false)
        }

        View {
            attr {
                alignItemsCenter()
                padding(left = 20f, right = 20f, top = 42f, bottom = 30f)
            }

            View {
                attr {
                    width(minOf(pager.pagerData.pageViewWidth - 40f, 420f))
                }

                Text {
                    attr {
                        text(title)
                        fontSize(28f)
                        fontWeightBold()
                        color(Color(pager.SRThemeColor.primaryText))
                    }
                }

                Text {
                    attr {
                        text("用户名")
                        marginTop(32f)
                        marginBottom(8f)
                        fontSize(14f)
                        color(Color(pager.SRThemeColor.secondaryText))
                    }
                }
                View {
                    attr {
                        padding(left = 10f, right = 10f)
                        borderRadius(SRRadius.MEDIUM)
                        border(Border(1f, BorderStyle.SOLID, Color(pager.SRThemeColor.divider)))
                        backgroundColor(Color(pager.SRThemeColor.stockInfoCard))
                    }
                    Input {
                        attr {
                            height(48f)
                            color(Color(pager.SRThemeColor.primaryText))
                            fontSize(16f)
                            placeholder("请输入用户名")
                            placeholderColor(Color(pager.SRThemeColor.secondaryText))
                            maxTextLength(32, LengthLimitType.CHARACTER)
                            returnKeyTypeNext()
                        }
                        event {
                            textDidChange { onUsernameChange(it.text) }
                        }
                    }
                }

                Text {
                    attr {
                        text("密码")
                        marginTop(20f)
                        marginBottom(8f)
                        fontSize(14f)
                        color(Color(pager.SRThemeColor.secondaryText))
                    }
                }

                View {
                    attr {
                        padding(left = 10f, right = 10f)
                        borderRadius(SRRadius.MEDIUM)
                        border(Border(1f, BorderStyle.SOLID, Color(pager.SRThemeColor.divider)))
                        backgroundColor(Color(pager.SRThemeColor.stockInfoCard))
                    }
                    Input {
                        attr {
                            height(48f)
                            color(Color(pager.SRThemeColor.primaryText))
                            fontSize(16f)
                            placeholder("请输入密码")
                            placeholderColor(Color(pager.SRThemeColor.secondaryText))
                            maxTextLength(72, LengthLimitType.CHARACTER)
                            keyboardTypePassword()
                            returnKeyTypeDone()
                            autoHideKeyboardOnImeAction(true)
                        }
                        event {
                            textDidChange { onPasswordChange(it.text) }
                            inputReturn { onSubmit() }
                        }
                    }
                }

                vif({ state().errorMessage.isNotEmpty() }) {
                    Text {
                        attr {
                            text(state().errorMessage)
                            marginTop(12f)
                            fontSize(13f)
                            color(Color(pager.SRThemeColor.error))
                        }
                    }
                }

                View {
                    attr {
                        height(48f)
                        marginTop(24f)
                        borderRadius(SRRadius.MEDIUM)
                        allCenter()
                        backgroundColor(
                            Color(
                                if (state().isSubmitting) {
                                    pager.SRThemeColor.secondaryText
                                } else {
                                    pager.SRThemeColor.primary
                                }
                            )
                        )
                    }
                    event {
                        click { onSubmit() }
                    }
                    Text {
                        attr {
                            text(if (state().isSubmitting) submittingText else submitText)
                            fontSize(16f)
                            fontWeightBold()
                            color(Color.WHITE)
                        }
                    }
                }

                Row {
                    attr {
                        marginTop(22f)
                        justifyContentCenter()
                        alignItemsCenter()
                    }
                    Text {
                        attr {
                            text(footerText)
                            fontSize(14f)
                            color(Color(pager.SRThemeColor.secondaryText))
                        }
                    }
                    Text {
                        attr {
                            text(footerActionText)
                            marginLeft(6f)
                            fontSize(14f)
                            fontWeightBold()
                            color(Color(pager.SRThemeColor.primary))
                        }
                        event {
                            click { onFooterAction() }
                        }
                    }
                }
            }
        }
    }
}
