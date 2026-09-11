package com.imcys.sairen.core.network.model

import kotlinx.serialization.Serializable

/** 股票详情页路由参数，仅传递股票代码。 */
@Serializable
data class StockCode(
    val code: String,
) : SRModel
