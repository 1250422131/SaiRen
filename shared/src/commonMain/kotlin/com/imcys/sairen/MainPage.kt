package com.imcys.sairen

import com.imcys.sairen.base.BasePager
import com.imcys.sairen.core.common.ext.acquireRouterModule
import com.imcys.sairen.core.network.toKJSONObject
import com.imcys.sairen.ui.home.HomeScreen
import com.imcys.sairen.ui.home.HomeScreenIntent
import com.imcys.sairen.ui.home.HomeScreenStore
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder

@Page("router", supportInLocal = true)
internal class MainPage : BasePager() {

    private val homeScreenStore = HomeScreenStore(this)

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                flex(1f)
                backgroundColor(Color(ctx.SRThemeColor.pageBackground))
            }

            HomeScreen {
                attr {
                    store = ctx.homeScreenStore
                }
                event {
                    onIntent = { intent -> ctx.handleHomeIntent(intent) }
                }
            }

        }
    }

    private fun handleHomeIntent(intent: HomeScreenIntent) {
        when (intent) {
            is HomeScreenIntent.OpenStockDetail -> {
                acquireRouterModule().openPage("stock_detail", intent.stock.toKJSONObject())
            }

            else -> homeScreenStore.dispatch(intent)
        }
    }

}
