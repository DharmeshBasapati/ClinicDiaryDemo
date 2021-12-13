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
import com.app.clinicdiarydemo.ultimate.CalendarUtils.getDaysListToShowInHeader
import com.app.clinicdiarydemo.ultimate.CalendarUtils.getMonth
import com.app.clinicdiarydemo.ultimate.Constants.apiKey
import com.app.clinicdiarydemo.ultimate.Constants.appCalendarName
import com.app.clinicdiarydemo.ultimate.Constants.authEndPoint
import com.app.clinicdiarydemo.ultimate.Constants.calendarScopes
import com.app.clinicdiarydemo.ultimate.Constants.clientID
import com.app.clinicdiarydemo.ultimate.Constants.grantTypeForRefreshToken
import com.app.clinicdiarydemo.ultimate.Constants.redirectUri
import com.app.clinicdiarydemo.ultimate.Constants.tokenEndPoint
import net.openid.appauth.*
import org.joda.time.DateTime
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class CDAppointmentsActivity : AppCompatActivity(), EventScrollListener, LoadingListener {

    private lateinit var daysListShowingInHeader: List<List<DateTime>>

    private lateinit var eventsList: List<Item>

    private lateinit var authService: AuthorizationService

    private lateinit var binding: ActivityTheUltimateTryBinding

    private var currentDaysView = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTheUltimateTryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title =
            getMonth(DateTime.now())

        if (prefs.accessToken!!.isEmpty()) {
            fetchAccessToken()
        } else if (prefs.calendarID!!.isNotEmpty()) {
            fetchMyEvents()
        }

        setupHoursList()

        setupDaysListToShowInHeader()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

                when (daysListShowingInHeader[position].size) {
                    3 -> {
                        supportActionBar?.title =
                            getMonth(daysListShowingInHeader[position][1])
                    }
                    7 -> {
                        supportActionBar?.title =
                            getMonth(daysListShowingInHeader[position][3])
                    }
                    else -> {
                        supportActionBar?.title =
                            getMonth(daysListShowingInHeader[position][0])
                    }
                }
            }
        })

    }

    private fun setupDaysListToShowInHeader() {
        daysListShowingInHeader = getDaysListToShowInHeader().chunked(currentDaysView)
    }

    private fun setupHoursList() {
        binding.rvHours.layoutManager = LinearLayoutManager(this@CDAppointmentsActivity)
        binding.rvHours.adapter = MyHoursListAdapter(CalendarUtils.myTimeSlotsList)
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

                when {
                    response.code() == 200 -> {
                        Log.d(
                            "TAG",
                            "onResponse: Events Listed Successfully - ${response.body()}"
                        )

                        eventsList = response.body()?.items ?: arrayListOf()

                        updateCalendarView(currentDaysView)

                    }
                    response.code() == 401 -> {

                        //Request had invalid authentication credentials.
                        // Expected OAuth 2 access token, login cookie or other valid authentication credential.
                        doRefreshToken()

                    }
                    response.code() == 403 -> {

                        //The request is missing a valid API key.

                    }
                }


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

     fun doRefreshToken() {
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

                prefs.accessToken = "Bearer ${response.body()?.access_token}"
                fetchMyEvents()
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
            Uri.parse(authEndPoint),
            Uri.parse(tokenEndPoint)
        )

        val authRequestBuilder = AuthorizationRequest.Builder(
            serviceConfig,
            clientID,
            ResponseTypeValues.CODE,
            Uri.parse(
                redirectUri
            )
        )

        authService = AuthorizationService(this)

        val authRequest =
            authRequestBuilder.setScope(calendarScopes)
                .build()

        val authIntent = authService.getAuthorizationRequestIntent(authRequest)

        startActivityForResult(authIntent, 111)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 111) {

            val authResponse = data?.let { AuthorizationResponse.fromIntent(it) }

            authResponse?.createTokenExchangeRequest()?.let {
                authService.performTokenRequest(
                    it
                ) { response, ex ->

                    prefs.accessToken = "Bearer ${response?.accessToken}"
                    prefs.refreshToken = response?.refreshToken

                    if (prefs.calendarID!!.isEmpty()) {
                        insertNewCalendarUsingApi()
                    } else {
                        fetchMyEvents()
                    }
                }
            }
        }
    }

    private fun insertNewCalendarUsingApi() {

        Log.d("TAG", "insertNewCalendarUsingApi: Called")

        showProgress(true)
        RetrofitBuilder.focusApiServices.insertCalendarType(
            InsertCalendarRequest(appCalendarName),
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
                    prefs.calendarID = response.body()!!.id
                    Toast.makeText(
                        this@CDAppointmentsActivity,
                        "$appCalendarName added as new calendar type.",
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

    private fun showProgress(needToShow: Boolean) = if (needToShow) {
        binding.lnrProgress.visibility = View.VISIBLE
    } else {
        binding.lnrProgress.visibility = View.GONE
    }

    private fun updateCalendarView(daysCount: Int) {

        currentDaysView = daysCount

        setupDaysListToShowInHeader()

        CalendarUtils.setDaysListAccordingToViews(daysCount)

        CalendarUtils.setTimeSlotsSelectionList(daysCount)

        binding.viewPager.adapter = MyViewPagerAdapter(
            this@CDAppointmentsActivity, daysListShowingInHeader,
            daysCount, eventsList
        ) { selectedDate, selectedTimeSlot ->
            showAddEventSheet(selectedDate, selectedTimeSlot)
        }

        /*updatedDaysList.forEachIndexed { mainIndex, list ->

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
        }*/
        binding.viewPager.setPageTransformer(ZoomOutPageTransformer())
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
                updateCalendarView(1)
            }
            R.id.menuThreeDay -> {
                updateCalendarView(3)
            }
            R.id.menuWeek -> {
                updateCalendarView(7)
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