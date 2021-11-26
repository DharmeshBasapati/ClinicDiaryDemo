package com.app.clinicdiarydemo.ultimate

import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.CalendarContract.Events
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.clinicdiarydemo.R
import com.app.clinicdiarydemo.databinding.ActivityTheUltimateTryBinding
import com.app.clinicdiarydemo.network.builder.RetrofitBuilder
import com.app.clinicdiarydemo.network.model.InsertCalendarRequest
import com.app.clinicdiarydemo.network.model.InsertCalendarResponse
import com.app.clinicdiarydemo.network.services.APIServices
import com.app.clinicdiarydemo.ultimate.Constants.dateAndTimeFormatForAddingEventToCalendar
import com.app.clinicdiarydemo.ultimate.MyUtils.getDateNumber
import com.app.clinicdiarydemo.ultimate.MyUtils.getMonth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.joda.time.DateTime
import org.joda.time.LocalDate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.ArrayList


class TheUltimateTry : AppCompatActivity(), EventScrollListener {

    private lateinit var binding: ActivityTheUltimateTryBinding

    private val events: ArrayList<com.app.clinicdiarydemo.ultimate.Events> = ArrayList()

    private var isDayViewSelected: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTheUltimateTryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getMonth(DateTime.now())

        //getAccessToken()

        //makeOAuthToken()

        insertNewCalendarUsingApi()

        //addNewCalendarType()

        setDaysView(1)

        //syncEventsFromCalendar()

        Log.d(
            "TAG",
            "MORNING WALK START DATE & TIME: ${
                MyUtils.convertMillisToDateInString(
                    1637969400000,
                    dateAndTimeFormatForAddingEventToCalendar
                )
            }"
        )
        Log.d(
            "TAG",
            "MORNING WALK END DATE & TIME: ${
                MyUtils.convertMillisToDateInString(
                    1637973000000,
                    dateAndTimeFormatForAddingEventToCalendar
                )
            }"
        )
    }



    private fun makeOAuthToken(){

        val url = "https://accounts.google.com/o/oauth2/v2/"

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder().addInterceptor(interceptor)
            .build()

        val retro = Retrofit.Builder()
            .baseUrl(url).client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiServices = retro.create(APIServices::class.java)

        apiServices.getAccessToken().enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                Log.d("TAG", "onResponse: $response")
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                Log.d("TAG", "onFailure: ${t.message}")
            }

        })


    }

    private fun insertNewCalendarUsingApi() {

        RetrofitBuilder.focusApiServices.insertCalendarType(
            InsertCalendarRequest("Clinic Diary"),
            "AIzaSyCR0jAUvVLfbDot1hYCLTIJnupgdl78nJ4",
            "Bearer ya29.a0ARrdaM9BZnf4RmPxyKEQ0_x92-rqLZYGLht5Qs0e73Xmf0zAb0_Ta9dpqEs7DhLTB5ZowCXHs6ua2d4ToQnV5u3soFY6SP0ucOxv6Suj9a828wHioFKUBbZyara06bNncHqcg3pl-W18L7Dcmr5VIt-Yrolg"
        )
            .enqueue(object : Callback<InsertCalendarResponse> {
                override fun onResponse(
                    call: Call<InsertCalendarResponse>,
                    response: Response<InsertCalendarResponse>
                ) {
                    Log.d("TAG", "onResponse: $response")
                    Log.d("TAG", "onResponse - Body: ${response.body()}")
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

    private fun addNewCalendarType() {
        val event = ContentValues()
        event.put(CalendarContract.Calendars.NAME, "Vanilla")
        event.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, "VanillaCal")
        event.put(CalendarContract.Calendars.VISIBLE, 1)
        event.put(CalendarContract.Calendars.SYNC_EVENTS, 1)

        event.put(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
        event.put(CalendarContract.Calendars.ACCOUNT_NAME, "spacestem3@gmail.com")
        event.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)

        val baseUri: Uri =
            Uri.parse("content://com.android.calendar/calendars")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            contentResolver.update(baseUri, event, null)
        }

    }
}