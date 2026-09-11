package com.imcys.sairen.theme

import com.imcys.sairen.base.BasePager
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView

internal interface SRThemeColors {
    val primary : Long
    val primaryContainer: Long
    val pageBackground: Long
    val stockInfoCard: Long
    val stockTagBackground: Long
    val transparent: Long
    val primaryText: Long
    val secondaryText: Long
    val divider: Long
    val tabIndicator: Long
    val loadingTint: Long
    val sweepHighlight: Long
    val error: Long
    val errorBackground: Long
    val neutralChange: Long
    val rise: Long
    val fall: Long
    val neutralChangeBackground: Long
    val riseBackground: Long
    val fallBackground: Long
    val lowRisk: Long
    val lowRiskBackground: Long
    val mediumLowRisk: Long
    val mediumLowRiskBackground: Long
    val mediumRisk: Long
    val mediumRiskBackground: Long
    val mediumHighRisk: Long
    val mediumHighRiskBackground: Long
}

internal data class SRColorPalette(
    override val primary: Long,
    override val primaryContainer: Long,
    override val pageBackground: Long,
    override val stockInfoCard: Long,
    override val stockTagBackground: Long,
    override val transparent: Long,
    override val primaryText: Long,
    override val secondaryText: Long,
    override val divider: Long,
    override val tabIndicator: Long,
    override val loadingTint: Long,
    override val sweepHighlight: Long,
    override val error: Long,
    override val errorBackground: Long,
    override val neutralChange: Long,
    override val rise: Long,
    override val fall: Long,
    override val neutralChangeBackground: Long,
    override val riseBackground: Long,
    override val fallBackground: Long,
    override val lowRisk: Long,
    override val lowRiskBackground: Long,
    override val mediumLowRisk: Long,
    override val mediumLowRiskBackground: Long,
    override val mediumRisk: Long,
    override val mediumRiskBackground: Long,
    override val mediumHighRisk: Long,
    override val mediumHighRiskBackground: Long,
) : SRThemeColors

internal class SRAppTheme(
    private val dayPalette: SRColorPalette,
    private val nightPalette: SRColorPalette,
) {
    fun colorPalette(isNightMode: Boolean): SRColorPalette {
        return if (isNightMode) nightPalette else dayPalette
    }

    companion object {
        val Default = SRAppTheme(
            dayPalette = SRColorPalette(
                primary = 0xFF0750DA,
                primaryContainer = 0xFFE6EDFB,
                pageBackground = 0xFFFFFFFF,
                stockInfoCard = 0xFFFFFFFF,
                stockTagBackground = 0xFFE8EEF7,
                transparent = 0x00000000,
                primaryText = 0xFF000000,
                secondaryText = 0xFF797C80,
                divider = 0x80E5E6E8,
                tabIndicator = 0xFF0750DA,
                loadingTint = 0xFF1677FF,
                sweepHighlight = 0xFFFFFFFF,
                error = 0xFFE53935,
                errorBackground = 0x1AE53935,
                neutralChange = 0xFF797C80,
                rise = 0xFFE53935,
                fall = 0xFF16A34A,
                neutralChangeBackground = 0x14797C80,
                riseBackground = 0x1AE53935,
                fallBackground = 0x1A16A34A,
                lowRisk = 0xFF27815B,
                lowRiskBackground = 0xFFEAF7F0,
                mediumLowRisk = 0xFF2B7566,
                mediumLowRiskBackground = 0xFFEAF6F3,
                mediumRisk = 0xFF627087,
                mediumRiskBackground = 0xFFF1F3F6,
                mediumHighRisk = 0xFF9A6800,
                mediumHighRiskBackground = 0xFFFFF6E1,
            ),
            nightPalette = SRColorPalette(
                primary = 0xFF78A9FF,
                primaryContainer = 0xFF1D365C,
                pageBackground = 0xFF121212,
                stockInfoCard = 0xFF121212,
                stockTagBackground = 0xFF2B3038,
                transparent = 0x00000000,
                primaryText = 0xFFF5F5F5,
                secondaryText = 0xFFB0B3B8,
                divider = 0x80595C60,
                tabIndicator = 0xFF78A9FF,
                loadingTint = 0xFF78A9FF,
                sweepHighlight = 0xFFFFFFFF,
                error = 0xFFFF7B72,
                errorBackground = 0x26FF7B72,
                neutralChange = 0xFFB0B3B8,
                rise = 0xFFFF7B72,
                fall = 0xFF6DD58C,
                neutralChangeBackground = 0x24B0B3B8,
                riseBackground = 0x26FF7B72,
                fallBackground = 0x266DD58C,
                lowRisk = 0xFF75D9A1,
                lowRiskBackground = 0xFF173C2D,
                mediumLowRisk = 0xFF6AD4BF,
                mediumLowRiskBackground = 0xFF173A36,
                mediumRisk = 0xFFB6C4D8,
                mediumRiskBackground = 0xFF27313E,
                mediumHighRisk = 0xFFFFD37A,
                mediumHighRiskBackground = 0xFF3B2D16,
            ),
        )
    }
}

internal val <T : ComposeAttr, E : ComposeEvent> ComposeView<T, E>.SRThemeColor: SRThemeColors
    get() = (getPager() as BasePager).SRThemeColor

internal val <T : ComposeAttr, E : ComposeEvent> ComposeView<T, E>.nightModel: Boolean
    get() = (getPager() as BasePager).nightModel ?: false
