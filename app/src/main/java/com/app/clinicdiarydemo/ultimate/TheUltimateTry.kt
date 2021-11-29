package com.app.clinicdiarydemo.ultimate

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract.Events
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.clinicdiarydemo.R
import com.app.clinicdiarydemo.databinding.ActivityTheUltimateTryBinding
import com.app.clinicdiarydemo.network.builder.RetrofitBuilder
import com.app.clinicdiarydemo.network.model.InsertCalendarRequest
import com.app.clinicdiarydemo.network.model.InsertCalendarResponse
import com.app.clinicdiarydemo.ultimate.Constants.accessTokenForCalendarAPI
import com.app.clinicdiarydemo.ultimate.Constants.apiKey
import com.app.clinicdiarydemo.ultimate.Constants.calendarId
import com.app.clinicdiarydemo.ultimate.Constants.clientID
import com.app.clinicdiarydemo.ultimate.Constants.dateAndTimeFormatForAddingEventToCalendar
import com.app.clinicdiarydemo.ultimate.MyUtils.getDateNumber
import com.app.clinicdiarydemo.ultimate.MyUtils.getMonth
import net.openid.appauth.*
import org.joda.time.DateTime
import org.joda.time.LocalDate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


class TheUltimateTry : AppCompatActivity(), EventScrollListener {

    private lateinit var authService: AuthorizationService
    private lateinit var binding: ActivityTheUltimateTryBinding

    private val events: ArrayList<com.app.clinicdiarydemo.ultimate.Events> = ArrayList()

    private var isDayViewSelected: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTheUltimateTryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getMonth(DateTime.now())

        fetchAccessToken()

        //insertNewCalendarUsingApi("Clinic Diary")

        //addNewCalendarType()

        setDaysView(1)

        //syncEventsFromCalendar()
    }

    private fun fetchAccessToken() {

        val serviceConfig = AuthorizationServiceConfiguration(
            Uri.parse("https://accounts.google.com/o/oauth2/auth"),
            Uri.parse("https://oauth2.googleapis.com/token")
        )

        val authRequestBuilder = AuthorizationRequest.Builder(
            serviceConfig,
            clientID,
            ResponseTypeValues.CODE,
            Uri.parse("http://localhost/urn:ietf:wg:oauth:2.0:oob")
        )

        authService = AuthorizationService(this)

        val authRequest =
            authRequestBuilder.setScope("https://www.googleapis.com/auth/calendar https://www.googleapis.com/auth/calendar.events")
                .build()

        val authIntent = authService.getAuthorizationRequestIntent(authRequest)

        startActivityForResult(authIntent, 111)

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 111) {

            val authResponse = data?.let { AuthorizationResponse.fromIntent(it) }
            Log.d(
                "TAG",
                "onActivityResult: authResponse Auth Code = ${authResponse?.authorizationCode} "
            )

            authResponse?.createTokenExchangeRequest()?.let {
                authService.performTokenRequest(
                    it
                ) { response, ex ->
                    Log.d("TAG", "on Token Exchange Response : ${response?.toString()}")
                    Log.d("TAG", "on Token Exchange Response : ${response?.accessToken}")
                    accessTokenForCalendarAPI = "Bearer ${response?.accessToken}"
                    insertNewCalendarUsingApi( "CP Clinic Diary")
                }
            }
        }
    }

    private fun insertNewCalendarUsingApi(newEventTitle: String) {

        RetrofitBuilder.focusApiServices.insertCalendarType(
            InsertCalendarRequest(newEventTitle),
            apiKey,
            accessTokenForCalendarAPI
        )
            .enqueue(object : Callback<InsertCalendarResponse> {
                override fun onResponse(
                    call: Call<InsertCalendarResponse>,
                    response: Response<InsertCalendarResponse>
                ) {
                    Log.d("TAG", "onResponse: $response")
                    calendarId = response.body()!!.id
                    Toast.makeText(this@TheUltimateTry, "$newEventTitle added as new calendar type.", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(call: Call<InsertCalendarResponse>, t: Throwable) {
                    Log.d("TAG", "onFailure: ${t.message}")
                }

            })

    }

    private fun setDaysView(daysCount: Int) {

        binding.viewPager.adapter =
            MyViewPagerAdapter(
                this@TheUltimateTry,
                MyUtils.getDaysListToShowInHeader().chunked(daysCount),
                MyUtils.getDaysListToUseInEvent().chunked(daysCount),
                daysCount
            ) { selectedDate, selectedTimeSlot ->
                showAddEventSheet(selectedDate, selectedTimeSlot)
            }

        binding.rvHours.layoutManager = LinearLayoutManager(this@TheUltimateTry)

        binding.rvHours.adapter = MyHoursListAdapter()

        if (isDayViewSelected) {
            binding.viewPager.currentItem = getDateNumber(DateTime().withDate(LocalDate.now())) - 1
        } else {
            val myDatesList = ArrayList<Int>()
            for (i in 1..DateTime().dayOfMonth().maximumValue) {
                val formattedDate = getDateNumber(DateTime().withDayOfMonth(i))
                myDatesList.add(formattedDate)
            }

            val updatedChunkedList = myDatesList.chunked(daysCount)

            updatedChunkedList.forEachIndexed { index, list ->
                if (list.contains(getDateNumber(DateTime().withDate(LocalDate.now())))) {
                    binding.viewPager.currentItem = index
                }
            }
        }
    }

    override fun onEventScrolled(scrollXPos: Int, scrollYPos: Int) {
        binding.rvHours.scrollBy(scrollXPos, scrollYPos)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.day_options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.isChecked = true

        when (item.itemId) {
            R.id.menuOneDay -> {
                isDayViewSelected = true
                MyUtils.setDaysListAccordingToViews(1)
                MyUtils.setTimeSlotsSelectionList(1)
                setDaysView(1)

            }
            R.id.menuThreeDay -> {
                isDayViewSelected = false
                MyUtils.setDaysListAccordingToViews(3)
                MyUtils.setTimeSlotsSelectionList(3)
                setDaysView(3)
            }
            R.id.menuWeek -> {
                isDayViewSelected = false
                MyUtils.setDaysListAccordingToViews(7)
                MyUtils.setTimeSlotsSelectionList(7)
                setDaysView(7)
            }
        }
        return true
    }


    private fun showAddEventSheet(selectedDate: String, selectedTimeSlot: String) {

        val addEventBottomSheet = AddEventBottomSheet()
        val bundle = Bundle()
        bundle.putString("IN_DATE", selectedDate)
        bundle.putString("IN_TIME", selectedTimeSlot)
        addEventBottomSheet.arguments = bundle
        addEventBottomSheet.show(supportFragmentManager, "AddEventBottomSheet")

    }

    private fun syncEventsFromCalendar() {
        val projection = arrayOf(
            Events._ID,
            Events.TITLE,
            Events.DESCRIPTION,
            Events.DTSTART,
            Events.DTEND,
            Events.ALL_DAY,
            Events.EVENT_LOCATION
        )

        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DATE)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        val hours: Int = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes: Int = calendar.get(Calendar.MINUTE)
        val startMillis: Long = calendar.run {
            set(year, month, day - 5, hours, minutes)
            timeInMillis
        }
        val endMillis: Long = Calendar.getInstance().run {
            set(year, month, day + 5, hours, minutes)
            timeInMillis
        }

        val selection =
            "(( " + Events.DTSTART + " >= " + startMillis + " ) AND ( " + Events.DTSTART + " <= " + endMillis + " ) AND ( deleted != 1 ))"

        val cursor: Cursor? = contentResolver
            .query(Events.CONTENT_URI, projection, selection, null, null)

        if (cursor != null && cursor.count > 0 && cursor.moveToFirst()) {
            do {
                events.add(
                    Events(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6)
                    )
                )
            } while (cursor.moveToNext())
            cursor.close()
        }
        Log.d("TAG", "SYNCED EVENTS FROM CALENDAR : $events")
    }

}