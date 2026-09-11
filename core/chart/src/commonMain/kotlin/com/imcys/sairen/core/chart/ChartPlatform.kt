package com.imcys.sairen.core.chart

/** Android 原生 Canvas 首次初始化较重，平台侧决定是否延后其首次挂载。 */
internal expect fun shouldDeferInitialChartMount(): Boolean
