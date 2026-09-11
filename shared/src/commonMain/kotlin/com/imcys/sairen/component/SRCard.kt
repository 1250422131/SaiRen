package com.imcys.sairen.component

import com.imcys.sairen.base.BasePager
import com.imcys.sairen.theme.SRRadius
import com.tencent.kuikly.core.base.Border
import com.tencent.kuikly.core.base.BorderStyle
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.views.DivView
import com.tencent.kuikly.core.views.View

/**
 * 带默认边框和圆角的卡片容器。
 */
internal fun ViewContainer<*, *>.SRCard(init: DivView.() -> Unit = {}) {
    val themeColors = (getPager() as BasePager).SRThemeColor
    View {
        attr {
            border(
                Border(
                    lineWidth = 1f,
                    lineStyle = BorderStyle.SOLID,
                    color = Color(themeColors.divider),
                )
            )
            borderRadius(SRRadius.LARGE)
        }
        init()
    }
}
