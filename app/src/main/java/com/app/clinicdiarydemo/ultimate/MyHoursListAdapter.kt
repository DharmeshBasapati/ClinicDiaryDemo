package com.app.clinicdiarydemo.ultimate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.clinicdiarydemo.databinding.WeeklyViewHourTextviewBinding
import org.joda.time.DateTime

class MyHoursListAdapter: RecyclerView.Adapter<MyHoursListAdapter.ViewHolder>() {
    inner class ViewHolder(var binding:WeeklyViewHourTextviewBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        WeeklyViewHourTextviewBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder){
            binding.apply {
                val hourDateTime = DateTime().withDate(2000, 1, 1).withTime(0, 0, 0, 0)
                val formattedHours = getHours(hourDateTime.withHourOfDay(position))
                weeklyViewHourTextview.text = formattedHours
            }
        }
    }

    override fun getItemCount() = 24

    private fun getHours(dateTime: DateTime) = dateTime.toString(getHourPattern())

    private fun getHourPattern() = "h a"
}