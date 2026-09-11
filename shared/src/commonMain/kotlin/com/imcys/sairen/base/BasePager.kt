package com.imcys.sairen.base

import com.imcys.sairen.component.SRTextView
import com.imcys.sairen.core.common.ext.acquireBridgeModule
import com.imcys.sairen.core.common.ext.acquireRouterModule
import com.imcys.sairen.core.common.ext.acquireSharedPreferencesModule
import com.imcys.sairen.core.common.ext.loginErrorChannel
import com.imcys.sairen.core.common.module.BridgeModule
import com.imcys.sairen.theme.SRAppTheme
import com.imcys.sairen.theme.SRThemeColors
import com.imcys.sairen.theme.SRThemeConfig
import com.imcys.sairen.theme.SRThemeMode
import com.imcys.sairen.theme.SRThemeRuntime
import com.tencent.kuikly.core.base.ViewConst
import com.tencent.kuikly.core.coroutines.launch
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.pager.IViewCreator
import com.tencent.kuikly.core.module.Module
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.reactive.ObservableThreadSafetyMode
import com.tencent.kuikly.core.reactive.handler.observable

internal abstract class BasePager : Pager() {

    private val appTheme = SRAppTheme.Default

    /**
     * 注意：这里必须用 PagerScope.observable(scope) 这一版。
     *
     * 老的 `observable(init)` 在写入时用 `BridgeManager.currentPageId` 定位 observer，
     * 跨页面广播主题时会写到「当前页」的监听表里，导致栈内旧页面收不到通知。
     * 带 scope 的版本会把 currentPageId 临时切到属性所属页面，通知才能落到正确的页面。
     */
    private val reactiveMode = ObservableThreadSafetyMode.NONE

    /** 系统当前是否深色（由宿主端通过 pageData / themeDidChanged 同步） */
    var systemNightMode: Boolean by observable(reactiveMode, false)
        private set

    /** 当前生效主题配置：来自本地持久化的 JSON，可被「外观」页修改 */
    var themeConfig: SRThemeConfig by observable(reactiveMode, SRThemeConfig.Default)
        private set

    var nightModel: Boolean? by observable(reactiveMode, null)
        private set

    var SRThemeColor: SRThemeColors by observable(
        reactiveMode,
        appTheme.colorPalette(isNightMode = false)
    )
        private set

    /** 主题变更监听（跨页面同步用，必须是稳定引用才能反注册） */
    private val themeChangeListener: (SRThemeConfig) -> Unit = { config ->
        applyTheme(config)
    }

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
        systemNightMode = pageData.params.optBoolean(IS_NIGHT_MODE_KEY)
        SRThemeRuntime.register(themeChangeListener)
        applyTheme(loadPersistedTheme())
        bindEvent()
    }

    override fun onDestroyPager() {
        SRThemeRuntime.unregister(themeChangeListener)
        super.onDestroyPager()
    }

    /**
     * 页面回到前台时兜底同步一次（配置未变时 observable 不会触发重绘，开销可忽略）。
     */
    override fun pageDidAppear() {
        super.pageDidAppear()
        applyTheme(loadPersistedTheme())
    }

    /** 读取本地主题；异常时回落默认主题，绝不因主题读取失败而影响页面创建 */
    private fun loadPersistedTheme(): SRThemeConfig {
        return runCatching { SRThemeRuntime.current(acquireSharedPreferencesModule()) }
            .getOrDefault(SRThemeConfig.Default)
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
        systemNightMode = data.optBoolean(IS_NIGHT_MODE_KEY)
        // 「跟随系统」时系统深浅色变化要立即生效；强制浅色/深色时保持不变
        if (themeConfig.mode == SRThemeMode.SYSTEM) {
            applyTheme(themeConfig)
        }
    }

    /**
     * 应用一份主题配置：解析深浅色 → 计算色板 → 通知宿主端同步系统栏外观。
     */
    private fun applyTheme(config: SRThemeConfig) {
        themeConfig = config
        val isNight = config.mode.resolveNightMode(systemNightMode)
        nightModel = isNight
        SRThemeColor = appTheme.colorPalette(isNight)
        // 强制暗色/亮色时宿主端也要跟着换状态栏图标明暗
        acquireBridgeModule().setAppNightMode(isNight)
    }

    // 是否为夜间模式（宿主端在渲染时查询，需返回最终生效值，而不是系统值）
    override fun isNightMode(): Boolean {
        if (nightModel == null) {
            systemNightMode = pageData.params.optBoolean(IS_NIGHT_MODE_KEY)
            applyTheme(loadPersistedTheme())
        }
        return nightModel ?: false
    }

    // 不开启调试UI模式
    override fun debugUIInspector(): Boolean {
        return false
    }

    companion object {
        const val IS_NIGHT_MODE_KEY = "isNightMode"
    }

}
