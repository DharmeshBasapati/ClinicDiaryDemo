package com.app.clinicdiarydemo.ultimate

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.app.clinicdiarydemo.R
import com.app.clinicdiarydemo.databinding.ActivityTheUltimateTryBinding
import com.app.clinicdiarydemo.network.builder.RetrofitBuilder
import com.app.clinicdiarydemo.network.model.*
import com.app.clinicdiarydemo.ultimate.Constants.accessTokenForCalendarAPI
import com.app.clinicdiarydemo.ultimate.Constants.apiKey
import com.app.clinicdiarydemo.ultimate.Constants.calendarId
import com.app.clinicdiarydemo.ultimate.Constants.clientID
import com.app.clinicdiarydemo.ultimate.Constants.ddMMyyyy
import com.app.clinicdiarydemo.ultimate.Constants.grantTypeForRefreshToken
import com.app.clinicdiarydemo.ultimate.CalendarUtils.getDaysListToShowInHeader
import com.app.clinicdiarydemo.ultimate.CalendarUtils.getMonth
import net.openid.appauth.*
import org.joda.time.DateTime
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


class CDAppointmentsActivity : AppCompatActivity(), EventScrollListener, LoadingListener {

    private lateinit var eventsList: List<Item>

    private lateinit var authService: AuthorizationService

    private lateinit var binding: ActivityTheUltimateTryBinding

    private var isDayViewSelected: Boolean = false

    private var currentDaysView = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTheUltimateTryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title =
            getMonth(DateTime.now())

        if (prefs.accessToken!!.isEmpty()) {
            fetchAccessToken()
        } else if(prefs.calendarID!!.isNotEmpty()) {
            fetchMyEvents()
        }

        //doRefreshToken()

        val cellDate = CalendarUtils.convertDateTimeToString(
            DateTime.parse("2021-12-03T14:28:23.193+05:30"),
            "yyyy-MM-dd"
        )
        val cellTime = CalendarUtils.convertDateToString(
            CalendarUtils.getDateFromString("03:30 PM", "hh:mm a"),
            "HH:mm:ss"
        )
        val resultedCellDateTime: DateTime =
            CalendarUtils.convertStringToDateTime("${cellDate}T${cellTime}Z")

        Log.d("TAG", "resultedCellDateTime: $resultedCellDateTime")

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                when (currentDaysView) {
                    3 -> {

                        if (getDaysListToShowInHeader().chunked(currentDaysView)[position].size == 3) {
                            supportActionBar?.title =
                                getMonth(getDaysListToShowInHeader().chunked(currentDaysView)[position][1])
                        } else {
                            supportActionBar?.title =
                                getMonth(getDaysListToShowInHeader().chunked(currentDaysView)[position][0])
                        }

                    }
                    7 -> {

                        if (getDaysListToShowInHeader().chunked(currentDaysView)[position].size == 7) {
                            supportActionBar?.title =
                                getMonth(getDaysListToShowInHeader().chunked(currentDaysView)[position][3])
                        } else {
                            supportActionBar?.title =
                                getMonth(getDaysListToShowInHeader().chunked(currentDaysView)[position][0])
                        }

                    }
                    else -> {
                        Log.d(
                            "TAG",
                            "onPageSelected: ${getDaysListToShowInHeader().chunked(currentDaysView)[position][0]}"
                        )

                        supportActionBar?.title =
                            getMonth(getDaysListToShowInHeader().chunked(currentDaysView)[position][0])
                    }
                }

            }
        })

    }

    private fun fetchMyEvents() {

        Log.d("TAG", "fetchMyEvents: Called")

        showProgress(true)
        RetrofitBuilder.focusApiServices.listEvents(
            prefs.calendarID!!, prefs.accessToken!!,
        ).enqueue(object : Callback<ListEventsResponse> {
            override fun onResponse(
                call: Call<ListEventsResponse>,
                response: Response<ListEventsResponse>
            ) {
                showProgress(false)
                Log.d(
                    "TAG",
                    "onResponse: Events Listed Successfully - ${response.body()}"
                )

                eventsList = response.body()?.items ?: arrayListOf()

                setDaysView(currentDaysView)

            }

            override fun onFailure(call: Call<ListEventsResponse>, t: Throwable) {
                showProgress(false)
                Log.d(
                    "TAG",
                    "onFailure: Events list API - ${t.message.toString()}"
                )

            }


        })

    }

    private fun doRefreshToken() {
        showProgress(true)
        RetrofitBuilder.refreshTokenApiServices.refreshToken(
            clientID,
            prefs.refreshToken!!,
            grantTypeForRefreshToken
        ).enqueue(object : Callback<RefreshTokenResponse> {
            override fun onResponse(
                call: Call<RefreshTokenResponse>,
                response: Response<RefreshTokenResponse>
            ) {
                showProgress(false)
                Log.d(
                    "TAG",
                    "onResponse: doRefreshToken - New Access Token - ${response.body()?.access_token}"
                )

                prefs.accessToken = response.body()?.access_token
            }

            override fun onFailure(call: Call<RefreshTokenResponse>, t: Throwable) {
                showProgress(false)
                Log.d("TAG", "onFailure: doRefreshToken - ${t.message}")
            }

        })


    }

    private fun fetchAccessToken() {

        Log.d("TAG", "fetchAccessToken: Called")

        val serviceConfig = AuthorizationServiceConfiguration(
            Uri.parse("https://accounts.google.com/o/oauth2/auth"),
            Uri.parse("https://oauth2.googleapis.com/token")
        )

        val authRequestBuilder = AuthorizationRequest.Builder(
            serviceConfig,
            clientID,
            ResponseTypeValues.CODE,
            Uri.parse(
                "http://localhost/urn:ietf:wg:oauth:2.0:oob"
            )
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
                    Log.d("TAG", "Initial Access Token : ${response?.accessToken}")
                    accessTokenForCalendarAPI = "Bearer ${response?.accessToken}"

                    prefs.accessToken = "Bearer ${response?.accessToken}"
                    prefs.refreshToken = response?.refreshToken

                    if (prefs.calendarID!!.isEmpty()) {
                        insertNewCalendarUsingApi("Center Fresh")
                    }else{
                        fetchMyEvents()
                    }
                }
            }
        }
    }

    private fun insertNewCalendarUsingApi(newEventTitle: String) {

        Log.d("TAG", "insertNewCalendarUsingApi: Called")

        showProgress(true)
        RetrofitBuilder.focusApiServices.insertCalendarType(
            InsertCalendarRequest(newEventTitle),
            apiKey,
            prefs.accessToken!!
        )
            .enqueue(object : Callback<InsertCalendarResponse> {
                override fun onResponse(
                    call: Call<InsertCalendarResponse>,
                    response: Response<InsertCalendarResponse>
                ) {
                    showProgress(false)
                    Log.d("TAG", "onResponse: $response")
                    calendarId = response.body()!!.id
                    prefs.calendarID = response.body()!!.id
                    Toast.makeText(
                        this@CDAppointmentsActivity,
                        "$newEventTitle added as new calendar type.",
                        Toast.LENGTH_SHORT
                    ).show()
                    fetchMyEvents()
                }

                override fun onFailure(call: Call<InsertCalendarResponse>, t: Throwable) {
                    showProgress(false)
                    Log.d("TAG", "onFailure: ${t.message}")
                }

            })

    }

    private fun showProgress(needToShow: Boolean) {
        if (needToShow) {
            binding.lnrProgress.visibility = View.VISIBLE
        } else {
            binding.lnrProgress.visibility = View.GONE
        }

    }

    private fun setDaysView(daysCount: Int) {

        CalendarUtils.setDaysListAccordingToViews(daysCount)

        CalendarUtils.setTimeSlotsSelectionList(daysCount)

        binding.viewPager.adapter =
            MyViewPagerAdapter(
                this@CDAppointmentsActivity,
                getDaysListToShowInHeader().chunked(daysCount),
                daysCount,
                eventsList
            ) { selectedDate, selectedTimeSlot ->
                showAddEventSheet(selectedDate, selectedTimeSlot)
            }

        binding.rvHours.layoutManager = LinearLayoutManager(this@CDAppointmentsActivity)

        binding.rvHours.adapter = MyHoursListAdapter(CalendarUtils.myTimeSlotsList)

        val updatedChunkedList = getDaysListToShowInHeader().chunked(daysCount)

        updatedChunkedList.forEachIndexed { mainIndex, list ->

            list.forEachIndexed { _, dateTime ->
                if (CalendarUtils.convertDateTimeToString(
                        dateTime,
                        ddMMyyyy
                    ) == CalendarUtils.convertDateTimeToString(
                        DateTime.now(), ddMMyyyy
                    )
                ) {
                    binding.viewPager.setCurrentItem(mainIndex, true)
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
                currentDaysView = 1
                setDaysView(currentDaysView)

            }
            R.id.menuThreeDay -> {
                isDayViewSelected = false
                currentDaysView = 3
                setDaysView(currentDaysView)
            }
            R.id.menuWeek -> {
                isDayViewSelected = false
                currentDaysView = 7
                setDaysView(currentDaysView)
            }
        }
        return true
    }

    private fun showAddEventSheet(selectedDate: DateTime, selectedTimeSlot: String) {

        val addEventBottomSheet = AddEventBottomSheet()
        val bundle = Bundle()
        bundle.putString("IN_DATE", selectedDate.toString())
        bundle.putString("IN_TIME", selectedTimeSlot)
        addEventBottomSheet.arguments = bundle
        addEventBottomSheet.show(supportFragmentManager, "AddEventBottomSheet")

    }

    override fun checkIfAPICalling(isLoading: Boolean) {
        showProgress(isLoading)
    }

}