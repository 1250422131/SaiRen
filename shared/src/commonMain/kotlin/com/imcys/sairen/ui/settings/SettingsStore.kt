package com.imcys.sairen.ui.settings

import com.imcys.sairen.base.BasePager
import com.imcys.sairen.core.common.config.ServerPreferences
import com.imcys.sairen.core.common.ext.acquireSharedPreferencesModule
import com.imcys.sairen.theme.SRThemeMode
import com.tencent.kuikly.core.reactive.ObservableThreadSafetyMode
import com.tencent.kuikly.core.reactive.handler.observable

internal sealed interface SettingsIntent {
    /** 重新读取设置数据（页面创建、从子页面回到前台时） */
    data object Refresh : SettingsIntent
}

internal data class SettingsState(
    /** 当前主题深浅色策略（来自 BasePager 的生效主题配置） */
    val themeMode: SRThemeMode = SRThemeMode.SYSTEM,
    /** 当前生效的塞壬后台地址（未配置时为默认地址） */
    val serverUrl: String = ServerPreferences.DEFAULT_BASE_URL,
)

/**
 * 「设置」页状态（MVI）：唯一的 state，通过 intent 驱动 reduce。
 * Refresh 会在页面创建和每次回到前台时派发，重新读取设置数据，
 * 保证从「外观」/「配置」子页返回后展示的是最新值。
 */
internal class SettingsStore(private val pager: BasePager) {

    var state by pager.observable(ObservableThreadSafetyMode.NONE, SettingsState())
        private set

    val themeMode: SRThemeMode
        get() = state.themeMode

    val serverUrl: String
        get() = state.serverUrl

    fun dispatch(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.Refresh -> reduce { refresh() }
        }
    }

    /** 重新读取设置数据，驱动 attr 作用域内的摘要文本刷新 */
    private fun SettingsState.refresh(): SettingsState = copy(
        themeMode = pager.themeConfig.mode,
        serverUrl = runCatching {
            pager.acquireSharedPreferencesModule().getString(ServerPreferences.SERVER_BASE_URL)
        }.getOrNull().takeUnless { it.isNullOrBlank() } ?: ServerPreferences.DEFAULT_BASE_URL,
    )

    private fun reduce(transform: SettingsState.() -> SettingsState) {
        state = state.transform()
    }
}
