package com.app.clinicdiarydemo.network.model

data class EventRequest(
    val summary: String,
    val description: String,
    val start: EventTime,
    val end: EventTime,

)
