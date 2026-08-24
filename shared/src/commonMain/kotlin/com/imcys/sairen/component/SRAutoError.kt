package com.imcys.sairen.component

import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer

internal class SRAutoErrorView: ComposeView<SRAutoErrorViewAttr, SRAutoErrorViewEvent>() {
    
    override fun createEvent(): SRAutoErrorViewEvent {
        return SRAutoErrorViewEvent()
    }

    override fun createAttr(): SRAutoErrorViewAttr {
        return SRAutoErrorViewAttr()
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            
        }
    }
}


internal class SRAutoErrorViewAttr : ComposeAttr() {

}

internal class SRAutoErrorViewEvent : ComposeEvent() {
    
}

internal fun ViewContainer<*, *>.SRAutoError(init: SRAutoErrorView.() -> Unit) {
    addChild(SRAutoErrorView(), init)
}