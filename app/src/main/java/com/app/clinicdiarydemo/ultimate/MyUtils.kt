package com.app.clinicdiarydemo.ultimate

import com.app.clinicdiarydemo.ultimate.Constants.dateAndDayFormatToShowInHeader
import com.app.clinicdiarydemo.ultimate.Constants.dateFormatToShowWhileAddingEvent
import com.app.clinicdiarydemo.ultimate.Constants.dateNumberFormat
import com.app.clinicdiarydemo.ultimate.Constants.ddMMyyyy
import com.app.clinicdiarydemo.ultimate.Constants.monthYearFormatToShowOnToolbar
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

object MyUtils {

    private lateinit var timeSlotSelectionList: List<List<Int>>

    private lateinit var myChunkedCellsList: List<List<Int>>

    val myTimeSlotsList = arrayListOf(
        "12:00 AM",
        "12:30 AM",
        "1:00 AM",
        "1:30 AM",
        "2:00 AM",
        "2:30 AM",
        "3:00 AM",
        "3:30 AM",
        "4:00 AM",
        "4:30 AM",
        "5:00 AM",
        "5:30 AM",
        "6:00 AM",
        "6:30 AM",
        "7:00 AM",
        "7:30 AM",
        "8:00 AM",
        "8:30 AM",
        "9:00 AM",
        "9:30 AM",
        "10:00 AM",
        "10:30 AM",
        "11:00 AM",
        "11:30 AM",
        "12:00 PM",
        "12:30 PM",
        "1:00 PM",
        "1:30 PM",
        "2:00 PM",
        "2:30 PM",
        "3:00 PM",
        "3:30 PM",
        "4:00 PM",
        "4:30 PM",
        "5:00 PM",
        "5:30 PM",
        "6:00 PM",
        "6:30 PM",
        "7:00 PM",
        "7:30 PM",
        "8:00 PM",
        "8:30 PM",
        "9:00 PM",
        "9:30 PM",
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

        (1..(48 * daysCount)).forEach { i ->
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

            for (j in i..(48 * daysCount) step daysCount) {

                myRowOneList.add(j)

            }

        }

        myChunkedCellsList = myRowOneList.chunked(48)
    }

    private fun getChunkedCellsList(): List<List<Int>> = myChunkedCellsList

    fun getDateNumber(dateTime: DateTime) = Integer.parseInt(dateTime.toString(dateNumberFormat))

     fun getDate(dateTime: DateTime): String =
        dateTime.toString(dateFormatToShowWhileAddingEvent)

    fun getDateToShowInHeader(dateTime: DateTime): String =
        dateTime.toString(dateAndDayFormatToShowInHeader)

    fun convertDateTimeToString(dateTime: DateTime,newDateFormat: String): String = dateTime.toString(newDateFormat)

    fun convertStringToDateTime(dateTimeInString: String): DateTime{
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

    fun getDaysListToUseInEvent(): ArrayList<String> {

        val daysListToUseInEvent = ArrayList<String>()

        for (i in 3 downTo 1) {
            for (dayNumber in 1..DateTime().minusMonths(i).dayOfMonth().maximumValue) {
                val formattedDate = getDate(DateTime().minusMonths(i).withDayOfMonth(dayNumber))
                daysListToUseInEvent.add(formattedDate)
            }
        }

        for (i in 1..DateTime().dayOfMonth().maximumValue) {
            val formattedDate = getDate(DateTime().withDayOfMonth(i))
            daysListToUseInEvent.add(formattedDate)
        }

        for (i in 1..3) {
            for (monthNum in 1..DateTime().plusMonths(i).dayOfMonth().maximumValue) {
                val formattedDate = getDate(DateTime().plusMonths(i).withDayOfMonth(monthNum))
                daysListToUseInEvent.add(formattedDate)
            }
        }

        return daysListToUseInEvent
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

    fun convertMillisToDateInString(milliSeconds: Long, dateFormat: String): String {
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }


}