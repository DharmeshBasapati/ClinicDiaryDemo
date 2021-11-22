package com.app.clinicdiarydemo.others

import android.util.SparseArray
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter


class WeekAdapters(fm: FragmentManager, private val mListener: WeekFragmentListener) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val fragments = SparseArray<FragmentOne>()

    fun updateScrollY(pos: Int, y: Int) {
        if (pos - 1 >= 0) {
            fragments[pos - 1].updateScrollY(y)
        }
        if (pos + 1 < 6) {
            fragments[pos + 1].updateScrollY(y)
        }
    }

    override fun getItem(position: Int): Fragment {
        val fragment = FragmentOne()
        fragments.put(position, fragment)
        fragment.listener = mListener
        return fragment
    }

    override fun getCount(): Int {
        return 6
    }

}