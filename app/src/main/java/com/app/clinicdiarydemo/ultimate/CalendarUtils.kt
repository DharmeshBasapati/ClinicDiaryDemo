package com.app.clinicdiarydemo.ultimate

import com.app.clinicdiarydemo.ultimate.Constants.dateAndDayFormatToShowInHeader
import com.app.clinicdiarydemo.ultimate.Constants.dateFormatToShowWhileAddingEvent
import com.app.clinicdiarydemo.ultimate.Constants.monthYearFormatToShowOnToolbar
import com.app.clinicdiarydemo.ultimate.Constants.timeSlotRowCount
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

object CalendarUtils {

    private lateinit var timeSlotSelectionList: List<List<Int>>

    private lateinit var myChunkedCellsList: List<List<Int>>

    val myTimeSlotsList = arrayListOf(
        "00:00 AM",
        "00:30 AM",
        "01:00 AM",
        "01:30 AM",
        "02:00 AM",
        "02:30 AM",
        "03:00 AM",
        "03:30 AM",
        "04:00 AM",
        "04:30 AM",
        "05:00 AM",
        "05:30 AM",
        "06:00 AM",
        "06:30 AM",
        "07:00 AM",
        "07:30 AM",
        "08:00 AM",
        "08:30 AM",
        "09:00 AM",
        "09:30 AM",
        "10:00 AM",
        "10:30 AM",
        "11:00 AM",
        "11:30 AM",
        "12:00 PM",
        "12:30 PM",
        "01:00 PM",
        "01:30 PM",
        "02:00 PM",
        "02:30 PM",
        "03:00 PM",
        "03:30 PM",
        "04:00 PM",
        "04:30 PM",
        "05:00 PM",
        "05:30 PM",
        "06:00 PM",
        "06:30 PM",
        "07:00 PM",
        "07:30 PM",
        "08:00 PM",
        "08:30 PM",
        "09:00 PM",
        "09:30 PM",
        "10:00 PM",
        "10:30 PM",
        "11:00 PM",
        "11:30 PM"
    )

    fun doSomethingFor3or7DaysView(cellNumber: Int): String {
        getTimeSlotSelectionList().indices.forEach { i ->

            if (getTimeSlotSelectionList()[i].contains(cellNumber)) {

                return myTimeSlotsList[i]

            }
        }

        return ""
    }

    fun setTimeSlotsSelectionList(daysCount: Int) {

        val myList = arrayListOf<Int>()

        (1..(timeSlotRowCount * daysCount)).forEach { i ->
            myList.add(i)
        }

        timeSlotSelectionList = myList.chunked(daysCount)
    }

    private fun getTimeSlotSelectionList(): List<List<Int>> = timeSlotSelectionList

    fun doSomethingFor1DayView(cellNumber: Int): String {

        for (i in 0 until myTimeSlotsList.size) {
            if (i == cellNumber) {
                return myTimeSlotsList[i]
            }
        }

        return ""
    }

    fun getSelectedDateFromCellNumber(cellNumber: Int): Int {

        getChunkedCellsList().indices.forEach { i ->

            if (getChunkedCellsList()[i].contains(cellNumber)) {

                return i

            }
        }

        return 0
    }

    fun setDaysListAccordingToViews(daysCount: Int) {

        val myRowOneList = arrayListOf<Int>()

        for (i in 1..daysCount) {

            for (j in i..(timeSlotRowCount * daysCount) step daysCount) {

                myRowOneList.add(j)

            }

        }

        myChunkedCellsList = myRowOneList.chunked(timeSlotRowCount)
    }

    private fun getChunkedCellsList(): List<List<Int>> = myChunkedCellsList

    fun getDate(dateTime: DateTime): String =
        dateTime.toString(dateFormatToShowWhileAddingEvent)

    fun getDateToShowInHeader(dateTime: DateTime): String =
        dateTime.toString(dateAndDayFormatToShowInHeader)

    fun convertDateTimeToString(dateTime: DateTime, newDateFormat: String): String =
        dateTime.toLocalDateTime().toString(newDateFormat)

    fun convertStringToDateTime(dateTimeInString: String): DateTime {
        return DateTime.parse(dateTimeInString)
    }

    fun getMonth(dateTime: DateTime): String = dateTime.toString(monthYearFormatToShowOnToolbar)

    fun getDaysListToShowInHeader(): ArrayList<DateTime> {

        val daysListToShowInHeader = ArrayList<DateTime>()

        for (i in 3 downTo 1) {
            for (dayNumber in 0 until DateTime().minusMonths(i).dayOfMonth().maximumValue) {
                daysListToShowInHeader.add(DateTime().minusMonths(i).plusDays(dayNumber))
            }
        }

        for (i in 0 until DateTime().dayOfMonth().maximumValue) {
            daysListToShowInHeader.add(DateTime().plusDays(i))
        }

        for (i in 1..6) {
            for (dayNumber in 0 until DateTime().plusMonths(i).dayOfMonth().maximumValue) {
                daysListToShowInHeader.add(DateTime().plusMonths(i).plusDays(dayNumber))
            }
        }

        return daysListToShowInHeader
    }

    fun getDateFromString(date: String, dateFormat: String): Date {
        val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
        return sdf.parse(date)
    }

    fun addHourToSelectedDate(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.HOUR, 1)
        return calendar.time
    }

    fun dateFromUTC(date: Date): Date {
        return Date(date.time + Calendar.getInstance().timeZone.getOffset(Date().time))
    }

    fun convertDateToString(date: Date, newDateFormat: String): String {
        val dateFormatter = SimpleDateFormat(newDateFormat, Locale.getDefault())
        return dateFormatter.format(date)
    }

    fun convertMillisToString(milliSeconds: Long, dateFormat: String): String {
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }


}