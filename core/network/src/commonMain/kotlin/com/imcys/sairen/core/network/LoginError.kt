package com.imcys.sairen.core.network

/** HTTP 鉴权失败和业务登录失效均需要重新登录，其他错误仍由页面展示。 */
@PublishedApi
internal fun isLoginError(httpStatus: Int?, businessCode: Int): Boolean =
    httpStatus == 401 || businessCode == 401 || businessCode == 4001
