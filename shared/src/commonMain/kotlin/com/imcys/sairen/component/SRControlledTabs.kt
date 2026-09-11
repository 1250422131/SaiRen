package com.imcys.sairen.component

import com.tencent.kuikly.core.base.Animation
import com.tencent.kuikly.core.base.Attr
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewConst
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.ViewRef
import com.tencent.kuikly.core.base.domChildren
import com.tencent.kuikly.core.collection.fastArrayListOf
import com.tencent.kuikly.core.layout.Frame
import com.tencent.kuikly.core.layout.undefined
import com.tencent.kuikly.core.layout.valueEquals
import com.tencent.kuikly.core.pager.IPagerLayoutEventObserver
import com.tencent.kuikly.core.views.DivView
import com.tencent.kuikly.core.views.ListAttr
import com.tencent.kuikly.core.views.ListEvent
import com.tencent.kuikly.core.views.ListView
import com.tencent.kuikly.core.views.TabItemView
import com.tencent.kuikly.core.views.View
import kotlin.math.max
import kotlin.math.min

/**
 * 可由业务选中索引驱动的 Tabs。
 *
 * Kuikly 官方 TabsView 仅通过 PageList 的 ScrollParams 更新选中项；这里沿用其指示器
 * 的位置和尺寸计算，补充 selectedIndex，以支持没有分页列表的普通点击场景。
 */
internal class SRControlledTabsView : ListView<SRControlledTabsAttr, SRControlledTabsEvent>(),
    IPagerLayoutEventObserver {

    private var indicatorViewRef: ViewRef<DivView>? = null
    private var contentViewFrame: Frame = Frame.zero
    private var didInitializeIndicator = false
    private var equalItemWidth = Float.NaN

    override fun createAttr() = SRControlledTabsAttr()

    override fun createEvent() = SRControlledTabsEvent()

    override fun didMoveToParentView() {
        super.didMoveToParentView()
        getPager().addPagerLayoutEventObserver(this)
    }

    override fun didRemoveFromParentView() {
        super.didRemoveFromParentView()
        getPager().removePagerLayoutEventObserver(this)
    }

    internal fun selectedIndexDidChange() {
        updateIndicatorPositionIfNeed(animate = didInitializeIndicator)
        scrollSelectedTabIntoViewIfNeed(animate = didInitializeIndicator)
    }

    /**
     * 滚动模式下，把选中项滚到可视区域（居中），参考官方 TabsView 的 setContentOffset 套路。
     * 仅在选中索引变化/首次布局时调用，用户手动滚动不会被拉回。
     */
    private fun scrollSelectedTabIntoViewIfNeed(animate: Boolean) {
        if (!attr.scrollable || contentViewFrame === Frame.zero) {
            return
        }
        val tabItems = contentView?.domChildren()?.filterIsInstance<TabItemView>() ?: return
        if (tabItems.isEmpty()) {
            return
        }
        val viewWidth = flexNode.layoutFrame.width
        if (viewWidth <= 0f || contentViewFrame.width <= viewWidth) {
            return
        }
        val selectedIndex = min(attr.selectedIndex, tabItems.lastIndex)
        val itemFrame = tabItems[selectedIndex].flexNode.layoutFrame
        val maxOffset = contentViewFrame.width - viewWidth
        // 目标：选中项中心对齐容器中心，并 clamp 到 [0, maxOffset]
        val targetOffset = min(
            max(itemFrame.x + itemFrame.width * 0.5f - viewWidth * 0.5f, 0f),
            maxOffset
        )
        if (targetOffset != curOffsetX) {
            setContentOffset(targetOffset, 0f, animate)
        }
    }

    private fun updateIndicatorPositionIfNeed(animate: Boolean = false) {
        if (contentViewFrame === Frame.zero) {
            return
        }
        val tabItems = contentView?.domChildren()?.filterIsInstance<TabItemView>() ?: fastArrayListOf()
        if (tabItems.isEmpty()) {
            indicatorViewRef?.view?.getViewAttr()?.visibility(false)
            return
        }

        indicatorViewRef?.view?.getViewAttr()?.visibility(true)
        val selectedIndex = min(attr.selectedIndex, tabItems.lastIndex)
        val selectedItem = tabItems[selectedIndex]
        val itemFrame = selectedItem.flexNode.layoutFrame
        val contentFrame = selectedItem.domChildren().firstOrNull()?.flexNode?.layoutFrame
        val indicatorLeft = itemFrame.x + (contentFrame?.x ?: 0f)
        val indicatorWidth = contentFrame?.width ?: itemFrame.width
        val indicatorAttr = indicatorViewRef?.view?.getViewAttr() ?: return
        if (animate) {
            indicatorAttr.setProp(Attr.StyleConst.ANIMATION, Animation.linear(0.2f).toString())
        }
        indicatorAttr.left(indicatorLeft)
        indicatorAttr.top(0f)
        indicatorAttr.size(indicatorWidth, contentViewFrame.height)
        indicatorViewRef?.view?.renderView?.setFrame(
            indicatorLeft,
            0f,
            indicatorWidth,
            contentViewFrame.height
        )
        if (animate) {
            indicatorAttr.setProp(Attr.StyleConst.ANIMATION, "")
        }
        didInitializeIndicator = true
    }

    private fun updateEqualItemWidthIfNeed(): Boolean {
        if (!attr.equalItemWidth || flexNode.layoutFrame.width <= 0f) {
            return false
        }
        val tabItems = contentView?.domChildren()?.filterIsInstance<TabItemView>() ?: return false
        if (tabItems.isEmpty()) {
            return false
        }
        val width = flexNode.layoutFrame.width / tabItems.size
        if (equalItemWidth == width) {
            return false
        }
        equalItemWidth = width
        tabItems.forEach { tabItem ->
            tabItem.getViewAttr().width(width)
        }
        return true
    }

    override fun willInit() {
        super.willInit()
        attr.flexDirectionRow()
        attr.bouncesEnable(false)
        attr.showScrollerIndicator(false)
    }

    override fun didInit() {
        super.didInit()
        attr.flexDirectionRow()
        // 滚动模式开启边界回弹；均分模式内容宽度=容器宽度，回弹会出现空隙，保持关闭
        attr.bouncesEnable(attr.scrollable)
        if (attr.flexNode!!.styleHeight.valueEquals(Float.undefined)) {
            error("SRControlledTabs needs setup height")
        }
        attr.indicatorCreator?.also { creator ->
            View {
                ref { this@SRControlledTabsView.indicatorViewRef = it }
                attr {
                    absolutePosition(top = 0f, left = 0f)
                    visibility(false)
                }
                creator.invoke(this)
            }
        }
    }

    override fun onPagerWillCalculateLayoutFinish() = Unit

    override fun onPagerCalculateLayoutFinish() = Unit

    override fun onPagerDidLayout() {
        if (updateEqualItemWidthIfNeed()) {
            return
        }
        contentView?.flexNode?.let { flexNode ->
            if (flexNode.layoutFrame != contentViewFrame) {
                val isFirstLayout = !didInitializeIndicator
                contentViewFrame = flexNode.layoutFrame
                updateIndicatorPositionIfNeed()
                if (isFirstLayout) {
                    // 首次布局：defaultIndex 不为 0 时初始滚动位置也要到位
                    scrollSelectedTabIntoViewIfNeed(animate = false)
                }
            }
        }
    }

    override fun viewName(): String = ViewConst.TYPE_LIST
}

internal class SRControlledTabsAttr : ListAttr() {
    internal var selectedIndex = 0
    internal var indicatorCreator: ViewBuilder? = null
    internal var equalItemWidth = false
    internal var scrollable = false

    fun selectedIndex(index: Int) {
        val normalizedIndex = max(index, 0)
        if (selectedIndex == normalizedIndex) {
            return
        }
        selectedIndex = normalizedIndex
        (view() as? SRControlledTabsView)?.selectedIndexDidChange()
    }

    fun indicatorInTabItem(creator: ViewBuilder) {
        indicatorCreator = creator
    }

    fun equalItemWidth() {
        equalItemWidth = true
    }

    /*
     * 横向滚动模式：TabItem 宽度自适应内容，超出可横向滚动，选中项自动滚动到可视区域
     */
    fun scrollMode() {
        scrollable = true
    }
}

internal class SRControlledTabsEvent : ListEvent()

internal fun ViewContainer<*, *>.SRControlledTabs(init: SRControlledTabsView.() -> Unit) {
    addChild(SRControlledTabsView(), init)
}
