package com.example.modul5.api

import com.example.modul5.request.LoginRequest
import com.example.modul5.request.RegisterRequest
import com.example.modul5.request.RefreshTokenRequest
import com.example.modul5.response.LoginResponse
import com.example.modul5.response.RegisterResponse
import com.example.modul5.response.RefreshTokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Header

interface AuthService {

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("register") // ✅ Diperbaiki dari "auth/register" menjadi "register"
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>

    @POST("logout")
    suspend fun logout(@Header("Authorization") token: String): Response<Void>
}
