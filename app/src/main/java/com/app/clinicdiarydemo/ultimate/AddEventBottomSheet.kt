package com.app.clinicdiarydemo.ultimate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.clinicdiarydemo.databinding.AddEventBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddEventBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = AddEventBottomSheetBinding.inflate(layoutInflater)

        val IN_DATE = arguments?.get("IN_DATE")
        val IN_TIME = arguments?.get("IN_TIME")

        binding.tvStartDate.text = IN_DATE.toString()
        binding.tvEndDate.text = IN_DATE.toString()
        binding.tvStartTime.text = IN_TIME.toString()
        binding.tvEndTime.text = IN_TIME.toString()

        return binding.root
    }

}