package com.imcys.sairen.core.common

/**
 * common 公共模块
 *
 * 存放跨模块复用的纯 common 代码与基础依赖（无平台实现、无 @Page 页面）。
 * 以 api 方式引入 kotlinx.coroutines（KuiklyBase 平台补丁版），
 * 供 core:chart、core:network 等子模块传递使用。
 */
object CommonModule {
    const val MODULE_NAME: String = "common"
}
