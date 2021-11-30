package com.app.clinicdiarydemo.network.services

import com.app.clinicdiarydemo.network.model.RefreshTokenResponse
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface RTApiServices {

    @POST("token")
    fun refreshToken(
        @Query("client_id") clientId: String,
        @Query("refresh_token") refresh_token: String,
        @Query("grant_type") grant_type: String,
    ): Call<RefreshTokenResponse>
}