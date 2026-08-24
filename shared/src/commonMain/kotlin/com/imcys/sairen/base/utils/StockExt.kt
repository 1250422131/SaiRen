package com.imcys.sairen.base.utils


import com.imcys.sairen.theme.SRThemeColors
import kotlin.math.roundToInt

private const val TEN_THOUSAND = 10_000.0

// 处理颜色
internal fun changePercentColor(changePercent: String, colors: SRThemeColors): Long {
    val value = changePercent.trim().removeSuffix("%").toDoubleOrNull()
    return when {
        value == null || value == 0.0 -> colors.neutralChange
        value > 0.0 -> colors.rise
        else -> colors.fall
    }
}

internal fun changePercentBackgroundColor(changePercent: String, colors: SRThemeColors): Long {
    val value = changePercent.trim().removeSuffix("%").toDoubleOrNull()
    return when {
        value == null || value == 0.0 -> colors.neutralChangeBackground
        value > 0.0 -> colors.riseBackground
        else -> colors.fallBackground
    }
}

internal fun formatChangePercent(changePercent: String, appendPercentSign: Boolean = true): String {
    val rawValue = changePercent.trim().removeSuffix("%")
    val value = rawValue.toDoubleOrNull() ?: return changePercent
    val absoluteValue = rawValue.removePrefix("+").removePrefix("-")
    val suffix = if (appendPercentSign) "%" else ""
    return when {
        value > 0.0 -> "+$absoluteValue$suffix"
        value < 0.0 -> "-$absoluteValue$suffix"
        else -> "$absoluteValue$suffix"
    }
}

internal fun priceColor(price: String, previousClosePrice: String, colors: SRThemeColors): Long {
    val current = price.trim().toDoubleOrNull()
    val previousClose = previousClosePrice.trim().toDoubleOrNull()
    return when {
        current == null || previousClose == null || previousClose == 0.0 -> colors.secondaryText
        current > previousClose -> colors.rise
        current < previousClose -> colors.fall
        else -> colors.neutralChange
    }
}

internal fun formatVolume(volume: String): String {
    val value = volume.toDoubleOrNull() ?: return volume
    if (value <= TEN_THOUSAND) return volume
    val valueInTenThousands = (value / TEN_THOUSAND * 10).roundToInt() / 10.0
    return "${valueInTenThousands}万手"
}
