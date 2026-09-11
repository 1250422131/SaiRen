package com.imcys.sairen.theme

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 主题配置：可整体序列化为 JSON 存入本地。
 *
 * 存储形如（单行，字段全量输出便于排查）：
 * ```json
 * {"mode":"SYSTEM"}
 * ```
 */
@Serializable
internal data class SRThemeConfig(
    /** 深浅色策略 */
    val mode: SRThemeMode = SRThemeMode.SYSTEM,
) {

    /** 是否仍是默认主题（跟随系统） */
    val isDefault: Boolean
        get() = mode == SRThemeMode.SYSTEM

    /** 序列化为紧凑 JSON 字符串 */
    fun encode(): String = themeJson.encodeToString(this)

    companion object {

        val Default = SRThemeConfig()

        private val themeJson = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

        /**
         * 反序列化本地配置；缺失 / 脏数据一律回落到默认主题，保证启动不会因配置损坏而失败。
         * `ignoreUnknownKeys` 兼容历史配置里可能存在的多余字段（如已下线的 primary）。
         */
        fun decode(raw: String?): SRThemeConfig {
            if (raw.isNullOrBlank()) {
                return Default
            }
            return runCatching { themeJson.decodeFromString<SRThemeConfig>(raw) }.getOrDefault(Default)
        }
    }
}
