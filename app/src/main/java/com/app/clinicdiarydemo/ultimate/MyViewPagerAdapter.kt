package com.app.clinicdiarydemo.ultimate

import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.clinicdiarydemo.R
import com.app.clinicdiarydemo.databinding.RowItemViewPagerBinding
import com.app.clinicdiarydemo.ultimate.Constants.dateAndDayFormatToShowInHeader
import org.joda.time.DateTime
import org.joda.time.LocalDate

class MyViewPagerAdapter(
    val listener: EventScrollListener,
    private val daysList: List<List<DateTime>>,
    private val daysCount: Int,
    val showAddEventSheet: (DateTime,String) -> Unit

) : RecyclerView.Adapter<MyViewPagerAdapter.ViewHolder>() {

    inner class ViewHolder(var binding: RowItemViewPagerBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        RowItemViewPagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            binding.apply {

                rvEvents.layoutManager = GridLayoutManager(itemView.context, daysList[position].size)

                rvEvents.adapter = MyWeekViewAdapter(daysList[position], daysCount){s1,s2 -> showAddEventSheet(s1,s2)}

                lnrDates.removeAllViews()

                for (element in daysList[position]) {

                    val inflater = LayoutInflater.from(itemView.context)
                        .inflate(R.layout.day_view_header, null, false)
                    (inflater as TextView).apply {
                        text = MyUtils.getDateToShowInHeader(element)
                        layoutParams =
                            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1F)
                        if (MyUtils.convertDateTimeToString(DateTime.now()) == MyUtils.convertDateTimeToString(element)) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                setTextColor(itemView.context.getColor(R.color.black))
                                setBackgroundColor(itemView.context.getColor(R.color.teal_200))
                            }
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                setTextColor(itemView.context.getColor(R.color.white))
                                setBackgroundColor(Color.TRANSPARENT)
                            }
                        }
                        lnrDates.addView(this)
                    }

                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    rvEvents.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            super.onScrolled(recyclerView, dx, dy)
                            listener.onEventScrolled(dx, dy)
                        }

                    })

                }
            }
        }
    }

    private fun getDate(dateTime: DateTime) = dateTime.toString(dateAndDayFormatToShowInHeader)

    override fun getItemCount() = daysList.size

}