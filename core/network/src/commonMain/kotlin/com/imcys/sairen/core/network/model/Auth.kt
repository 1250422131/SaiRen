package com.imcys.sairen.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthCredentials(
    val username: String,
    val password: String,
)

@Serializable
data class AuthUser(
    val id: String,
    val username: String,
)

@Serializable
data class AuthSession(
    val token: String,
    val expiresAt: String,
    val user: AuthUser,
) : SRModel
