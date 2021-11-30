package com.app.clinicdiarydemo.ultimate

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {

    private val calendarIdPref = "calendarIDPref"
    private val accessTokenPref = "accessTokenPref"
    private val refreshTokenPref = "refreshTokenPref"

    private val preferences: SharedPreferences =
        context.getSharedPreferences("", Context.MODE_PRIVATE)

    var calendarID: String?
        get() = preferences.getString(calendarIdPref, "")
        set(value) = preferences.edit().putString(calendarIdPref, value).apply()

    var accessToken: String?
        get() = preferences.getString(accessTokenPref, "")
        set(value) = preferences.edit().putString(accessTokenPref, value).apply()

    var refreshToken: String?
        get() = preferences.getString(refreshTokenPref, "")
        set(value) = preferences.edit().putString(refreshTokenPref, value).apply()
}
