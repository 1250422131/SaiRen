package com.imcys.sairen.ui.auth

import com.imcys.sairen.base.BasePager
import com.imcys.sairen.component.SRNavigationBar
import com.imcys.sairen.core.common.auth.AuthPreferences
import com.imcys.sairen.core.common.ext.acquireRouterModule
import com.imcys.sairen.core.common.ext.acquireSharedPreferencesModule
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.views.View

@Page("login")
internal class LoginPage : BasePager() {
    private val store by lazy(LazyThreadSafetyMode.NONE) {
        AuthStore.login(this) { acquireRouterModule().closePage() }
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
                        title = "登录"
                        enabledBack = true
                    }
                    event {
                        callBack = {
                            ctx.acquireRouterModule().closePage()
                            ctx.acquireRouterModule().openPage("router")
                        }
                    }
                }

                AuthForm(
                    pager = ctx,
                    title = "欢迎回来",
                    submitText = "登录",
                    submittingText = "正在登录",
                    footerText = "还没有账户？",
                    footerActionText = "注册",
                    state = { ctx.store.state },
                    onUsernameChange = { ctx.store.dispatch(AuthIntent.UpdateUsername(it)) },
                    onPasswordChange = { ctx.store.dispatch(AuthIntent.UpdatePassword(it)) },
                    onSubmit = { ctx.store.dispatch(AuthIntent.Submit) },
                    onFooterAction = { ctx.acquireRouterModule().openPage("register") },
                )
            }
        }
    }

    override fun pageDidAppear() {
        super.pageDidAppear()
        val preferences = acquireSharedPreferencesModule()
        val token = preferences.getString(AuthPreferences.TOKEN)
        val isLoggedIn = preferences.getString(AuthPreferences.IS_LOGGED_IN) == "true"
        if (token.isNotBlank() && isLoggedIn) {
            acquireRouterModule().closePage()
        }
    }
}
