package com.example.modul5.response

data class LoginResponse(
    val message: String?,
    val accessToken: String?,
    val refreshToken: String?
)

data class RegisterResponse(
    val message: String?,
    val accessToken: String?,
    val refreshToken: String?
)

data class RefreshTokenResponse(
    val message: String?,
    val accessToken: String?
)
