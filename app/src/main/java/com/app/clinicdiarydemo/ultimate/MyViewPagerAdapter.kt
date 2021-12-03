package com.app.clinicdiarydemo.ultimate

import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.clinicdiarydemo.R
import com.app.clinicdiarydemo.databinding.RowItemViewPagerBinding
import com.app.clinicdiarydemo.network.model.Item
import com.app.clinicdiarydemo.ultimate.Constants.dateAndDayFormatToShowInHeader
import org.joda.time.DateTime

class MyViewPagerAdapter(
    val listener: EventScrollListener,
    private val daysList: List<List<DateTime>>,
    private val daysCount: Int,
    private val eventsList: List<Item>,
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

                rvEvents.adapter = MyWeekViewAdapter(eventsList,daysList[position], daysCount){s1,s2 -> showAddEventSheet(s1,s2)}

                lnrDates.removeAllViews()

                for (element in daysList[position]) {

                    val inflater = LayoutInflater.from(itemView.context)
                        .inflate(R.layout.new_day_view_header, null, false)

                    val lnrDayDateRoot = inflater.findViewById<LinearLayout>(R.id.lnrDayDateRoot)
                    val tvDay = inflater.findViewById<TextView>(R.id.tvDay)
                    val tvDate = inflater.findViewById<TextView>(R.id.tvDate)

                    tvDay.text = CalendarUtils.convertDateTimeToString(element,"E")
                    tvDate.text = CalendarUtils.convertDateTimeToString(element,"dd")

                    lnrDayDateRoot.layoutParams =
                        LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1F)

                    if (CalendarUtils.convertDateTimeToString(DateTime.now(), Constants.ddMMyyyy) == CalendarUtils.convertDateTimeToString(element,
                            Constants.ddMMyyyy
                        )) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            tvDate.setTextColor(itemView.context.getColor(R.color.white))
                            tvDate.background = AppCompatResources.getDrawable(itemView.context,R.drawable.today_circle_drawable)
                        }
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            tvDate.setTextColor(itemView.context.getColor(R.color.mild_black))
                            tvDate.setBackgroundColor(Color.TRANSPARENT)
                        }
                    }
                    lnrDates.addView(lnrDayDateRoot)

                    /*(inflater as TextView).apply {
                        text = CalendarUtils.getDateToShowInHeader(element)
                        layoutParams =
                            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1F)
                        if (CalendarUtils.convertDateTimeToString(DateTime.now(), Constants.ddMMyyyy) == CalendarUtils.convertDateTimeToString(element,
                                Constants.ddMMyyyy
                            )) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                setTextColor(itemView.context.getColor(R.color.white))
                                setBackgroundColor(itemView.context.getColor(R.color.today_color))
                            }
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                setTextColor(itemView.context.getColor(R.color.mild_black))
                                setBackgroundColor(Color.TRANSPARENT)
                            }
                        }
                        lnrDates.addView(this)
                    }*/

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