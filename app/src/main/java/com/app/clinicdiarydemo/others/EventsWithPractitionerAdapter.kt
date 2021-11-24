package com.app.clinicdiarydemo.others

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.clinicdiarydemo.databinding.RowItemEventsBinding

class EventsWithPractitionerAdapter(val showPractitioner: Boolean) :
    RecyclerView.Adapter<EventsWithPractitionerAdapter.ViewHolder>() {
    inner class ViewHolder(var binding: RowItemEventsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        RowItemEventsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {

            binding.apply {


            }

        }
    }

    override fun getItemCount() = 3
}