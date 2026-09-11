package com.imcys.sairen.core.network

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoginErrorTest {
    @Test
    fun http401WithoutBusinessBodyRequiresLogin() {
        assertTrue(isLoginError(401, 0))
    }

    @Test
    fun businessLoginCodesRequireLoginRegardlessOfHttpStatus() {
        for (status in listOf(null, 200, 401)) {
            assertTrue(isLoginError(status, 401))
            assertTrue(isLoginError(status, 4001))
        }
    }

    @Test
    fun unrelatedFailuresDoNotRedirectToLogin() {
        assertFalse(isLoginError(500, 5000))
        assertFalse(isLoginError(null, 0))
        assertFalse(isLoginError(200, 0))
        assertFalse(isLoginError(403, 4003))
    }
}
