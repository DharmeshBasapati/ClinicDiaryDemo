package com.app.clinicdiarydemo.ultimate

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.app.clinicdiarydemo.databinding.AddEventBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.*

class AddEventBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: AddEventBottomSheetBinding
    private lateinit var requestMultiplePermissions: ActivityResultLauncher<Array<String>>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AddEventBottomSheetBinding.inflate(layoutInflater)

        val selectedDate = arguments?.get("IN_DATE")
        val selectedTime = arguments?.get("IN_TIME")

        binding.apply {

            tvStartDate.text = selectedDate.toString()
            tvEndDate.text = selectedDate.toString()
            tvStartTime.text = "04:20"
            tvEndTime.text = "04:50"

            tvStartDate.setOnClickListener {
                openDatePicker(
                    "Start Date",
                    MyUtils.getTimestampFromDateInString(tvStartDate.text.toString())
                )
            }

            tvEndDate.setOnClickListener {
                openDatePicker(
                    "End Date",
                    MyUtils.getTimestampFromDateInString(tvEndDate.text.toString())
                )

            }

            tvStartTime.setOnClickListener {
                openTimePicker(
                    "Start Time",
                    Integer.parseInt(
                        tvStartTime.text.toString()
                            .substring(0, tvStartTime.text.toString().indexOf(":"))
                    ),
                    Integer.parseInt(
                        tvStartTime.text.toString().substring(
                            tvStartTime.text.toString().indexOf(":")+1,
                            tvStartTime.text.toString().length
                        )
                    )
                )
            }

            tvEndTime.setOnClickListener {
                openTimePicker(
                    "End Time",
                    Integer.parseInt(
                        tvEndTime.text.toString()
                            .substring(0, tvEndTime.text.toString().indexOf(":"))
                    ),
                    Integer.parseInt(
                        tvEndTime.text.toString().substring(
                            tvEndTime.text.toString().indexOf(":")+1,
                            tvEndTime.text.toString().length
                        )
                    )
                )
            }

            btnDiscard.setOnClickListener {
                dismiss()
            }

            btnSave.setOnClickListener {
                checkPermission()
            }

            switchAllDay.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    tvStartTime.visibility = View.GONE
                    tvEndTime.visibility = View.GONE
                } else {
                    tvStartTime.visibility = View.VISIBLE
                    tvEndTime.visibility = View.VISIBLE
                }
            }
        }


        return binding.root
    }

    private fun openDatePicker(pickerTitle: String, timeStamp: Long) {
        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setSelection(timeStamp)
                .build()
        datePicker.show(requireActivity().supportFragmentManager, "DATE");
        datePicker.addOnPositiveButtonClickListener {
            Log.d("TAG", "openDatePicker: ${datePicker.selection?.let { it1 -> getDateTime(it1) }}")
            if (pickerTitle == "Start Date") {
                binding.tvStartDate.text = datePicker.selection?.let { it1 -> getDateTime(it1) }
            } else {
                binding.tvEndDate.text = datePicker.selection?.let { it1 -> getDateTime(it1) }
            }
        }
    }

    private fun getDateTime(s: Long): String? {
        return try {
            val sdf = SimpleDateFormat("E, MMM dd, yyyy", Locale.getDefault())
            val netDate = Date(s)
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
    }

    private fun openTimePicker(pickerTitle: String, hourToSet: Int, minuteToSet: Int) {
        val picker =
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(hourToSet)
                .setMinute(minuteToSet)
                .build()
        picker.show(requireActivity().supportFragmentManager, pickerTitle);
        picker.addOnPositiveButtonClickListener {
            Log.d("TAG", "openTimePicker: ${picker.hour}:${picker.minute}")
            if (pickerTitle == "Start Time") {
                binding.tvStartTime.text = "${picker.hour}:${picker.minute}"
            } else {
                binding.tvEndTime.text = "${picker.hour}:${picker.minute}"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestMultiplePermissions =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                permissions.entries.forEach {
                    Log.d("TAG", "${it.key} = ${it.value}")
                }
                if (permissions[Manifest.permission.WRITE_CALENDAR] == true && permissions[Manifest.permission.READ_CALENDAR] == true) {
                    Log.d("TAG", "Permission granted")
                    insertNewEventToCalendar()
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                    Log.d("TAG", "Permission not granted")
                    Toast.makeText(requireContext(), "Permissions not granted", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun insertNewEventToCalendar() {
        dismiss()
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DATE)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        val hours: Int = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes: Int = calendar.get(Calendar.MINUTE)
        val startMillis: Long = calendar.run {
            set(year, month, day, hours, minutes)
            timeInMillis
        }
        val endMillis: Long = Calendar.getInstance().run {
            set(year, month, day, hours, minutes + 60)
            timeInMillis
        }
        val event = ContentValues()
        event.put(CalendarContract.Events.CALENDAR_ID, 1)

        event.put(CalendarContract.Events.TITLE, binding.edtTitle.text.toString())
        event.put(CalendarContract.Events.DESCRIPTION, binding.edtDesc.text.toString())
        event.put(CalendarContract.Events.EVENT_LOCATION, "Lords, London")

        event.put(CalendarContract.Events.DTSTART, startMillis)
        event.put(CalendarContract.Events.DTEND, endMillis)
        var isAllDay = 0
        if (binding.switchAllDay.isChecked) {
            isAllDay = 1
        }
        event.put(CalendarContract.Events.ALL_DAY, isAllDay) // 0 for false, 1 for true
        event.put(CalendarContract.Events.HAS_ALARM, 1) // 0 for false, 1 for true
        val timeZone = TimeZone.getDefault().id
        event.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone)
        /*val rules = "RRULE:FREQ=WEEKLY;UNTIL=20141007T000000Z;WKST=SU;BYDAY=TU,TH"
        event.put(CalendarContract.Events.RRULE, rules)*/
        val baseUri: Uri =
            Uri.parse("content://com.android.calendar/events")

        requireActivity().contentResolver.insert(baseUri, event)

        Toast.makeText(requireContext(), "Adding event to calendar.", Toast.LENGTH_SHORT).show()

    }

    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED -> {
                insertNewEventToCalendar()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CALENDAR) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
            }
            else -> {
                requestMultiplePermissions.launch(
                    arrayOf(
                        Manifest.permission.WRITE_CALENDAR,
                        Manifest.permission.READ_CALENDAR
                    )
                )
            }
        }

    }

}