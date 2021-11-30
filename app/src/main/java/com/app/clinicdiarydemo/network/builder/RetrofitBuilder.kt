package com.app.clinicdiarydemo.network.builder

import com.app.clinicdiarydemo.network.services.APIServices
import com.app.clinicdiarydemo.network.services.RTApiServices
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitBuilder {

    private const val BASE_URL = "https://www.googleapis.com/calendar/v3/"

    private fun getRetrofit(): Retrofit {

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder().addInterceptor(interceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL).client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun getRetrofitForRefreshToken(): Retrofit {

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder().addInterceptor(interceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://oauth2.googleapis.com/").client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    val focusApiServices: APIServices = getRetrofit().create(APIServices::class.java)

    val refreshTokenApiServices: RTApiServices = getRetrofitForRefreshToken().create(RTApiServices::class.java)

}