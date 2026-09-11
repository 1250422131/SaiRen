package com.imcys.sairen.ui.home

import com.imcys.sairen.component.SRNavigationBar
import com.imcys.sairen.component.SRPageList
import com.imcys.sairen.component.SRPageListView
import com.imcys.sairen.component.SRTabs
import com.imcys.sairen.component.SRTabsView
import com.imcys.sairen.component.screen.home.StockList
import com.imcys.sairen.core.common.ext.acquireRouterModule
import com.imcys.sairen.core.common.ext.toCommonAssets
import com.imcys.sairen.model.Market
import com.imcys.sairen.theme.SRThemeColor
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.ViewRef
import com.tencent.kuikly.core.base.attr.ImageUri
import com.tencent.kuikly.core.views.Image
import com.tencent.kuikly.core.views.View

internal class HomeScreenView: ComposeView<HomeScreenViewAttr, HomeScreenViewEvent>() {

    private val store: HomeScreenStore
        get() = attr.store ?: error("HomeScreenStore is required")
    private var tabsRef: ViewRef<SRTabsView<Market>>? = null
    private var pageListRef: ViewRef<SRPageListView<Market>>? = null

    override fun createEvent(): HomeScreenViewEvent {
        return HomeScreenViewEvent()
    }

    override fun createAttr(): HomeScreenViewAttr {
        return HomeScreenViewAttr()
    }


    override fun body(): ViewBuilder {
        val ctx = this
        return {
            // 导航栏
            SRNavigationBar {
                attr {
                    title = "塞壬"
                    icon = ImageUri.pageAssets("logo.png")
                    action = {
                        Image {
                            attr {
                                size(30f,30f)
                                src("ai_action.gif".toCommonAssets())
                                marginRight(5f)
                            }
                            event {
                                click {
                                    ctx.acquireRouterModule().openPage("agent_chat")
                                }
                            }
                        }
                        Image {
                            attr {
                                size(24f,24f)
                                src("settings_24dp.svg".toCommonAssets())
                                tintColor(Color(ctx.SRThemeColor.primary))
                            }
                            event {
                                click {
                                    ctx.acquireRouterModule().openPage("settings")
                                }
                            }
                        }
                    }
                }
            }

            // 顶层
            SRTabs {
                ref {
                    ctx.tabsRef = it
                }
                attr {
                    margin(right = 20f, left = 10f)
                    dataList = ctx.store.markets
                    defaultIndex = 0

                }
                event {
                    onTitle = { it.name }
                    onTabClick = { item ->
                        val index = ctx.store.markets.indexOf(item)
                        ctx.event.onIntent(HomeScreenIntent.SelectMarket(index))
                    }
                }

            }


            // 分页
            SRPageList {
                ref {
                    ctx.pageListRef = it
                }
                attr {
                    dataList = ctx.store.markets
                    flexDirectionRow()
                    pageItemWidth(pagerData.pageViewWidth - 20)
                    pageItemHeight(pagerData.pageViewHeight - pagerData.statusBarHeight - 53 - 50)
                    defaultPageIndex(0)
                    offscreenPageLimit(1)
                }
                event {
                    onCompose = { market ->
                        {
                            View {
                                attr {
                                    flex(1f)
                                    padding(left = 20f)
                                }
                                StockList {
                                    attr {
                                        flex(1f)
                                        marketParams = market.params
                                        store = ctx.store
                                    }
                                    event {
                                        onIntent = { intent -> ctx.event.onIntent(intent) }
                                    }
                                }
                            }

                        }
                    }
                    scroll { params ->
                        ctx.tabsRef?.view?.updateScrollParams(params)
                    }
                }
            }
        }
    }

    override fun didInit() {
        super.didInit()
        bindValueChange({ store.selectedMarketIndex }) { value ->
            pageListRef?.view?.scrollToPageIndex(value as Int, true)
        }
    }

}


internal class HomeScreenViewAttr : ComposeAttr() {
    var store: HomeScreenStore? = null
}

internal class HomeScreenViewEvent : ComposeEvent() {
    var onIntent: (HomeScreenIntent) -> Unit = {}
}

internal fun ViewContainer<*, *>.HomeScreen(init: HomeScreenView.() -> Unit) {
    addChild(HomeScreenView(), init)
}
