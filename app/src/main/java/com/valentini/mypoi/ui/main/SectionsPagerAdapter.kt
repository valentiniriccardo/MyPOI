package com.valentini.mypoi.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.valentini.mypoi.R

private val TAB_TITLES = arrayOf(
    R.string.tab_home,
    R.string.tab_map,
    R.string.tab_myplaces,
)

class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                HomeFragment.newInstance(position + 1) //+1 perché il tab fa 0,1,2,3...
            }
            1 -> {
                MapFragment.newInstance(position + 1)
            }
            2 -> {
                MyPlacesFragment.newInstance(position + 1)
            }
            else -> {
                throw RuntimeException("Tab non esistente") //Eventualita' impossibile
            }
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return TAB_TITLES.size
    }
}