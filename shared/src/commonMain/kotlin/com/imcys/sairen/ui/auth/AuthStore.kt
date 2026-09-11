package com.imcys.sairen.ui.auth

import com.imcys.sairen.core.common.auth.AuthPreferences
import com.imcys.sairen.core.common.ext.acquireSharedPreferencesModule
import com.imcys.sairen.core.network.NetWorkResult
import com.imcys.sairen.core.network.model.AuthSession
import com.imcys.sairen.core.network.service.AppApiService
import com.tencent.kuikly.core.coroutines.launch
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.ObservableThreadSafetyMode
import com.tencent.kuikly.core.reactive.handler.observable
import kotlinx.coroutines.flow.Flow

internal data class AuthState(
    val username: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String = "",
)

internal sealed interface AuthIntent {
    data class UpdateUsername(val value: String) : AuthIntent
    data class UpdatePassword(val value: String) : AuthIntent
    data object Submit : AuthIntent
}

internal class AuthStore(
    private val pager: Pager,
    private val request: (Pager, String, String) -> Flow<NetWorkResult<AuthSession>>,
    private val onSuccess: () -> Unit,
) {
    var state by pager.observable(ObservableThreadSafetyMode.NONE, AuthState())
        private set

    fun dispatch(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.UpdateUsername -> reduce {
                copy(username = intent.value, errorMessage = "")
            }

            is AuthIntent.UpdatePassword -> reduce {
                copy(password = intent.value, errorMessage = "")
            }

            AuthIntent.Submit -> submit()
        }
    }

    private fun submit() {
        if (state.isSubmitting) return
        val username = state.username.trim()
        val password = state.password
        val validationError = validate(username, password)
        if (validationError.isNotEmpty()) {
            reduce { copy(errorMessage = validationError) }
            return
        }

        pager.lifecycleScope.launch {
            request(pager, username, password).collect { result ->
                when (result) {
                    is NetWorkResult.Loading -> reduce {
                        copy(isSubmitting = true, errorMessage = "")
                    }

                    is NetWorkResult.Success -> {
                        val session = result.data
                        if (session == null || session.token.isBlank()) {
                            reduce {
                                copy(isSubmitting = false, errorMessage = "登录响应缺少 token")
                            }
                        } else {
                            saveSession(session)
                            reduce { copy(isSubmitting = false) }
                            onSuccess()
                        }
                    }

                    is NetWorkResult.Error -> reduce {
                        copy(isSubmitting = false, errorMessage = result.errorMsg.orEmpty())
                    }

                    is NetWorkResult.Default -> Unit
                }
            }
        }
    }

    private fun saveSession(session: AuthSession) {
        pager.acquireSharedPreferencesModule().apply {
            setString(AuthPreferences.TOKEN, session.token)
            setString(AuthPreferences.USERNAME, session.user.username)
            setString(AuthPreferences.IS_LOGGED_IN, "true")
        }
    }

    private fun validate(username: String, password: String): String = when {
        username.length < 3 -> "用户名至少需要 3 个字符"
        username.length > 32 -> "用户名最多 32 个字符"
        password.length < 6 -> "密码至少需要 6 个字符"
        password.length > 72 -> "密码最多 72 个字符"
        else -> ""
    }

    private fun reduce(transform: AuthState.() -> AuthState) {
        state = state.transform()
    }

    companion object {
        fun login(pager: Pager, onSuccess: () -> Unit) = AuthStore(
            pager = pager,
            request = AppApiService::login,
            onSuccess = onSuccess,
        )

        fun register(pager: Pager, onSuccess: () -> Unit) = AuthStore(
            pager = pager,
            request = AppApiService::register,
            onSuccess = onSuccess,
        )
    }
}
