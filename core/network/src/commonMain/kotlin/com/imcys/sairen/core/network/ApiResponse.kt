package com.imcys.sairen.core.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * 通用接口响应包装模型
 *
 * 示例：演示 kotlinx.serialization（KuiklyBase 平台补丁版）在 common 代码中的用法，
 * 后续网络层的请求/响应模型统一放在本模块。
 */
@Serializable
data class ApiResponse<out T>(
    val code: Int = 0,
    val data: T?,
    val msg: String = "",
)
