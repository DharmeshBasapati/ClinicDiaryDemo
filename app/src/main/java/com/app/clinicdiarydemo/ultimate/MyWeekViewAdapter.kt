package com.app.clinicdiarydemo.ultimate

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.app.clinicdiarydemo.R
import com.app.clinicdiarydemo.databinding.RowItemWeekGridBinding
import com.app.clinicdiarydemo.network.model.Item
import com.app.clinicdiarydemo.ultimate.Constants.timeSlotRowCount
import org.joda.time.DateTime

class MyWeekViewAdapter(
    private val eventsList: List<Item>,
    private var daysList: List<DateTime>,
    private val daysCount: Int,
    val showAddEventSheet: (DateTime, String) -> Unit
) :
    RecyclerView.Adapter<MyWeekViewAdapter.ViewHolder>() {
    inner class ViewHolder(var binding: RowItemWeekGridBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        RowItemWeekGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            binding.apply {

                myView.background =
                    AppCompatResources.getDrawable(
                        itemView.context,
                        R.drawable.custom_week_grid_white
                    )

                when (daysList.size) {
                    1 -> {
                        lnr3Events.visibility = View.GONE
                        lnr7Events.visibility = View.GONE
//                        if (position == 4) {
//                            lnrDayEvents.visibility = View.VISIBLE
//                        } else {
//                            lnrDayEvents.visibility = View.GONE
//                        }

                        val cellDate =
                            CalendarUtils.convertDateTimeToString(daysList[0], "yyyy-MM-dd")
                        val cellTime = CalendarUtils.convertDateToString(
                            CalendarUtils.getDateFromString(
                                CalendarUtils.doSomethingFor1DayView(
                                    position
                                ), "hh:mm a"
                            ),
                            "HH:mm:ss"
                        )
                        //val resultedCellDateTime: DateTime = //2021-12-03T19:30:00.000+05:30
                        //CalendarUtils.convertStringToDateTime("${cellDate}T${cellTime}Z")
                        lnrDayEvents.visibility = View.GONE
                        eventsList.forEachIndexed { index, item ->
                            val eventStartDate = CalendarUtils.convertDateTimeToString( DateTime.parse(item.start.dateTime), "yyyy-MM-dd")
                            val eventStartTime = CalendarUtils.convertDateTimeToString( DateTime.parse(item.start.dateTime), "HH:mm:ss")
                            if(cellDate == eventStartDate && cellTime == eventStartTime){
                                Log.d("TAG", "$cellDate == $eventStartDate && $cellTime == $eventStartTime")
                                Log.d("TAG", "CELL NUMBER #$position FOUND TO ADD EVENT INTO IT !!! ")
                                lnrDayEvents.visibility = View.VISIBLE
                            }
                        }
                    }
                    3 -> {
                        lnrDayEvents.visibility = View.GONE
                        lnr7Events.visibility = View.GONE
                        if (position == 6 || position == 7 || position == 8) {
                            lnr3Events.visibility = View.VISIBLE
                        } else {
                            lnr3Events.visibility = View.GONE
                        }

                    }
                    7 -> {
                        lnrDayEvents.visibility = View.GONE
                        lnr3Events.visibility = View.GONE
                        if (position == 10) {
                            lnr7Events.visibility = View.VISIBLE
                        } else {
                            lnr7Events.visibility = View.GONE
                        }

                    }
                }

                myView.setOnClickListener {

                    Log.d("TAG", "onBindViewHolder: CELL NUMBER - ${position + 1}")

                    when (daysList.size) {
                        1 -> {

                            Log.d("TAG", "onBindViewHolder: SELECTED DATE - ${daysList[0]}")

                            Log.d(
                                "TAG",
                                "onBindViewHolder: SELECTED TIME SLOT - ${
                                    CalendarUtils.doSomethingFor1DayView(position)
                                }"
                            )

                            showAddEventSheet(
                                daysList[0],
                                CalendarUtils.doSomethingFor1DayView(position)
                            )
                        }
                        3, 7 -> {

                            Log.d(
                                "TAG",
                                "onBindViewHolder: SELECTED DATE - ${
                                    daysList[CalendarUtils.getSelectedDateFromCellNumber(
                                        position + 1
                                    )]
                                }"
                            )

                            Log.d(
                                "TAG",
                                "onBindViewHolder: SELECTED TIME SLOT - ${
                                    CalendarUtils.doSomethingFor3or7DaysView(position + 1)
                                }"
                            )

                            showAddEventSheet(
                                daysList[CalendarUtils.getSelectedDateFromCellNumber(
                                    position + 1
                                )], CalendarUtils.doSomethingFor3or7DaysView(position + 1)
                            )

                        }
                    }
                }
            }
        }
    }

    override fun getItemCount() = timeSlotRowCount * daysCount

}