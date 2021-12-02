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
                        if (position == 4 ) {
                            lnrDayEvents.visibility = View.VISIBLE
                        } else {
                            lnrDayEvents.visibility = View.GONE
                        }


                    }
                    3 -> {
                        lnrDayEvents.visibility = View.GONE
                        lnr7Events.visibility = View.GONE
                        if (position == 4) {
                            lnr3Events.visibility = View.VISIBLE
                        } else {
                            lnr3Events.visibility = View.GONE
                        }

                    }
                    7 -> {
                        lnrDayEvents.visibility = View.GONE
                        lnr3Events.visibility = View.GONE
                        if (position == 4) {
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
                                    MyUtils.doSomethingFor1DayView(position)
                                }"
                            )

                            showAddEventSheet(
                                daysList[0],
                                MyUtils.doSomethingFor1DayView(position)
                            )
                        }
                        3, 7 -> {

                            Log.d(
                                "TAG",
                                "onBindViewHolder: SELECTED DATE - ${
                                    daysList[MyUtils.getSelectedDateFromCellNumber(
                                        position + 1
                                    )]
                                }"
                            )

                            Log.d(
                                "TAG",
                                "onBindViewHolder: SELECTED TIME SLOT - ${
                                    MyUtils.doSomethingFor3or7DaysView(position + 1)
                                }"
                            )

                            showAddEventSheet(
                                daysList[MyUtils.getSelectedDateFromCellNumber(
                                    position + 1
                                )], MyUtils.doSomethingFor3or7DaysView(position + 1)
                            )

                        }
                    }
                }
            }
        }
    }

    override fun getItemCount() = 48 * daysCount


}