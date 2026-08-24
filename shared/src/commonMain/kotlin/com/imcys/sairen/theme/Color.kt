package com.imcys.sairen.theme

import com.imcys.sairen.theme.SRColor.palette
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView

internal interface SRThemeColors {
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
    val neutralChange: Long
    val rise: Long
    val fall: Long
    val neutralChangeBackground: Long
    val riseBackground: Long
    val fallBackground: Long
}

internal data class SRColorPalette(
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
    override val neutralChange: Long,
    override val rise: Long,
    override val fall: Long,
    override val neutralChangeBackground: Long,
    override val riseBackground: Long,
    override val fallBackground: Long,
) : SRThemeColors

internal object SRColor {
    val Light = SRColorPalette(
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
        neutralChange = 0xFF797C80,
        rise = 0xFFE53935,
        fall = 0xFF16A34A,
        neutralChangeBackground = 0x14797C80,
        riseBackground = 0x1AE53935,
        fallBackground = 0x1A16A34A,
    )

    val Dark = SRColorPalette(
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
        neutralChange = 0xFFB0B3B8,
        rise = 0xFFFF7B72,
        fall = 0xFF6DD58C,
        neutralChangeBackground = 0x24B0B3B8,
        riseBackground = 0x26FF7B72,
        fallBackground = 0x266DD58C,
    )

    fun palette(isNightMode: Boolean): SRColorPalette = if (isNightMode) Dark else Light
}

internal fun dynamicSRThemeColors(
    paletteProvider: () -> SRColorPalette,
): SRThemeColors = object : SRThemeColors {
    private val colors: SRColorPalette
        get() = paletteProvider()

    override val pageBackground get() = colors.pageBackground
    override val stockInfoCard get() = colors.stockInfoCard
    override val stockTagBackground get() = colors.stockTagBackground
    override val transparent get() = colors.transparent
    override val primaryText get() = colors.primaryText
    override val secondaryText get() = colors.secondaryText
    override val divider get() = colors.divider
    override val tabIndicator get() = colors.tabIndicator
    override val loadingTint get() = colors.loadingTint
    override val sweepHighlight get() = colors.sweepHighlight
    override val neutralChange get() = colors.neutralChange
    override val rise get() = colors.rise
    override val fall get() = colors.fall
    override val neutralChangeBackground get() = colors.neutralChangeBackground
    override val riseBackground get() = colors.riseBackground
    override val fallBackground get() = colors.fallBackground
}

internal val <T : ComposeAttr, E : ComposeEvent> ComposeView<T, E>.SRThemeColor: SRThemeColors
    get() = dynamicSRThemeColors { palette(getPager().isNightMode()) }
