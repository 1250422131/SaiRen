package com.imcys.sairen.component

import com.imcys.sairen.theme.SRThemeColor
import com.imcys.sairen.theme.SRRadius
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.ScrollParams
import com.tencent.kuikly.core.views.TabItem
import com.tencent.kuikly.core.views.Tabs
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

internal class SRTabsView<T> : ComposeView<SRTabsViewAttr<T>, SRTabsViewEvent<T>>() {

    private var currentIndex by observable(0)
    private var tabDataList by observableList<T>()
    private var scrollParamsState by observable<ScrollParams?>(null)

    override fun createEvent(): SRTabsViewEvent<T> {
        return SRTabsViewEvent()
    }

    override fun createAttr(): SRTabsViewAttr<T> {
        return SRTabsViewAttr()
    }

    override fun created() {
        super.created()
        tabDataList.clear()
        tabDataList.addAll(attr.dataList)
        currentIndex = attr.defaultIndex
    }


    fun updateScrollParams(params: ScrollParams?) {
        scrollParamsState = params
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            val colors = ctx.SRThemeColor
            Tabs {
                attr {
                    indicatorAlignCenter()
                    height(30f)
                    defaultInitIndex(ctx.currentIndex)
                    ctx.scrollParamsState?.also {
                        scrollParams(it)
                    }
                    indicatorInTabItem {
                        View {
                            attr {
                                absolutePosition(left = 15f, right = 15f, bottom = 0f)
                                height(4f)
                                borderRadius(SRRadius.SMALL)
                                backgroundColor(colors.tabIndicator)
                            }
                        }
                    }
                }

                vfor({ ctx.tabDataList }) { tabItem ->
                    TabItem { state ->
                        attr {
                            allCenter()
                        }
                        event {
                            click {
                                ctx.event.onTabClick.invoke(tabItem)
                            }
                        }

                        Text {
                            attr {
                                fontWeightBold()
                                margin(left = 12f, right = 8f)
                                text(ctx.event.onTitle.invoke(tabItem))
                                fontSize(17f)
                                color(colors.primaryText)
                            }
                        }
                    }
                }

            }
        }
    }
}

internal class SRTabsViewAttr<T> : ComposeAttr() {
    var dataList = emptyList<T>()
    var defaultIndex = 0
}

internal class SRTabsViewEvent<T> : ComposeEvent() {
    var onTitle: (T) -> String = { "" }
    var onTabClick: (T) -> Unit = { }
}

internal fun <T> ViewContainer<*, *>.SRTabs(init: SRTabsView<T>.() -> Unit) {
    addChild(SRTabsView(), init)
}
