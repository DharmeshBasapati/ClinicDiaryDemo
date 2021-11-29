package com.app.clinicdiarydemo.network.model

data class EventResponse(
    val end: EventTime,
    val start: EventTime,
    val summary: String,
    val description: String,
    val colorId: Int,
    val location: String,
    val htmlLink: String,
    val guestsCanInviteOthers: Boolean,
    val guestsCanSeeOtherGuests: Boolean,
    val recurrence: List<String>,
    val kind: String,
    val etag: String,
    val id: String,
    val status: String,
    val created: String,
    val updated: String,
    val iCalUID: String,
    val sequence: Int,
    val eventType: String,
)
