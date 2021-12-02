package com.app.clinicdiarydemo.network.services

import com.app.clinicdiarydemo.network.model.*
import retrofit2.Call
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

    @GET("calendars/{calendarId}/events")
    fun listEvents(
        @Path("calendarId") calendarId: String,
        @Header("Authorization") accessToken: String,
    ): Call<ListEventsResponse>

}