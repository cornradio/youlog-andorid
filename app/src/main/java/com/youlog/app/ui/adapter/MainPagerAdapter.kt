package com.youlog.app.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.youlog.app.ui.TimelineFragment
import com.youlog.app.ui.MiniViewFragment

class MainPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 2
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TimelineFragment()
            1 -> MiniViewFragment()
            else -> TimelineFragment()
        }
    }
}

