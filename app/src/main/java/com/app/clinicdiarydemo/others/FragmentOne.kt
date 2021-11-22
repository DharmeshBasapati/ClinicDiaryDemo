package com.app.clinicdiarydemo.others

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.widget.NestedScrollView
import com.app.clinicdiarydemo.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentOne.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentOne : Fragment() {
    private var scrollView: NestedScrollView? = null

    var listener: WeekFragmentListener? = null

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_one, container, false)
        scrollView = view.findViewById(R.id.nestedScrollView)

        scrollView?.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY -> checkScrollLimits(scrollY) })

        scrollView?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
              /*  if (fullHeight < scrollView.height) {
                    scrollView.layoutParams.height = fullHeight - 1.toInt()
                }

                val initialScrollY = (rowHeight * config.startWeeklyAt).toInt()
                updateScrollY(Math.max(listener?.getCurrScrollY() ?: 0, initialScrollY))*/
            }
        })

        return view
    }

    private fun checkScrollLimits(scrollY: Int) {
        Log.d("TAG", "checkScrollLimits(Week): Called - $scrollY")
        listener?.scrollTo(scrollY)
    }

    fun updateScrollY(y: Int) {
        Log.d("TAG", "updateScrollY(Week): Called - $y")
        scrollView?.scrollY = y
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentOne.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentOne().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


}