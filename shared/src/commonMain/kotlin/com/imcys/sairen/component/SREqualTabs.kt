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
                    if (ctx.attr.scrollable) scrollMode() else equalItemWidth()
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
                            if (ctx.attr.scrollable) {
                                // 滚动模式：宽度自适应内容，间距用 margin、留白用 padding（指示条随内容宽）
                                if (index > 0) marginLeft(ctx.attr.scrollItemSpacing)
                                paddingLeft(ctx.attr.scrollItemPadding)
                                paddingRight(ctx.attr.scrollItemPadding)
                            }
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

    /** 横向滚动模式（默认 false = 均分宽度） */
    var scrollable = false
    /** 滚动模式下 Tab 之间的间距 */
    var scrollItemSpacing = 12f
    /** 滚动模式下 Tab 左右内边距 */
    var scrollItemPadding = 8f

    /*
     * 切换为横向滚动模式：Tab 宽度自适应内容、可横向滚动，选中项自动滚动到可视区域（居中）
     */
    fun scrollMode() {
        scrollable = true
    }

    /*
     * 滚动模式下 Tab 之间的间距（默认 12）
     */
    fun scrollItemSpacing(spacing: Float) {
        scrollItemSpacing = spacing
    }

    /*
     * 滚动模式下 Tab 左右内边距（默认 8，指示条宽度跟随内容+留白）
     */
    fun scrollItemPadding(padding: Float) {
        scrollItemPadding = padding
    }
}

internal class SREqualTabsViewEvent<T> : ComposeEvent() {

    var onTabItem: (T) -> (ViewContainer<*, *>.() -> Unit) = { {} }
    var onTabClick: (T) -> Unit = { }
}

internal fun <T> ViewContainer<*, *>.SREqualTabs(init: SREqualTabsView<T>.() -> Unit) {
    addChild(SREqualTabsView(), init)
}
