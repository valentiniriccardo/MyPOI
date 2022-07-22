package com.valentini.mypoi

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.Marker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.valentini.mypoi.database.DatabaseHelper
import com.valentini.mypoi.databinding.ActivityMainBinding
import com.valentini.mypoi.ui.main.MapFragment
import com.valentini.mypoi.ui.main.MyPlacesFragment
import com.valentini.mypoi.ui.main.SectionsPagerAdapter


class MainActivity : AppCompatActivity() , OnMapReadyCallback {

    private val permissionCode = 1
    private var usePosition: Boolean = false

    lateinit var googleMapFragment: MapFragment

    lateinit var binding: ActivityMainBinding
    private lateinit var databaseHelper: DatabaseHelper
    val markerListTest = arrayListOf<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this@MainActivity.baseContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this@MainActivity.baseContext, "Fine location ok", Toast.LENGTH_SHORT).show()
            usePosition = true
            createEverything(usePosition)
        } else {
            requestFineLocationPermission()
        }


    }

    private fun insertInRecycleView(markerIndex: Int) {
        val myFragment = MyPlacesFragment()
        try {

            supportFragmentManager
                .beginTransaction()
                .add(R.id.constraintlayout, myFragment)
                .commitNowAllowingStateLoss()

            supportFragmentManager.executePendingTransactions() //FONDAMENTALE
        } catch (ex: RuntimeException) {

        }


        //this is fragment method, we call it from activity

        if (myFragment.isVisible) {
            myFragment.insertInRecycleView(markerIndex)
        }

    }

    private fun removeFromRecycleView(markerIndex: Int) {
        val myFragment = MyPlacesFragment()
        try {

            supportFragmentManager
                .beginTransaction()
                .add(R.id.constraintlayout, myFragment)
                .commitNowAllowingStateLoss()

            supportFragmentManager.executePendingTransactions() //FONDAMENTALE
        } catch (ex: RuntimeException) {

        }


        if (myFragment.isVisible) {
            myFragment.removeFromRecycleView(markerIndex)
        }

    }

    private fun updateInRecycleView(markerIndex: Int) {
        val myFragment = MyPlacesFragment()
        try {

            supportFragmentManager
                .beginTransaction()
                .add(R.id.constraintlayout, myFragment)
                .commitNowAllowingStateLoss()

            supportFragmentManager.executePendingTransactions() //FONDAMENTALE
        } catch (ex: RuntimeException) {

        }


        if (myFragment.isAdded) {
            myFragment.updateInRecycleView(markerIndex)
        }

    }

    fun insertMarkerInList(marker: Marker) {
        markerListTest.add(marker)
        insertInRecycleView(markerListTest.indexOf(marker))
    }

    fun removeMarkerInList(marker: Marker) {
        removeFromRecycleView(markerListTest.indexOf(marker))
        markerListTest.remove(marker)
    }

    fun updateMarkerInList(oldmarker: Marker, newmarker: Marker) {
        val index = markerListTest.indexOf(oldmarker)
        markerListTest[index] = newmarker
        updateInRecycleView(index)
    }


    private fun createEverything(usePosition: Boolean) {
        databaseHelper = DatabaseHelper(baseContext)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager, usePosition)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
        val fab: FloatingActionButton = binding.fab

        fab.setOnClickListener {
            goToMapTab()
        }
    }


    private fun requestFineLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            AlertDialog.Builder(this)
                .setTitle("Permission needed")
                .setMessage("Dai, dammi la posizione precisa")
                .setPositiveButton("Ok") { dialog, which ->
                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), permissionCode)
                }
                .setNegativeButton("Ok") { dialog, which ->
                    dialog.dismiss()
                }
                .create().show()
        } else {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), permissionCode)
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        usePosition = if (requestCode == permissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this@MainActivity, "Permission fine granted", Toast.LENGTH_SHORT).show()
                true
            } else {
                //Toast.makeText(this@MainActivity, "Permesso posizione non ottenuto", Toast.LENGTH_SHORT).show()
                false
            }

        } else {
            false
        }

        createEverything(usePosition)
    }


    private fun goToMapTab() {
        this@MainActivity.binding.tabs.getTabAt(1)
            ?.select() //Con questa funzione faccio lo switch alla mappa ovunque io sia
    }

    @SuppressLint("ResourceType")
    override fun onMapReady(p0: GoogleMap) {

    }

}