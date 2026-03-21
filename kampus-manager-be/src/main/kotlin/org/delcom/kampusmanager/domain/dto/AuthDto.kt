package org.delcom.kampusmanager.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username : String = "",
    val password : String = "",
)

@Serializable
data class RegisterRequest(
    val name     : String = "",
    val username : String = "",
    val password : String = "",
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken : String = "",
    val authToken    : String = "",
)

@Serializable
data class AuthTokenResponse(
    val authToken    : String,
    val refreshToken : String,
)
