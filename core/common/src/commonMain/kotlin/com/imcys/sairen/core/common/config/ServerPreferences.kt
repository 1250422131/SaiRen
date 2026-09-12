package com.imcys.sairen.core.common.config

/**
 * 服务器地址相关的本地持久化 key 与默认值。
 */
object ServerPreferences {
    /** 塞壬后台地址（host:port 或完整 URL，存储前已经 [normalize] 归一化） */
    const val SERVER_BASE_URL = "sr_siren_server_base_url"

    /** 塞壬后台默认地址 */
    const val DEFAULT_BASE_URL = "https://sairenapi.imcys.com"

    /**
     * 归一化用户输入：去首尾空白、去掉结尾的 `/`；没有协议头时补 `http://`；
     * 空输入回落到默认地址。
     */
    fun normalize(raw: String): String {
        var value = raw.trim().trimEnd('/')
        if (value.isEmpty()) {
            return DEFAULT_BASE_URL
        }
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            value = "http://$value"
        }
        return value
    }
}
