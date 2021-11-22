package com.app.clinicdiarydemo.ultimate

import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.clinicdiarydemo.R
import com.app.clinicdiarydemo.databinding.ActivityTheUltimateTryBinding
import org.joda.time.DateTime
import java.util.*
import kotlin.collections.ArrayList

class TheUltimateTry : AppCompatActivity(), EventScrollListener {

    private lateinit var binding: ActivityTheUltimateTryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTheUltimateTryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getMonth(DateTime.now())

        setDaysView(1)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            binding.rvHours.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                }

            })

        }

    }

    private fun setDaysView(daysCount: Int) {

        val daysList = ArrayList<String>()

        for (i in 1..DateTime().dayOfMonth().maximumValue) {
            val formattedDate = getDate(DateTime().withDayOfMonth(i))
            daysList.add(formattedDate)
        }

//        val timeList = ArrayList<String>()
//
//        for (i in 1..DateTime().hourOfDay().maximumValue) {
//            val formattedDate = getDate(DateTime().withDayOfMonth(i))
//            daysList.add(formattedDate)
//        }


        binding.viewPager.adapter =
            MyViewPagerAdapter(this@TheUltimateTry, daysList.chunked(daysCount), daysCount)

        binding.rvHours.layoutManager = LinearLayoutManager(this@TheUltimateTry)

        binding.rvHours.adapter = MyHoursListAdapter()
    }

    private fun getDate(dateTime: DateTime) = dateTime.toString("dd\nE")
    private fun getMonth(dateTime: DateTime) = dateTime.toString("MMMM YYYY")

    override fun onEventScrolled(scrollXPos: Int, scrollYPos: Int) {
        binding.rvHours.scrollBy(scrollXPos, scrollYPos)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.day_options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.isChecked = true

        when (item.itemId) {
            R.id.menuOneDay -> {
                setDaysView(1)

            }
            R.id.menuThreeDay -> {
                setDaysView(3)
            }
            R.id.menuWeek -> {
                setDaysView(7)
            }
        }
        return true
    }

}