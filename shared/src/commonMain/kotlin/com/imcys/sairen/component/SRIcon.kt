package com.imcys.sairen.component

import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.views.Image
import com.tencent.kuikly.core.views.ImageView

internal fun ViewContainer<*, *>.SRIcon(init: ImageView.() -> Unit) {
    Image {
        attr {
            size(24f, 24f)
        }
        init()
    }
}
