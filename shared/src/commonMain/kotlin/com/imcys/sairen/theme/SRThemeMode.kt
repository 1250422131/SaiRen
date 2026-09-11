package com.imcys.sairen.theme

/**
 * 深浅色模式策略。
 *
 * - [SYSTEM]：跟随系统（默认）
 * - [LIGHT]：始终浅色
 * - [DARK]：始终深色
 */
internal enum class SRThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    /** 结合系统当前是否深色，得到最终是否走暗色板。 */
    fun resolveNightMode(systemNightMode: Boolean): Boolean {
        return when (this) {
            SYSTEM -> systemNightMode
            LIGHT -> false
            DARK -> true
        }
    }

    companion object {
        fun fromName(raw: String?): SRThemeMode {
            return entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: SYSTEM
        }
    }
}
