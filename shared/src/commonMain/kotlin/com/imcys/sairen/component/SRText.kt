package com.imcys.sairen.component

import com.imcys.sairen.base.BasePager
import com.tencent.kuikly.core.views.TextConst
import com.tencent.kuikly.core.views.TextView

internal class SRTextView : TextView() {

    override fun didInit() {
        super.didInit()
        val attr = getViewAttr()
        if (attr.getProp(TextConst.TEXT_COLOR) == null) {
            bindValueChange({ (getPager() as BasePager).SRThemeColor.primaryText }){
                attr.color(it as Long)
            }
        }
        if (attr.getProp(TextConst.FONT_FAMILY) == null) {
            attr.fontFamily(DEFAULT_FONT_FAMILY)
        }
    }

    override fun didSetProp(propKey: String, propValue: Any) {
        if (propKey == TextConst.FONT_WEIGHT) {
            val attr = getViewAttr()
            val fontFamily = if ((propValue.toString().toIntOrNull() ?: 500) >= 600) {
                BOLD_FONT_FAMILY
            } else {
                MEDIUM_FONT_FAMILY
            }
            attr.fontFamily(fontFamily)
        }
        super.didSetProp(propKey, propValue)
    }

    private companion object {
        const val MEDIUM_FONT_FAMILY = "MiSans-Medium"
        const val BOLD_FONT_FAMILY = "MiSans-Bold"
        const val DEFAULT_FONT_FAMILY = MEDIUM_FONT_FAMILY
    }
}
