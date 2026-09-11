package com.imcys.sairen.core.common.ext

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

object LoginError

// 登录校验异常
private val _loginErrorChannel = Channel<LoginError>(Channel.UNLIMITED)
val loginErrorChannel = _loginErrorChannel.receiveAsFlow()

fun sendLoginErrorEvent() {
    _loginErrorChannel.trySend(LoginError)
}