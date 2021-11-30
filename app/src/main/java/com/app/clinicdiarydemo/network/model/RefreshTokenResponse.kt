package com.app.clinicdiarydemo.network.model

data class RefreshTokenResponse(
    val access_token: String,
    val expires_in: Int,
    val scope: String,
    val token_type: String
)