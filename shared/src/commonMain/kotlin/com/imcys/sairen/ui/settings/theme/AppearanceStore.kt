package com.imcys.sairen.ui.settings.theme

import com.imcys.sairen.base.BasePager
import com.imcys.sairen.core.common.ext.acquireSharedPreferencesModule
import com.imcys.sairen.theme.SRThemeConfig
import com.imcys.sairen.theme.SRThemeMode
import com.imcys.sairen.theme.SRThemeRuntime
import com.tencent.kuikly.core.reactive.ObservableThreadSafetyMode
import com.tencent.kuikly.core.reactive.handler.observable

internal sealed interface AppearanceIntent {
    /** 选择深浅色策略（只改草稿，不落盘） */
    data class SelectMode(val mode: SRThemeMode) : AppearanceIntent

    /** 把草稿恢复成当前已应用的主题 */
    data object ResetDraft : AppearanceIntent

    /** 应用并持久化草稿主题 */
    data object Apply : AppearanceIntent
}

/**
 * 「外观」页状态。
 *
 * 草稿（draft）与应用态（applied）分离：点选项只改草稿，
 * 点「应用主题」才写入本地并广播给全局，避免误触直接换肤。
 */
internal class AppearanceStore(private val pager: BasePager) {

    /** 当前真正生效（已落盘）的主题配置 */
    var applied: SRThemeConfig by pager.observable(ObservableThreadSafetyMode.NONE, SRThemeConfig.Default)
        private set

    var draftMode: SRThemeMode by pager.observable(ObservableThreadSafetyMode.NONE, SRThemeMode.SYSTEM)
        private set

    init {
        syncFromApplied(pager.themeConfig)
    }

    val draftConfig: SRThemeConfig
        get() = SRThemeConfig(mode = draftMode)

    /** 草稿是否与已应用主题不同 */
    val isDirty: Boolean
        get() = draftMode != applied.mode

    fun dispatch(intent: AppearanceIntent) {
        when (intent) {
            is AppearanceIntent.SelectMode -> draftMode = intent.mode
            AppearanceIntent.ResetDraft -> syncFromApplied(applied)
            AppearanceIntent.Apply -> apply()
        }
    }

    private fun apply() {
        val config = draftConfig
        applied = config
        // 写入本地 + 广播：BasePager 监听后会重算色板，本页与栈内其它页面一起换肤
        SRThemeRuntime.update(pager.acquireSharedPreferencesModule(), config)
    }

    private fun syncFromApplied(config: SRThemeConfig) {
        applied = config
        draftMode = config.mode
    }
}
