package com.imcys.sairen.core.common.ext

import kotlin.test.Test
import kotlin.test.assertEquals

class LoginErrorEventTest {
    @Test
    fun onlyOriginatingPageReceivesEventAndDestroyedPageStopsReceiving() {
        var homeEvents = 0
        var chatEvents = 0
        registerLoginErrorListener("home") { homeEvents++ }
        registerLoginErrorListener("chat") { chatEvents++ }
        try {
            sendLoginErrorEvent("chat")
            assertEquals(0, homeEvents)
            assertEquals(1, chatEvents)
            unregisterLoginErrorListener("chat")
            sendLoginErrorEvent("chat")
            assertEquals(0, homeEvents)
            assertEquals(1, chatEvents)
        } finally {
            unregisterLoginErrorListener("home")
            unregisterLoginErrorListener("chat")
        }
    }
}
