package com.app.clinicdiarydemo.network.model

data class ListEventsResponse(
    val accessRole: String,
    val defaultReminders: List<Any>,
    val etag: String,
    val items: List<Item>,
    val kind: String,
    val nextSyncToken: String,
    val summary: String,
    val timeZone: String,
    val updated: String
)