package com.app.clinicdiarydemo.ultimate

import android.Manifest
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.app.clinicdiarydemo.databinding.AddEventBottomSheetBinding
import com.app.clinicdiarydemo.network.builder.RetrofitBuilder
import com.app.clinicdiarydemo.network.model.EventRequest
import com.app.clinicdiarydemo.network.model.EventResponse
import com.app.clinicdiarydemo.network.model.EventTime
import com.app.clinicdiarydemo.ultimate.Constants.dateAndTimeFormatForAddingEventToCalendar
import com.app.clinicdiarydemo.ultimate.Constants.dateFormatToShowWhileAddingEvent
import com.app.clinicdiarydemo.ultimate.Constants.dateTimeFormatToAddAsEventInCalendar
import com.app.clinicdiarydemo.ultimate.Constants.eventsCalendarUri
import com.app.clinicdiarydemo.ultimate.Constants.timeFormatFromTimePicker
import com.app.clinicdiarydemo.ultimate.Constants.timeFormatToShow
import com.app.clinicdiarydemo.ultimate.Constants.timeFormatToShowWhileAddingEvent
import com.app.clinicdiarydemo.ultimate.MyUtils.dateFromUTC
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import org.joda.time.DateTime
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class AddEventBottomSheet : BottomSheetDialogFragment(),
    com.wdullaer.materialdatetimepicker.time.TimePickerDialog.OnTimeSetListener {

    private var pickerTitle: String = ""
    private lateinit var binding: AddEventBottomSheetBinding
    private lateinit var requestMultiplePermissions: ActivityResultLauncher<Array<String>>

    private lateinit var loadingListener: LoadingListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        loadingListener = context as TheUltimateTry
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AddEventBottomSheetBinding.inflate(layoutInflater)

        val selectedDate: DateTime = DateTime.parse(arguments?.get("IN_DATE").toString())
        val selectedTime = arguments?.getString("IN_TIME")

        binding.apply {

            tvStartDate.text = MyUtils.getDate(selectedDate)

            tvEndDate.text = MyUtils.getDate(selectedDate)

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
                pickerTitle = "Start Time"
                openMDPDialog(
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
//                openTimePicker(
//                    "Start Time",
//                    Integer.parseInt(
//                        tvStartTime.text.toString()
//                            .substring(0, 2)
//                    ),
//                    Integer.parseInt(
//                        tvStartTime.text.toString().substring(
//                            3, 5
//                        )
//                    )
//                )
            }

            tvEndTime.setOnClickListener {
                pickerTitle = "End Time"
                openMDPDialog(
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

    private val timePickerListener: TimePickerDialog.OnTimeSetListener =
        TimePickerDialog.OnTimeSetListener { view: TimePicker?, hourOfDay: Int, minute: Int ->
            val formattedTime: String = when {
                hourOfDay == 0 -> {
                    if (minute < 10) {
                        "${hourOfDay + 12}:0${minute} am"
                    } else {
                        "${hourOfDay + 12}:${minute} am"
                    }
                }
                hourOfDay > 12 -> {
                    if (minute < 10) {
                        "${hourOfDay - 12}:0${minute} pm"
                    } else {
                        "${hourOfDay - 12}:${minute} pm"
                    }
                }
                hourOfDay == 12 -> {
                    if (minute < 10) {
                        "${hourOfDay}:0${minute} pm"
                    } else {
                        "${hourOfDay}:${minute} pm"
                    }
                }
                else -> {
                    if (minute < 10) {
                        "${hourOfDay}:${minute} am"
                    } else {
                        "${hourOfDay}:${minute} am"
                    }
                }
            }
            Log.d("TAG", "Formatted Time : $formattedTime")
        }

    private fun openOldTimePicker(hourToSet: Int, minuteToSet: Int) {

        val timePicker =
            TimePickerDialog(requireContext(), timePickerListener, hourToSet, minuteToSet, true)

        timePicker.show()

    }

    private fun openMDPDialog(hourToSet: Int, minuteToSet: Int){

        val timePicker = com.wdullaer.materialdatetimepicker.time.TimePickerDialog.newInstance(this,hourToSet,minuteToSet,false)
        timePicker.setTimeInterval(1,15)
        timePicker.show(requireActivity().supportFragmentManager,"MTP")

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

        if (areAllDetailsValid()) {


            addNewEventUsingCalendarApi()

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

    private fun addNewEventUsingCalendarApi() {
        loadingListener.checkIfAPICalling(true)
        RetrofitBuilder.focusApiServices.insertNewEvent(
            prefs.calendarID!!, prefs.accessToken!!,
            EventRequest(
                summary = binding.edtTitle.text.toString(),
                description = binding.edtDesc.text.toString(),
                start = EventTime(
                    dateTime = MyUtils.convertDateToString(
                        MyUtils.getDateFromString(
                            binding.tvStartDate.text.toString() + " " + binding.tvStartTime.text.toString(),
                            dateAndTimeFormatForAddingEventToCalendar
                        ), dateTimeFormatToAddAsEventInCalendar
                    ),//"2021-11-30T15:00:00"
                    timeZone = "Asia/Kolkata",
                ),
                end = EventTime(
                    dateTime = MyUtils.convertDateToString(
                        MyUtils.getDateFromString(
                            binding.tvEndDate.text.toString() + " " + binding.tvEndTime.text.toString(),
                            dateAndTimeFormatForAddingEventToCalendar
                        ), dateTimeFormatToAddAsEventInCalendar
                    ),
                    timeZone = "Asia/Kolkata",
                ),

                )
        ).enqueue(object : Callback<EventResponse> {
            override fun onResponse(call: Call<EventResponse>, response: Response<EventResponse>) {
                loadingListener.checkIfAPICalling(false)
                Log.d(
                    "TAG",
                    "onResponse: Event Added - ${response.body()}"
                )
                Toast.makeText(requireContext(), "Event Added Successfully.", Toast.LENGTH_SHORT)
                    .show()
                dismiss()
            }

            override fun onFailure(call: Call<EventResponse>, t: Throwable) {
                loadingListener.checkIfAPICalling(false)
                Log.d(
                    "TAG",
                    "onFailure: Event can't be added - ${t.message.toString()}"
                )
                Toast.makeText(requireContext(), "Event can't be added.", Toast.LENGTH_SHORT).show()
            }


        })
    }

    private fun addEventFromContentResolver() {
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

        event.put(
            CalendarContract.Events._ID,
            "ns0k2lb2l7d9a0800pva8gtqak@group.calendar.google.com"
        )

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
    }

    private fun areAllDetailsValid(): Boolean {

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

    override fun onTimeSet(
        view: com.wdullaer.materialdatetimepicker.time.TimePickerDialog?,
        hourOfDay: Int,
        minute: Int,
        second: Int
    ) {
        if (pickerTitle == "Start Time") {

            binding.tvStartTime.text = MyUtils.convertDateToString(
                MyUtils.getDateFromString(
                    "${hourOfDay}:${minute}",
                    timeFormatFromTimePicker
                ), timeFormatToShowWhileAddingEvent
            )

            binding.tvEndTime.text = MyUtils.convertDateToString(
                MyUtils.addHourToSelectedDate(
                    MyUtils.getDateFromString(
                        "${hourOfDay}:${minute}",
                        timeFormatFromTimePicker
                    )
                ), timeFormatToShowWhileAddingEvent
            )

        } else {

            binding.tvEndTime.text = MyUtils.convertDateToString(
                MyUtils.getDateFromString(
                    "${hourOfDay}:${minute}",
                    timeFormatFromTimePicker
                ), timeFormatToShowWhileAddingEvent
            )

        }
    }

}