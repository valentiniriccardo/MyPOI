package com.valentini.mypoi

import android.content.ContentValues
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.valentini.mypoi.databinding.ActivityMainBinding
import com.valentini.mypoi.ui.main.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        databaseHelper = DatabaseHelper(baseContext)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)


        val fab: FloatingActionButton = binding.fab

        fab.setOnClickListener {
            goToMapTab() //Se clicco sull'icona vado alle mappe
            val contentValues = ContentValues()
            contentValues.put(COL_NAME, "TEST")
            contentValues.put(COL_LATITUDE, "1.343545")
            contentValues.put(COL_LONGITUDE, "34.352326")
            databaseHelper.insertMarker(contentValues)
            Toast.makeText(this@MainActivity.baseContext, "DB creato", Toast.LENGTH_SHORT).show()
        }
    }

    private fun goToMapTab() {
        this@MainActivity.binding.tabs.getTabAt(1)?.select() //Con questa funzione faccio lo switch alla mappa ovunque io sia
    }

    /*object FeedReaderContract {
        // Table contents are grouped together in an anonymous object.
        object FeedEntry : BaseColumns {
            const val TABLE_NAME = "entry"
            const val COLUMN_NAME_TITLE = "title"
            const val COLUMN_NAME_SUBTITLE = "subtitle"
        }
    }*/


}