package com.app.clinicdiarydemo.ultimate

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.app.clinicdiarydemo.R
import com.app.clinicdiarydemo.databinding.RowItemWeekGridBinding

class MyWeekViewAdapter(
    private var daysList: List<String>,
    private val daysCount: Int,
) :
    RecyclerView.Adapter<MyWeekViewAdapter.ViewHolder>() {
    inner class ViewHolder(var binding: RowItemWeekGridBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        RowItemWeekGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            binding.apply {

                myView.background =
                    AppCompatResources.getDrawable(
                        itemView.context,
                        R.drawable.custom_week_grid_white
                    )

                myView.setOnClickListener {

                    Log.d("TAG", "onBindViewHolder: CELL NUMBER - ${position + 1}")

                    when (daysList.size) {
                        1 -> {

                            Log.d("TAG", "onBindViewHolder: SELECTED DATE - ${daysList[0]}")

                            Log.d(
                                "TAG",
                                "onBindViewHolder: SELECTED TIME SLOT - ${
                                    MyUtils.doSomethingFor1DayView(position)
                                }"
                            )
                        }
                        3 -> {

                            Log.d(
                                "TAG",
                                "onBindViewHolder: SELECTED DATE - ${
                                    daysList[MyUtils.getSelectedDateFromCellNumber(
                                        position+1,
                                        3
                                    )]
                                }"
                            )

                            Log.d(
                                "TAG",
                                "onBindViewHolder: SELECTED TIME SLOT - ${
                                    MyUtils.doSomethingFor1or3DaysView(position, 3)
                                }"
                            )

                        }
                        7 -> {

                            Log.d(
                                "TAG",
                                "onBindViewHolder: SELECTED DATE - ${
                                    daysList[MyUtils.getSelectedDateFromCellNumber(
                                        position+1,
                                        7
                                    )]
                                }"
                            )

                            Log.d(
                                "TAG",
                                "onBindViewHolder: SELECTED TIME SLOT - ${
                                    MyUtils.doSomethingFor1or3DaysView(position, 7)
                                }"
                            )

                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        myView.background =
                            AppCompatResources.getDrawable(
                                itemView.context,
                                R.drawable.custom_week_grid_blue
                            )
                    }
                }
            }
        }
    }

    override fun getItemCount() = 24 * daysCount
}