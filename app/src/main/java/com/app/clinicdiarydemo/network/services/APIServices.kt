package com.app.clinicdiarydemo.network.services

import com.app.clinicdiarydemo.network.model.InsertCalendarRequest
import com.app.clinicdiarydemo.network.model.InsertCalendarResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.*

interface APIServices {

    @POST("calendars")
    fun insertCalendarType(
        @Body insertCalendarRequest: InsertCalendarRequest,
        @Query("key") apiKey: String,
        @Header("Authorization") accessToken: String
    ): Call<InsertCalendarResponse>
//

    @GET("auth?scope=email%20profile&response_type=code&state=security_token%3D138r5719ru3e1%26url%3Dhttps%3A%2F%2Foauth2.googleapis.com%2Ftoken&redirect_uri=urn:ietf:wg:oauth:2.0:oob&client_id=388580567532-0n3p2lqmvsi3vb28mcdaa2idbc4fq36s.apps.googleusercontent.com")
    fun getAccessToken():Call<Any>

}