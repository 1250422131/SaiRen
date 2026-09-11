package com.imcys.sairen.core.common.ext

// 在 Kuikly 线程中按请求所属页面分发，避免多个页面竞争消费同一个 Channel。
private val loginErrorListeners = mutableMapOf<String, () -> Unit>()

fun registerLoginErrorListener(pagerId: String, listener: () -> Unit) {
    loginErrorListeners[pagerId] = listener
}

fun unregisterLoginErrorListener(pagerId: String) {
    loginErrorListeners.remove(pagerId)
}

fun sendLoginErrorEvent(pagerId: String) {
    loginErrorListeners[pagerId]?.invoke()
}
