package com.imcys.sairen.component

import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.PageListAttr
import com.tencent.kuikly.core.views.PageListEvent
import com.tencent.kuikly.core.views.PageListView
import com.tencent.kuikly.core.views.View

internal class SRPageListView<T> :
    PageListView<SRPageListAttr<T>, SRPageListEvent<T>>() {

    private var pageDataList by observableList<T>()

    override fun createAttr(): SRPageListAttr<T> {
        return SRPageListAttr()
    }

    override fun createEvent(): SRPageListEvent<T> {
        return SRPageListEvent()
    }

    override fun didInit() {
        super.didInit()
        pageDataList.clear()
        pageDataList.addAll(attr.dataList)
        val ctx = this
        vfor({ ctx.pageDataList }) { item ->
            View {
                ctx.event.onCompose(item).invoke(this)
            }
        }
    }
}

internal class SRPageListAttr<T> : PageListAttr() {
    var dataList = emptyList<T>()
}

internal class SRPageListEvent<T> : PageListEvent() {
    var onCompose: (T) -> (ViewContainer<*, *>.() -> Unit) = { { } }
}

internal fun <T> ViewContainer<*, *>.SRPageList(init: SRPageListView<T>.() -> Unit) {
    addChild(SRPageListView(), init)
}
