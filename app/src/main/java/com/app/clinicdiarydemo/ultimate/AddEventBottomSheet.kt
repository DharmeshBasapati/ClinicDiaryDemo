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
import com.app.clinicdiarydemo.ultimate.Constants.dateAndTimeFormatForAddingEventToCalendar
import com.app.clinicdiarydemo.ultimate.Constants.dateFormatToShowWhileAddingEvent
import com.app.clinicdiarydemo.ultimate.Constants.eventsCalendarUri
import com.app.clinicdiarydemo.ultimate.Constants.timeFormatFromTimePicker
import com.app.clinicdiarydemo.ultimate.Constants.timeFormatToShow
import com.app.clinicdiarydemo.ultimate.Constants.timeFormatToShowWhileAddingEvent
import com.app.clinicdiarydemo.ultimate.MyUtils.dateFromUTC
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.*

class AddEventBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: AddEventBottomSheetBinding
    private lateinit var requestMultiplePermissions: ActivityResultLauncher<Array<String>>

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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AddEventBottomSheetBinding.inflate(layoutInflater)

        val selectedDate = arguments?.get("IN_DATE")
        val selectedTime = arguments?.getString("IN_TIME")

        binding.apply {

            tvStartDate.text = selectedDate.toString()

            tvEndDate.text = selectedDate.toString()

            tvStartTime.text = MyUtils.convertDateToString(
                MyUtils.getDateFromString(
                    selectedTime.toString(),
                    timeFormatToShow
                ), timeFormatToShowWhileAddingEvent
            )

            tvEndTime.text = MyUtils.convertDateToString(
                MyUtils.addHourToSelectedDate(
                    MyUtils.getDateFromString(
                        selectedTime.toString(),
                        timeFormatToShow
                    )
                ), timeFormatToShowWhileAddingEvent
            )

            tvStartDate.setOnClickListener {
                openDatePicker(
                    MyUtils.getDateFromString(
                        tvStartDate.text.toString(),
                        dateFormatToShowWhileAddingEvent
                    )
                )
            }

            tvStartTime.setOnClickListener {
                openTimePicker(
                    "Start Time",
                    Integer.parseInt(
                        tvStartTime.text.toString()
                            .substring(0, 2)
                    ),
                    Integer.parseInt(
                        tvStartTime.text.toString().substring(
                            3, 5
                        )
                    )
                )
            }

            tvEndTime.setOnClickListener {
                openTimePicker(
                    "End Time",
                    Integer.parseInt(
                        tvEndTime.text.toString()
                            .substring(0, 2)
                    ),
                    Integer.parseInt(
                        tvEndTime.text.toString().substring(
                            3, 5
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

        }


        return binding.root
    }

    private fun openDatePicker(date: Date) {
        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setSelection(dateFromUTC(date).time)
                .build()
        datePicker.show(requireActivity().supportFragmentManager, "DATE")
        datePicker.addOnPositiveButtonClickListener {
            binding.tvStartDate.text = datePicker.selection?.let { it1 -> getDateTime(it1) }
            binding.tvEndDate.text = datePicker.selection?.let { it1 -> getDateTime(it1) }
        }
    }

    private fun getDateTime(s: Long): String? {
        return try {
            val sdf = SimpleDateFormat(dateFormatToShowWhileAddingEvent, Locale.getDefault())
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
        picker.show(requireActivity().supportFragmentManager, pickerTitle)
        picker.addOnPositiveButtonClickListener {

            if (pickerTitle == "Start Time") {

                binding.tvStartTime.text = MyUtils.convertDateToString(
                    MyUtils.getDateFromString(
                        "${picker.hour}:${picker.minute}",
                        timeFormatFromTimePicker
                    ), timeFormatToShowWhileAddingEvent
                )

                binding.tvEndTime.text = MyUtils.convertDateToString(
                    MyUtils.addHourToSelectedDate(
                        MyUtils.getDateFromString(
                            "${picker.hour}:${picker.minute}",
                            timeFormatFromTimePicker
                        )
                    ), timeFormatToShowWhileAddingEvent
                )

            } else {

                binding.tvEndTime.text = MyUtils.convertDateToString(
                    MyUtils.getDateFromString(
                        "${picker.hour}:${picker.minute}",
                        timeFormatFromTimePicker
                    ), timeFormatToShowWhileAddingEvent
                )

            }
        }
    }

    private fun insertNewEventToCalendar() {

        if (allDetailsAreValid()) {

            dismiss()

            /*val calendar = Calendar.getInstance()
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
            }*/
            val event = ContentValues()

            event.put(CalendarContract.Events.CALENDAR_ID, 1)

            event.put(CalendarContract.Events.DESCRIPTION, binding.edtDesc.text.toString())

            event.put(CalendarContract.Events.EVENT_LOCATION, "Birmingham, UK")

            event.put(
                CalendarContract.Events.DTSTART,
                MyUtils.getDateFromString(
                    binding.tvStartDate.text.toString() + " " + binding.tvStartTime.text.toString(),
                    dateAndTimeFormatForAddingEventToCalendar
                ).time
            )

            event.put(
                CalendarContract.Events.DTEND,
                MyUtils.getDateFromString(
                    binding.tvEndDate.text.toString() + " " + binding.tvEndTime.text.toString(),
                    dateAndTimeFormatForAddingEventToCalendar
                ).time
            )

            event.put(CalendarContract.Events.ALL_DAY, 0) // 0 for false, 1 for true

            event.put(CalendarContract.Events.HAS_ALARM, 1) // 0 for false, 1 for true

            val timeZone = TimeZone.getDefault().id
            event.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone)

            /*val rules = "RRULE:FREQ=WEEKLY;UNTIL=20141007T000000Z;WKST=SU;BYDAY=TU,TH"
            event.put(CalendarContract.Events.RRULE, rules)*/

            val baseUri: Uri =
                Uri.parse(eventsCalendarUri)

            requireActivity().contentResolver.insert(baseUri, event)

            Toast.makeText(requireContext(), "Adding event to calendar.", Toast.LENGTH_SHORT).show()

        } else {

            if (binding.edtTitle.text.isNotEmpty() && binding.edtDesc.text.isNotEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Start time cannot be after the end time.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please fill the required details.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }


    }

    private fun allDetailsAreValid(): Boolean {

        return when {
            binding.edtTitle.text.isEmpty() -> {
                binding.edtTitle.error = "Please enter event title"
                false
            }
            binding.edtDesc.text.isEmpty() -> {
                binding.edtDesc.error = "Please enter event description"
                false
            }
            else -> !MyUtils.getDateFromString(
                binding.tvEndTime.text.toString(),
                timeFormatToShowWhileAddingEvent
            ).before(
                MyUtils.getDateFromString(
                    binding.tvStartTime.text.toString(),
                    timeFormatToShowWhileAddingEvent
                )
            )
        }

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