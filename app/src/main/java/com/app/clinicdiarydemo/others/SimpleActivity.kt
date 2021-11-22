package com.app.clinicdiarydemo.others

import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.viewpager.widget.ViewPager
import com.app.clinicdiarydemo.R
import org.joda.time.DateTime

class SimpleActivity : AppCompatActivity(), WeekFragmentListener {


    private var nestedScrollView: NestedScrollView? = null
    private lateinit var weekAdapters: WeekAdapters
    private var weekViewHoursHolder: LinearLayout? = null
    private var weekViewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple)

        weekViewHoursHolder = findViewById(R.id.week_view_hours_holder)
        weekViewPager = findViewById(R.id.week_view_view_pager)

        addHours()
        setupViewPager()

        nestedScrollView = findViewById<NestedScrollView>(R.id.nestedScrollView)
        nestedScrollView?.setOnTouchListener { view, motionEvent -> true }
        nestedScrollView?.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            Log.d("TAG", "updateScrollY(Hours): Called - $scrollY")
            weekAdapters.updateScrollY(weekViewPager!!.currentItem, scrollY)
        })

    }

    override fun scrollTo(y: Int) {
        Log.d("TAG", "scrollTo: Called")
        nestedScrollView?.scrollY = y

    }

    override fun updateHoursTopMargin(margin: Int) {}

    override fun getCurrScrollY() = 0

    override fun updateRowHeight(rowHeight: Int) {
    }

    override fun getFullFragmentHeight() = 0

    private fun setupViewPager() {
        weekAdapters = WeekAdapters(supportFragmentManager, this)
        weekViewPager?.adapter = weekAdapters
    }

    private fun addHours() {
        weekViewHoursHolder?.removeAllViews()
        val hourDateTime = DateTime().withDate(2000, 1, 1).withTime(0, 0, 0, 0)
        for (i in 1..23) {
            val formattedHours = getHours(hourDateTime.withHourOfDay(i))
            (layoutInflater.inflate(
                R.layout.weekly_view_hour_textview,
                null,
                false
            ) as TextView).apply {
                text = formattedHours
                setTextColor(resources.getColor(R.color.black))
                height = 210
                weekViewHoursHolder?.addView(this)
            }
        }

    }

    private fun getHours(dateTime: DateTime) = dateTime.toString(getHourPattern())

    private fun getHourPattern() = "h a"

}