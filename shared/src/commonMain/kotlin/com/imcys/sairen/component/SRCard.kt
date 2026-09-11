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
 *
 * 描边色必须在 attr 作用域内读取，否则切主题后不会刷新（attr 块只在依赖变化时重跑）。
 */
internal fun ViewContainer<*, *>.SRCard(init: DivView.() -> Unit = {}) {
    val pager = getPager() as BasePager
    View {
        attr {
            border(
                Border(
                    lineWidth = 1f,
                    lineStyle = BorderStyle.SOLID,
                    color = Color(pager.SRThemeColor.divider),
                )
            )
            borderRadius(SRRadius.LARGE)
        }
        init()
    }
}
