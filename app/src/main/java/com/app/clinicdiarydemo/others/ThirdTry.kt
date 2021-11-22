package com.app.clinicdiarydemo.others

import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.clinicdiarydemo.R
import com.app.clinicdiarydemo.alamview.*
import com.google.android.material.button.MaterialButtonToggleGroup
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.HOUR_OF_DAY

class ThirdTry : AppCompatActivity(), MonthLoader.MonthChangeListener, WeekView.EventClickListener,
    WeekViewLoader, WeekView.ScrollListener {

    private var weekView1: WeekView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third_try)



        weekView1 = findViewById(R.id.weekView)
        weekView1?.monthChangeListener = this
        weekView1?.setOnEventClickListener(this)
        weekView1?.weekViewLoader = this
        weekView1?.scrollListener = this
        weekView1?.setEmptyViewClickListener {
            Log.d("TAG", "onCreate: Empty Cell Clicked - $it")
            Toast.makeText(this, it.get(HOUR_OF_DAY).toString(), Toast.LENGTH_SHORT).show()
        }

        setupDateTimeInterpreter(false)

        getWeekDaysList()
    }

    private fun getWeekDaysList() {
        val format: DateFormat = SimpleDateFormat("MM/dd/yyyy")
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.SUNDAY
        calendar[Calendar.DAY_OF_WEEK] = Calendar.SUNDAY

        val days = arrayOfNulls<String>(7)
        for (i in 0..6) {
            days[i] = format.format(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        //["11/22/2021", "11/23/2021", "11/24/2021", "11/25/2021", "11/26/2021", "11/27/2021","11/28/2021"]
        Log.d("TAG", "getWeekDaysList: $days")
    }

    override fun onMonthChange(newYear: Int, newMonth: Int): MutableList<out WeekViewEvent> {
        val startTime = Calendar.getInstance()
        startTime.set(2021, 11, 12, 16, 10)

        val endTime = Calendar.getInstance()
        endTime.set(2021, 11, 12, 16, 40)

        return mutableListOf(WeekViewEvent(1, "First Event", startTime, endTime))
    }

    private fun setupDateTimeInterpreter(shortDate: Boolean) {
        weekView1?.dateTimeInterpreter = object : DateTimeInterpreter {
            override fun interpretDate(date: Calendar): String {
                val weekdayNameFormat = SimpleDateFormat("EEE,", Locale.getDefault())
                var weekday = weekdayNameFormat.format(date.time)
                val format = SimpleDateFormat(" d MMM", Locale.getDefault())

                if (shortDate) weekday = weekday[0].toString()
                return weekday.uppercase() + format.format(date.time)
            }

            override fun interpretTime(hour: Int): String {
                val timeToShow = when {
                    hour - 12 == 0 -> "12 PM"
                    hour > 11 -> (hour - 12).toString() + " PM"
                    hour == 0 -> "12 AM"
                    else -> "$hour AM"
                }
                return timeToShow
            }
        }
    }

    override fun onEventClick(event: WeekViewEvent?, eventRect: RectF?) {
        TODO("Not yet implemented")
    }

    override fun toWeekViewPeriodIndex(instance: Calendar?): Double {
        return 0.0
    }

    override fun onLoad(periodIndex: Int): MutableList<out WeekViewEvent> {
        val startTime = Calendar.getInstance()
        startTime.set(2021, 11, 12, 13, 10)

        val endTime = Calendar.getInstance()
        endTime.set(2021, 11, 12, 13, 40)

        return mutableListOf(WeekViewEvent(periodIndex.toLong(), "First Event", startTime, endTime))
    }

    override fun onFirstVisibleDayChanged(
        newFirstVisibleDay: Calendar?,
        oldFirstVisibleDay: Calendar?
    ) {

    }
}