package com.app.clinicdiarydemo.network.services

import com.app.clinicdiarydemo.network.model.EventRequest
import com.app.clinicdiarydemo.network.model.EventResponse
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

    @POST("calendars/{calendarId}/events")
    fun insertNewEvent(
        @Path("calendarId") calendarId: String,
        @Header("Authorization") accessToken: String,
        @Body eventRequest: EventRequest
    ): Call<EventResponse>



}