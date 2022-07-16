package com.valentini.mypoi.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.valentini.mypoi.R
import java.lang.RuntimeException

private val TAB_TITLES = arrayOf(
    R.string.tab_home,
    R.string.tab_map,
    R.string.tab_myplaces,
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> {
                //Toast.makeText(context, "Sezione 1", Toast.LENGTH_SHORT).show()
                return HomeFragment.newInstance(position + 1) //+1 perché il tab fa 0,1,2,3...
            }
            1 -> {
                //Toast.makeText(context, "Sezione 2", Toast.LENGTH_SHORT).show()
                return MapFragment.newInstance(position + 1) //+1 perché il tab fa 0,1,2,3...
            }
            2 -> {
                //Toast.makeText(context, "Sezione 3", Toast.LENGTH_SHORT).show()
                return MyPlacesFragment.newInstance(position + 1)
            }
            else -> {
                throw RuntimeException("Tab non esistente")
            }
        }
        // getItem is called to instantiate the fragment for the given page.
        // Return a MyPlacesFragment (defined as a static inner class below).
    }

    override fun getPageTitle(position: Int): CharSequence {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return TAB_TITLES.size
    }
}