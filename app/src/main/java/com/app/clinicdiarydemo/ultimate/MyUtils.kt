package com.app.clinicdiarydemo.ultimate

import android.util.Log

object MyUtils {

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

    fun doSomethingFor1or3DaysView(cellNumber: Int, daysCount: Int): String {

        val myList = arrayListOf<Int>()

        (1..(24 * daysCount)).forEach { i ->
            myList.add(i)
        }

        val newMyList = myList.chunked(daysCount)

        newMyList.indices.forEach { i ->

            if (newMyList[i].contains(cellNumber)) {

                return myTimeSlotsList[i]

            }
        }

        return ""

    }

    fun doSomethingFor1DayView(cellNumber: Int): String {

        for (i in 1 until myTimeSlotsList.size) {
            if (i == cellNumber) {
                return myTimeSlotsList[i]
            }
        }

        return ""
    }

    fun getSelectedDateFromCellNumber(cellNumber: Int, daysCount: Int): Int {

        val myRowOneList = arrayListOf<Int>()

        for (i in 1..daysCount) {

            for (j in i..(24 * daysCount) step daysCount) {

                myRowOneList.add(j)

            }

        }

        Log.d("TAG", "getSelectedDateFromCellNumber: $myRowOneList")

        val myUpdatedList = myRowOneList.chunked(24)

        myUpdatedList.indices.forEach { i ->

            if (myUpdatedList[i].contains(cellNumber)) {

                return i

            }
        }

        return 0
    }

}