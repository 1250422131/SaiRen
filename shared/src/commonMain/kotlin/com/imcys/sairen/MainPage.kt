package com.imcys.sairen

import com.imcys.sairen.base.BasePager
import com.imcys.sairen.component.SRNavigationBar
import com.imcys.sairen.component.SRPageList
import com.imcys.sairen.component.SRPageListView
import com.imcys.sairen.component.SRTabs
import com.imcys.sairen.component.SRTabsView
import com.imcys.sairen.model.Market
import com.imcys.sairen.model.marketList
import com.imcys.sairen.theme.SRColor
import com.imcys.sairen.ui.home.StockList
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewRef
import com.tencent.kuikly.core.base.attr.ImageUri
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.View

@Page("router", supportInLocal = true)
internal class MainPage : BasePager() {

    private var tabDataList by observableList<Market>()
    private var tabsRef: ViewRef<SRTabsView<Market>>? = null
    private var pageListRef: ViewRef<SRPageListView<Market>>? = null

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                flex(1f)
                backgroundColor(Color(ctx.SRThemeColor.pageBackground))
            }

            // 导航栏
            SRNavigationBar {
                attr {
                    title = "塞壬"
                    icon = ImageUri.pageAssets("logo.png")
                }

            }

            // 顶层
            SRTabs {
                ref {
                    ctx.tabsRef = it
                }
                attr {
                    margin(right = 20f, left = 10f)
                    dataList = marketList
                    defaultIndex = 0
                }
                event {
                    onTitle = { it.name }
                    onTabClick = { item ->
                        val index = marketList.indexOf(item)
                        ctx.pageListRef?.view?.scrollToPageIndex(index, true)
                    }
                }

            }

            // 分页
            SRPageList {
                ref {
                    ctx.pageListRef = it
                }
                attr {
                    dataList = marketList
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

    override fun created() {
        super.created()
        tabDataList.addAll(marketList)
    }


}
