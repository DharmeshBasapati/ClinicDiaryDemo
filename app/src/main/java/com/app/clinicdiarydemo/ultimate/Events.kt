package com.app.clinicdiarydemo.ultimate

data class Events(
    val id: String,
    val title: String,
    val description: String,
    val dateStart: String,
    val dateEnd: String,
    val allDay: String,
    val location: String
)
