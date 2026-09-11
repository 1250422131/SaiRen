package com.imcys.sairen.core.network

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

/** 只重试聊天的传输错误；调用方必须复用同一 requestId，避免重复生成消息。 */
internal fun <T> retryChatTransport(request: () -> FlowNetWorkResult<T>): FlowNetWorkResult<T> = flow {
    repeat(2) { attempt ->
        var retry = false
        request().collect { result ->
            if (result is NetWorkResult.Error && result.isChatTransportFailure()) {
                if (attempt == 0) {
                    retry = true
                } else {
                    emit(NetWorkResult.Error<T>(
                        data = null,
                        responseData = null,
                        exception = "聊天连接中断或请求超时，请检查网络后重试。若回复已生成，可重新进入聊天查看。",
                    ))
                }
            } else {
                emit(result)
            }
        }
        if (!retry) return@flow
    }
}

private fun NetWorkResult.Error<*>.isChatTransportFailure(): Boolean {
    if (responseData != null) return false
    val message = exception.trim().lowercase()
    return message == "io exception" || message == "network error" || message == "failed to fetch"
        || message.contains("unexpected end of stream") || message.contains("timed out")
        || message.contains("timeout") || message.contains("connection reset")
}
