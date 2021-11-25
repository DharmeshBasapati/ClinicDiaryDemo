package com.app.clinicdiarydemo.ultimate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.clinicdiarydemo.R
import com.app.clinicdiarydemo.databinding.ActivityTheUltimateTryBinding
import com.app.clinicdiarydemo.ultimate.MyUtils.getDateNumber
import com.app.clinicdiarydemo.ultimate.MyUtils.getMonth
import org.joda.time.DateTime
import org.joda.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

class TheUltimateTry : AppCompatActivity(), EventScrollListener {

    private lateinit var requestMultiplePermissions: ActivityResultLauncher<Array<String>>
    private lateinit var binding: ActivityTheUltimateTryBinding

    private var isDayViewSelected: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTheUltimateTryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getMonth(DateTime.now())

        setDaysView(1)

    }

    private fun setDaysView(daysCount: Int) {

        binding.viewPager.adapter =
            MyViewPagerAdapter(
                this@TheUltimateTry,
                MyUtils.getDaysListToShowInHeader().chunked(daysCount),
                MyUtils.getDaysListToUseInEvent().chunked(daysCount),
                daysCount
            ) { selectedDate, selectedTimeSlot ->
                Log.d("TAG", "CALLBACK OF CALLBACK CALLED")
                showAddEventSheet(selectedDate, selectedTimeSlot)

            }

        binding.rvHours.layoutManager = LinearLayoutManager(this@TheUltimateTry)

        binding.rvHours.adapter = MyHoursListAdapter()

        if (isDayViewSelected) {
            binding.viewPager.currentItem = getDateNumber(DateTime().withDate(LocalDate.now())) - 1
        } else {
            val myDatesList = ArrayList<Int>()
            for (i in 1..DateTime().dayOfMonth().maximumValue) {
                val formattedDate = getDateNumber(DateTime().withDayOfMonth(i))
                myDatesList.add(formattedDate)
            }

            val updatedChunkedList = myDatesList.chunked(daysCount)

            updatedChunkedList.forEachIndexed { index, list ->
                if (list.contains(getDateNumber(DateTime().withDate(LocalDate.now())))) {
                    binding.viewPager.currentItem = index
                }
            }
        }
    }


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
                isDayViewSelected = true
                MyUtils.setDaysListAccordingToViews(1)
                MyUtils.setTimeSlotsSelectionList(1)
                setDaysView(1)

            }
            R.id.menuThreeDay -> {
                isDayViewSelected = false
                MyUtils.setDaysListAccordingToViews(3)
                MyUtils.setTimeSlotsSelectionList(3)
                setDaysView(3)
            }
            R.id.menuWeek -> {
                isDayViewSelected = false
                MyUtils.setDaysListAccordingToViews(7)
                MyUtils.setTimeSlotsSelectionList(7)
                setDaysView(7)
            }
        }
        return true
    }


    private fun showAddEventSheet(selectedDate: String, selectedTimeSlot: String) {

        val addEventBottomSheet = AddEventBottomSheet()
        val bundle = Bundle()
        bundle.putString("IN_DATE", selectedDate)
        bundle.putString("IN_TIME", selectedTimeSlot)
        addEventBottomSheet.arguments = bundle
        addEventBottomSheet.show(supportFragmentManager, "AddEventBottomSheet")

    }

}