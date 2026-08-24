package com.imcys.sairen.component

import com.imcys.sairen.theme.SRRadius
import com.imcys.sairen.theme.SRThemeColor
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.directives.vforIndex
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.TabItem
import com.tencent.kuikly.core.views.View

internal class SREqualTabsView<T> : ComposeView<SREqualTabsViewAttr<T>, SREqualTabsViewEvent<T>>() {

    private var currentIndex by observable(0)
    private var tabDataList by observableList<T>()

    override fun createEvent(): SREqualTabsViewEvent<T> = SREqualTabsViewEvent()

    override fun createAttr(): SREqualTabsViewAttr<T> = SREqualTabsViewAttr()

    override fun created() {
        super.created()
        tabDataList.clear()
        tabDataList.addAll(attr.dataList)
        currentIndex = attr.defaultIndex
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            val colors = ctx.SRThemeColor
            SRControlledTabs {
                attr {
                    height(30f)
                    equalItemWidth()
                    selectedIndex(ctx.currentIndex)
                    indicatorInTabItem {
                        View {
                            attr {
                                absolutePosition(left = 0f, right = 0f, bottom = 0f)
                                height(4f)
                                borderRadius(
                                    topLeft = SRRadius.SMALL,
                                    topRight = SRRadius.SMALL,
                                    bottomLeft = 0f,
                                    bottomRight = 0f
                                )
                                backgroundColor(colors.tabIndicator)
                            }
                        }
                    }
                }
                vforIndex({ ctx.tabDataList }) { tabItem, index, _ ->
                    TabItem {
                        attr {
                            allCenter()
                        }
                        event {
                            click {
                                ctx.currentIndex = index
                                ctx.event.onTabClick.invoke(tabItem)
                            }
                        }
                        View {
                            ctx.event.onTabItem(tabItem).invoke(this)
                        }
                    }
                }
            }
        }
    }
}

internal class SREqualTabsViewAttr<T> : ComposeAttr() {
    var dataList = emptyList<T>()
    var defaultIndex = 0
}

internal class SREqualTabsViewEvent<T> : ComposeEvent() {

    var onTabItem: (T) -> (ViewContainer<*, *>.() -> Unit) = { {} }
    var onTabClick: (T) -> Unit = { }
}

internal fun <T> ViewContainer<*, *>.SREqualTabs(init: SREqualTabsView<T>.() -> Unit) {
    addChild(SREqualTabsView(), init)
}
