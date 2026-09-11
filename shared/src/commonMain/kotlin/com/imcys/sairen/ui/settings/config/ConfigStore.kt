package com.imcys.sairen.ui.settings.config

import com.imcys.sairen.base.BasePager
import com.imcys.sairen.core.common.config.ServerPreferences
import com.imcys.sairen.core.common.ext.acquireSharedPreferencesModule
import com.tencent.kuikly.core.reactive.ObservableThreadSafetyMode
import com.tencent.kuikly.core.reactive.handler.observable

internal sealed interface ConfigIntent {
    /** 修改塞壬后台地址草稿（不落盘） */
    data class ChangeServerUrl(val url: String) : ConfigIntent

    /** 保存草稿（归一化后写入本地） */
    data object Save : ConfigIntent

    /** 恢复默认地址并落盘 */
    data object ResetToDefault : ConfigIntent
}

/**
 * 「配置」页状态。草稿（draft）与已保存态（saved）分离，
 * 点「保存」才归一化并写入本地；塞壬后台地址每次请求时即时读取，保存后即刻生效。
 */
internal class ConfigStore(private val pager: BasePager) {

    /** 当前已生效（已落盘）的地址 */
    var saved: String by pager.observable(ObservableThreadSafetyMode.NONE, currentSaved())
        private set

    /** 输入框草稿 */
    var draft: String by pager.observable(ObservableThreadSafetyMode.NONE, currentSaved())
        private set

    /** 草稿归一化后是否与已生效地址不同 */
    val isDirty: Boolean
        get() = ServerPreferences.normalize(draft) != saved

    fun dispatch(intent: ConfigIntent) {
        when (intent) {
            is ConfigIntent.ChangeServerUrl -> draft = intent.url
            ConfigIntent.Save -> save()
            ConfigIntent.ResetToDefault -> {
                draft = ServerPreferences.DEFAULT_BASE_URL
                save()
            }
        }
    }

    private fun save() {
        val normalized = ServerPreferences.normalize(draft)
        runCatching {
            pager.acquireSharedPreferencesModule()
                .setString(ServerPreferences.SERVER_BASE_URL, normalized)
        }
        saved = normalized
        draft = normalized
    }

    private fun currentSaved(): String {
        return runCatching {
            pager.acquireSharedPreferencesModule().getString(ServerPreferences.SERVER_BASE_URL)
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: ServerPreferences.DEFAULT_BASE_URL
    }
}
