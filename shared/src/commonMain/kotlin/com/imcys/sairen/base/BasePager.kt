package com.imcys.sairen.base

import com.imcys.sairen.component.SRTextView
import com.imcys.sairen.core.common.ext.acquireRouterModule
import com.imcys.sairen.core.common.ext.loginErrorChannel
import com.imcys.sairen.core.common.module.BridgeModule
import com.imcys.sairen.theme.SRAppTheme
import com.imcys.sairen.theme.SRThemeColors
import com.tencent.kuikly.core.base.ViewConst
import com.tencent.kuikly.core.coroutines.launch
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.pager.IViewCreator
import com.tencent.kuikly.core.module.Module
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.reactive.handler.observable

internal abstract class BasePager : Pager() {
    var nightModel: Boolean? by observable(null)
    private val appTheme = SRAppTheme.Default
    var SRThemeColor: SRThemeColors by observable(appTheme.colorPalette(isNightMode = false))
        private set

    override fun createExternalModules(): Map<String, Module>? {
        val externalModules = hashMapOf<String, Module>()
        externalModules[BridgeModule.MODULE_NAME] = BridgeModule()
        return externalModules
    }

    override fun created() {
        super.created()
        registerViewCreator(ViewConst.TYPE_TEXT_CLASS_NAME, object : IViewCreator {
            override fun createView() = SRTextView()
        })
        isNightMode()
        bindEvent()
    }

    private fun bindEvent() {
        lifecycleScope.launch {
            // 处理登录异常
            loginErrorChannel.collect {
                acquireRouterModule().openPage("login")
                acquireRouterModule().closePage()
            }
        }
    }

    override fun themeDidChanged(data: JSONObject) {
        super.themeDidChanged(data)
        nightModel = data.optBoolean(IS_NIGHT_MODE_KEY).also {
            SRThemeColor = appTheme.colorPalette(it)
        }
    }

    // 是否为夜间模式
    override fun isNightMode(): Boolean {
        if (nightModel == null) {
            nightModel = pageData.params.optBoolean(IS_NIGHT_MODE_KEY)
            SRThemeColor = appTheme.colorPalette(nightModel!!)
        }
        return nightModel!!
    }

    // 不开启调试UI模式
    override fun debugUIInspector(): Boolean {
        return false
    }

    companion object {
        const val IS_NIGHT_MODE_KEY = "isNightMode"
    }

}
