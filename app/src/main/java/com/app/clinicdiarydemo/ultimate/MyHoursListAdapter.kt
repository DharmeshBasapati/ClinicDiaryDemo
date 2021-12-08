package com.app.clinicdiarydemo.ultimate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.clinicdiarydemo.databinding.WeeklyViewHourTextviewBinding
import org.joda.time.DateTime

class MyHoursListAdapter(private val timeSlots: List<String>): RecyclerView.Adapter<MyHoursListAdapter.ViewHolder>() {
    inner class ViewHolder(var binding:WeeklyViewHourTextviewBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        WeeklyViewHourTextviewBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder){
            binding.apply {
                weeklyViewHourTextview.text = timeSlots[position]

            }
        }
    }

    override fun getItemCount() = timeSlots.size

    private fun getHourPattern() = "h a"
}