package com.imcys.sairen.core.common.ext

import com.tencent.kuikly.core.pager.PageData

/** 与 Compose 自适应布局一致的窗口宽度分级。 */
enum class WindowWidthSizeClass {
    Compact,
    Medium,
    Expanded,
    ;

    companion object {
        fun from(width: Float): WindowWidthSizeClass = when {
            width < MEDIUM_MIN_WIDTH -> Compact
            width < EXPANDED_MIN_WIDTH -> Medium
            else -> Expanded
        }

        const val MEDIUM_MIN_WIDTH = 600f
        const val EXPANDED_MIN_WIDTH = 840f
    }
}

/** 与 Compose 自适应布局一致的窗口高度分级。 */
enum class WindowHeightSizeClass {
    Compact,
    Medium,
    Expanded,
    ;

    companion object {
        fun from(height: Float): WindowHeightSizeClass = when {
            height < MEDIUM_MIN_HEIGHT -> Compact
            height < EXPANDED_MIN_HEIGHT -> Medium
            else -> Expanded
        }

        const val MEDIUM_MIN_HEIGHT = 480f
        const val EXPANDED_MIN_HEIGHT = 900f
    }
}

/** 当前页面可用窗口的宽高尺寸类型。 */
data class WindowSizeClass(
    val width: WindowWidthSizeClass,
    val height: WindowHeightSizeClass,
) {
    val isCompactWidth: Boolean
        get() = width == WindowWidthSizeClass.Compact

    val isMediumWidth: Boolean
        get() = width == WindowWidthSizeClass.Medium

    val isExpandedWidth: Boolean
        get() = width == WindowWidthSizeClass.Expanded
}

/**
 * 依据页面实际可用尺寸计算窗口分级。
 *
 * `pageViewWidth` 与 `pageViewHeight` 会在窗口尺寸变化后由 Kuikly 自动更新，
 * 因此在响应式 UI 中调用该函数可随横竖屏、分屏及平板窗口变化重新计算。
 */
fun PageData.windowSizeClass(): WindowSizeClass =
    WindowSizeClass(
        width = WindowWidthSizeClass.from(pageViewWidth),
        height = WindowHeightSizeClass.from(pageViewHeight),
    )
