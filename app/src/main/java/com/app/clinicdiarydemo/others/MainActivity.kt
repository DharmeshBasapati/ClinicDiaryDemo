package com.app.clinicdiarydemo.others

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.clinicdiarydemo.databinding.ActivityEventViewBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventViewBinding

    private var hoursList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hoursList = arrayListOf("12 AM","1 AM","2 AM","3 AM","4 AM","5 AM","6 AM","7 AM","8 AM","9 AM","10 AM","11 AM","12 PM","1 PM","2 PM",
            "3 PM","4 PM","5 PM","6 PM","7 PM","8 PM","9 PM","10 PM","11 PM")

        binding.apply {
            rvEvents.layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            val adapter = EventsAdapter(hoursList)
            rvEvents.adapter = adapter
        }

    }


}