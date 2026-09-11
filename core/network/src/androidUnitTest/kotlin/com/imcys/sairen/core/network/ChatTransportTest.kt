package com.imcys.sairen.core.network

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ChatTransportTest {
    @Test
    fun retriesDisconnectedTransportOnceWithoutEmittingFirstError() = runBlocking {
        var calls = 0
        val result = retryChatTransport {
            calls++
            if (calls == 1) flowOf(NetWorkResult.Error<String>(null, null, "io exception"))
            else flowOf(NetWorkResult.Success("回复", null))
        }.toList()
        assertEquals(2, calls)
        assertEquals(1, result.size)
        assertEquals("回复", assertIs<NetWorkResult.Success<String>>(result.single()).data)
    }

    @Test
    fun stopsAfterSecondTransportFailureWithReadableMessage() = runBlocking {
        var calls = 0
        val result = retryChatTransport<String> {
            calls++
            flowOf(NetWorkResult.Error(null, null, "unexpected end of stream"))
        }.toList()
        assertEquals(2, calls)
        assertTrue(assertIs<NetWorkResult.Error<String>>(result.single()).exception.contains("连接中断"))
    }

    @Test
    fun neverRetriesBusinessErrors() = runBlocking {
        var calls = 0
        val result = retryChatTransport<String> {
            calls++
            flowOf(NetWorkResult.Error(null, null, "登录已过期"))
        }.toList()
        assertEquals(1, calls)
        assertEquals("登录已过期", assertIs<NetWorkResult.Error<String>>(result.single()).exception)
    }

    @Test
    fun cancellationPropagatesWithoutRetry() = runBlocking {
        var calls = 0
        assertFailsWith<CancellationException> {
            retryChatTransport<String> {
                calls++
                flow { throw CancellationException("页面退出") }
            }.toList()
        }
        assertEquals(1, calls)
    }
}
