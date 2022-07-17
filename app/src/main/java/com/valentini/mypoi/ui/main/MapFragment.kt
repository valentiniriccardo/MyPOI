package com.valentini.mypoi.ui.main

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.valentini.mypoi.R
import com.valentini.mypoi.database.COL_LATITUDE
import com.valentini.mypoi.database.COL_LONGITUDE
import com.valentini.mypoi.database.COL_NAME
import com.valentini.mypoi.database.DatabaseHelper
import com.valentini.mypoi.databinding.MapFragmentBinding
import java.util.*


class MapFragment : OnMapReadyCallback, Fragment() {

    lateinit var googleMap: GoogleMap

    private lateinit var pageViewModel: PageViewModel
    private var mapFragmentBinding: MapFragmentBinding? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentMarker: Marker? = null
    lateinit var databaseHelper: DatabaseHelper
    private val binding get() = mapFragmentBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }
        databaseHelper = DatabaseHelper(requireContext())

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mapFragmentBinding = MapFragmentBinding.inflate(inflater, container, false)
        mapFragmentBinding!!.gotoFab.hide()


        Toast.makeText(requireContext(), "Sto creando!", Toast.LENGTH_SHORT).show()
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment? //IMPORTANTE CHE SIA CHILD E NON PARENT
        mapFragment?.getMapAsync(this@MapFragment)

        mapFragmentBinding!!.gpsFab.setOnClickListener {
        //Se clicco sull'icona vado alle mappe
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
            }
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                location.latitude,
                                location.longitude
                            ), 16.0f
                        ), 2000, null)
                } ?: kotlin.run {
                    // Handle Null case or Request periodic location update https://developer.android.com/training/location/receive-location-updates
                }
            }
        }

        return binding.root
    }


    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): MapFragment {
            return MapFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {


        this.googleMap = googleMap
        this.googleMap.isMyLocationEnabled = true
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {

            } // Night mode is not active, we're using the light theme
            Configuration.UI_MODE_NIGHT_YES -> {
                googleMap.setMapStyle(MapStyleOptions(resources.getString(R.string.style_json_dark)))
            } // Night mode is active, we're using dark theme
        }
        //val success = googleMap.setMapStyle(MapStyleOptions(resources.getString(R.string.style_json_dark)))

        val handler = Handler(Looper.getMainLooper())
        handler.post {
            val markersList = databaseHelper.markersInitList()
            for (t in markersList)
            {
                this.googleMap.addMarker(t)
            }
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        this.googleMap.setOnMarkerClickListener { marker -> // on marker click we are getting the title of our marker
            // which is clicked and displaying it in a toast message.
            currentMarker = marker
            mapFragmentBinding!!.gotoFab.show()
            val markerName = marker.title
            Toast.makeText(requireContext(), "Clicked location is $markerName", Toast.LENGTH_SHORT).show()
            false
        }



        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    /*    fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location ->
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            location.latitude,
                            location.longitude
                        ), 13.0f
                    ), 2000, null
                )
            }*/

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        location.latitude,
                        location.longitude
                    ), 13.0f
                ), 2000, null)
            } ?: kotlin.run {
                // Handle Null case or Request periodic location update https://developer.android.com/training/location/receive-location-updates
            }
        }


        this.googleMap.uiSettings.isCompassEnabled = true
        this.googleMap.uiSettings.isMyLocationButtonEnabled = false
        this.googleMap.uiSettings.isIndoorLevelPickerEnabled = true
        this.googleMap.uiSettings.isMapToolbarEnabled = false

        this.googleMap.setOnMapClickListener { point ->
            inserisciPunto(point)
            currentMarker = null
            mapFragmentBinding!!.gotoFab.hide()
            Toast.makeText(requireContext(), "Cliccato sulla mappa", Toast.LENGTH_SHORT).show()
        }
        mapFragmentBinding!!.gotoFab.setOnClickListener {
            getMeToMarker(currentMarker!!)
        }



        //Toast.makeText(this@MapFragment.context, "Ciaaooo pronto!", Toast.LENGTH_SHORT).show()
        //googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
        //TEST


        /*val como = LatLng(45.808060, 9.085176)
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(12.0f))
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(como))
        */

    }






    fun inserisciPunto(point: LatLng) {

        // Create an alert builder
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Name")

        // set the custom layout
        val customLayout: View = layoutInflater.inflate(R.layout.custom_dialog, null)
        builder.setView(customLayout)

        // add a button
        builder.setPositiveButton("OK") { dialog, which -> // send data from the
            // AlertDialog to the Activity
            val editText = customLayout.findViewById<EditText>(R.id.editText)
            editText.isSingleLine = true;
            sendDialogDataToActivity(editText.text.toString())
            if (editText.text.isNotEmpty()) {
                val marker = MarkerOptions().position(LatLng(point.latitude, point.longitude))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                    .title(editText.text.toString()).snippet("we")
                googleMap.addMarker(marker)
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            marker.position.latitude,
                            marker.position.longitude
                        ), 16.0f
                    ), 2000, null
                )
                val contentValues = ContentValues()
                contentValues.put(COL_NAME, marker.title)
                contentValues.put(COL_LATITUDE, marker.position.latitude)
                contentValues.put(COL_LONGITUDE, marker.position.longitude)
                databaseHelper.insertMarker(contentValues)
            }
        }

        builder.setNegativeButton("Annulla") { dialog, which ->
        }
        // create and show
        // the alert dialog
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()

    }


    private fun getMeToMarker(marker: Marker) {

        val uri: String = java.lang.String.format(
            Locale.ENGLISH,
            "geo:0,0?q=%f,%f(%s)",
            marker.position.latitude,
            marker.position.longitude,
            marker.title
        )
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        requireContext().startActivity(intent)
    }


    // Do something with the data
    // coming from the AlertDialog
    private fun sendDialogDataToActivity(data: String) {
        Toast.makeText(requireContext(), data, Toast.LENGTH_SHORT).show()
    }


}