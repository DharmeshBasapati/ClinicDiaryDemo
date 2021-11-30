package com.app.clinicdiarydemo.ultimate

import android.app.Application

val prefs: Prefs by lazy {
    MyApp.prefs!!
}

class MyApp: Application()
{
    companion object {
        var prefs: Prefs? = null
        lateinit var instance: MyApp
            private set
    }

    override fun onCreate() {
        super.onCreate()

        instance = this
        prefs = Prefs(applicationContext)
    }
}