package com.app.clinicdiarydemo.others

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.clinicdiarydemo.databinding.RowItemEventsWithPractitionerBinding

class EventsAdapter(var hoursList: List<String>) :
    RecyclerView.Adapter<EventsAdapter.ViewHolder>() {
    inner class ViewHolder(var binding: RowItemEventsWithPractitionerBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        RowItemEventsWithPractitionerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {

            binding.apply {
                tvHour.text = hoursList[position]

                rvEventsWithPractitioner.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL,false)
                val adapter = EventsWithPractitionerAdapter(position==0)
                rvEventsWithPractitioner.adapter = adapter
            }

        }
    }

    override fun getItemCount() = hoursList.size
}