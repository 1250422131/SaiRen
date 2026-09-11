package com.imcys.sairen.theme

import com.imcys.sairen.core.common.theme.ThemePreferences
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.module.SharedPreferencesModule

/**
 * 主题运行时（进程内单例）。
 *
 * 负责三件事：
 * 1. 本地配置的读 / 写（JSON 字符串存入 SharedPreferences）；
 * 2. 进程内缓存，避免每个页面重复反序列化；
 * 3. 广播主题变更 —— Kuikly 的 observable 是页面级的，跨页面无法互相感知，
 *    这里用监听表把「改主题」同步给所有存活页面（在栈里的旧页面也会即时换肤）。
 */
internal object SRThemeRuntime {

    private const val TAG = "SRThemeRuntime"

    private val listeners = mutableListOf<(SRThemeConfig) -> Unit>()
    private var cached: SRThemeConfig? = null

    /** 页面创建时注册，销毁时反注册。传入的 listener 必须是稳定引用（可比较），否则无法反注册。 */
    fun register(listener: (SRThemeConfig) -> Unit) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    fun unregister(listener: (SRThemeConfig) -> Unit) {
        listeners.remove(listener)
    }

    /**
     * 读取当前主题配置：优先命中进程内缓存；首次访问时从本地反序列化。
     *
     * 读不到内容时（首次启动没有配置、或宿主端存储模块还没就绪）返回默认主题且**不写缓存**，
     * 这样后续页面创建/回到前台时还会再读一次，不会把「读失败」固化成默认主题。
     */
    fun current(storage: SharedPreferencesModule): SRThemeConfig {
        cached?.let { return it }
        val raw = runCatching { storage.getString(ThemePreferences.CONFIG) }
            .onFailure { KLog.e(TAG, "读取本地主题配置失败: ${it.message}") }
            .getOrNull()
        if (raw.isNullOrBlank()) {
            return SRThemeConfig.Default
        }
        return SRThemeConfig.decode(raw).also { cached = it }
    }

    /**
     * 应用并持久化主题配置，随后同步给所有存活页面。
     */
    fun update(storage: SharedPreferencesModule, config: SRThemeConfig) {
        cached = config
        runCatching { storage.setString(ThemePreferences.CONFIG, config.encode()) }
            .onFailure { KLog.e(TAG, "写入本地主题配置失败: ${it.message}") }
        // 复制一份再遍历，避免监听回调里发生注册/反注册导致并发修改
        listeners.toList().forEach { it(config) }
    }
}
