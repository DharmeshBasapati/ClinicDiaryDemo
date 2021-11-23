package com.app.clinicdiarydemo.ultimate

import org.joda.time.DateTime

object MyUtils {

    private lateinit var timeSlotSelectionList: List<List<Int>>

    private lateinit var myChunkedCellsList: List<List<Int>>

    private val myTimeSlotsList = arrayListOf(
        "12 AM",
        "1 AM",
        "2 AM",
        "3 AM",
        "4 AM",
        "5 AM",
        "6 AM",
        "7 AM",
        "8 AM",
        "9 AM",
        "10 AM",
        "11 AM",
        "12 PM",
        "1 PM",
        "2 PM",
        "3 PM",
        "4 PM",
        "5 PM",
        "6 PM",
        "7 PM",
        "8 PM",
        "9 PM",
        "10 PM",
        "11 PM"
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

        (1..(24 * daysCount)).forEach { i ->
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

            for (j in i..(24 * daysCount) step daysCount) {

                myRowOneList.add(j)

            }

        }

        myChunkedCellsList = myRowOneList.chunked(24)
    }

    private fun getChunkedCellsList(): List<List<Int>> = myChunkedCellsList

    fun getDateNumber(dateTime: DateTime) = Integer.parseInt(dateTime.toString("dd"))

    private fun getDate(dateTime: DateTime): String = dateTime.toString("E, MMM dd, YYYY")

    private fun getDateToShowInHeader(dateTime: DateTime): String = dateTime.toString("dd\nE")

    fun getMonth(dateTime: DateTime): String = dateTime.toString("MMMM YYYY")

    fun getDaysListToShowInHeader(): ArrayList<String> {

        val daysListToShowInHeader = ArrayList<String>()

        for (i in 1..DateTime().dayOfMonth().maximumValue) {
            val formattedDate = getDateToShowInHeader(DateTime().withDayOfMonth(i))
            daysListToShowInHeader.add(formattedDate)
        }
        return daysListToShowInHeader
    }

    fun getDaysListToUseInEvent(): ArrayList<String> {

        val daysListToUseInEvent = ArrayList<String>()

        for (i in 1..DateTime().dayOfMonth().maximumValue) {
            val formattedDate = getDate(DateTime().withDayOfMonth(i))
            daysListToUseInEvent.add(formattedDate)
        }
        return daysListToUseInEvent
    }

}