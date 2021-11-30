package com.app.clinicdiarydemo.ultimate

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract.Events
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.clinicdiarydemo.R
import com.app.clinicdiarydemo.databinding.ActivityTheUltimateTryBinding
import com.app.clinicdiarydemo.network.builder.RetrofitBuilder
import com.app.clinicdiarydemo.network.model.InsertCalendarRequest
import com.app.clinicdiarydemo.network.model.InsertCalendarResponse
import com.app.clinicdiarydemo.network.model.RefreshTokenResponse
import com.app.clinicdiarydemo.ultimate.Constants.accessTokenForCalendarAPI
import com.app.clinicdiarydemo.ultimate.Constants.apiKey
import com.app.clinicdiarydemo.ultimate.Constants.calendarId
import com.app.clinicdiarydemo.ultimate.Constants.clientID
import com.app.clinicdiarydemo.ultimate.Constants.grantTypeForRefreshToken
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


class TheUltimateTry : AppCompatActivity(), EventScrollListener, LoadingListener {

    private lateinit var authService: AuthorizationService
    private lateinit var binding: ActivityTheUltimateTryBinding

    private val events: ArrayList<com.app.clinicdiarydemo.ultimate.Events> = ArrayList()

    private var isDayViewSelected: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTheUltimateTryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getMonth(DateTime.now())

        if (prefs.accessToken!!.isEmpty()) {
            fetchAccessToken()
        }

        //doRefreshToken()

        setDaysView(1)

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
                        insertNewCalendarUsingApi("CP Clinic Diary")
                    }
                }
            }
        }
    }

    private fun insertNewCalendarUsingApi(newEventTitle: String) {
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
                        this@TheUltimateTry,
                        "$newEventTitle added as new calendar type.",
                        Toast.LENGTH_SHORT
                    ).show()
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

    override fun checkIfAPICalling(isLoading: Boolean) {
        showProgress(isLoading)
    }

}