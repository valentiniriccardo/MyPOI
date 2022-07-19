package com.valentini.mypoi

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.valentini.mypoi.database.DatabaseHelper
import com.valentini.mypoi.databinding.ActivityMainBinding
import com.valentini.mypoi.ui.main.SectionsPagerAdapter


class MainActivity : AppCompatActivity() {

    private val FINELOCATION_PERMISSIONCODE = 1
    private var usePosition : Boolean = false

    private lateinit var binding: ActivityMainBinding
    lateinit var databaseHelper: DatabaseHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this@MainActivity.baseContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            //Toast.makeText(this@MainActivity.baseContext, "Fine location ok", Toast.LENGTH_SHORT).show()
            usePosition = true
            createEverything(usePosition)
        } else {
            requestFineLocationPermission()
        }

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
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
        {
            AlertDialog.Builder(this)
                .setTitle("Permission needed")
                .setMessage("Dai, dammi la posizione precisa")
                .setPositiveButton("Ok") {dialog, which ->
                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), FINELOCATION_PERMISSIONCODE)
                }
                .setNegativeButton("Ok") {dialog, which ->
                    dialog.dismiss()
                }
                .create().show()
        } else
        {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), FINELOCATION_PERMISSIONCODE)
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == FINELOCATION_PERMISSIONCODE)
        {
            usePosition =
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this@MainActivity, "Permission fine granted", Toast.LENGTH_SHORT).show()
                    true
                } else {
                    Toast.makeText(this@MainActivity, "Permesso posizione non ottenuto", Toast.LENGTH_SHORT).show()
                    false
                }

        } else {
            usePosition = false
        }

        createEverything(usePosition)
    }


    private fun goToMapTab() {
        this@MainActivity.binding.tabs.getTabAt(1)
            ?.select() //Con questa funzione faccio lo switch alla mappa ovunque io sia
    }

}