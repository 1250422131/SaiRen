package com.imcys.sairen.ui.auth

import com.imcys.sairen.base.BasePager
import com.imcys.sairen.component.SRNavigationBar
import com.imcys.sairen.core.common.ext.acquireRouterModule
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.views.View

@Page("register")
internal class RegisterPage : BasePager() {
    private val store by lazy(LazyThreadSafetyMode.NONE) {
        AuthStore.register(this) { acquireRouterModule().closePage() }
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
                        title = "注册"
                        enabledBack = true
                    }
                }

                AuthForm(
                    pager = ctx,
                    title = "创建账户",
                    submitText = "注册",
                    submittingText = "正在注册",
                    footerText = "已有账户？",
                    footerActionText = "返回登录",
                    state = { ctx.store.state },
                    onUsernameChange = { ctx.store.dispatch(AuthIntent.UpdateUsername(it)) },
                    onPasswordChange = { ctx.store.dispatch(AuthIntent.UpdatePassword(it)) },
                    onSubmit = { ctx.store.dispatch(AuthIntent.Submit) },
                    onFooterAction = { ctx.acquireRouterModule().closePage() },
                )
            }
        }
    }
}
