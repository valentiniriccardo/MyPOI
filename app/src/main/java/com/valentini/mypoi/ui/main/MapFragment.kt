package com.valentini.mypoi.ui.main

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.core.view.setPadding
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
import com.valentini.mypoi.R.font.inter_bold
import com.valentini.mypoi.R.id.goto_fab
import com.valentini.mypoi.R.id.options_list
import com.valentini.mypoi.database.*
import com.valentini.mypoi.databinding.MapFragmentBinding
import java.util.*


class MapFragment : OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, Fragment() {

    lateinit var googleMap: GoogleMap

    private lateinit var pageViewModel: PageViewModel
    private var mapFragmentBinding: MapFragmentBinding? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentMarker: Marker? = null
    lateinit var databaseHelper: DatabaseHelper
    private val binding get() = mapFragmentBinding!!
    private var clicked = false
    var old_color = -1

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
                googleMap.setMapStyle(MapStyleOptions(resources.getString(R.string.style_json_light)))
            } // Night mode is not active, we're using the light theme
            Configuration.UI_MODE_NIGHT_YES -> {
                googleMap.setMapStyle(MapStyleOptions(resources.getString(R.string.style_json_dark)))
            } // Night mode is active, we're using dark theme
        }
        //val success = googleMap.setMapStyle(MapStyleOptions(resources.getString(R.string.style_json_dark)))


        this.googleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter { //todo metodo a parte
            override fun getInfoWindow(marker: Marker): View {

                val info = LinearLayout(this@MapFragment.context)

                info.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_rounded_rectangle, null)
                //info.setPadding(24)
                info.setPadding(24)
                info.clipToOutline = true
                info.orientation = LinearLayout.VERTICAL
                val title = TextView(this@MapFragment.context)
                title.setTextColor(resources.getColor(R.color.teal_900, null))
                title.gravity = Gravity.CENTER
                title.typeface = ResourcesCompat.getFont(requireContext(), inter_bold)
                title.text = marker.title
                title.textSize = 16F

                val snippet = TextView(this@MapFragment.context)
                snippet.text = marker.snippet!!.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(
                        Locale.ROOT
                    ) else it.toString()
                }
                snippet.setTextColor(Color.GRAY)
                snippet.gravity = Gravity.CENTER
                snippet.textSize = 13F
                //snippet.isVisible = false todo mod 1
                info.addView(title)
                info.addView(snippet)

                return info
            }

            override fun getInfoContents(marker: Marker): View? {
                return null
            }

            fun changeBackground()
            {
                //todo
            }
        })
        this.googleMap.setOnInfoWindowLongClickListener {
            currentMarker?.remove()
            clicked = false
            currentMarker?.let { databaseHelper.markerRemove(it) }
            setNullAndHide()
        }

        this.googleMap.setOnInfoWindowClickListener {


            if (!clicked)
            {
                this.currentMarker?.isDraggable = true
                this.currentMarker?.alpha = 0.33f
                clicked = true
            } else
            {
                this.currentMarker?.isDraggable = false
                this.currentMarker?.alpha = 1f
                clicked = false
            }

            //setNullAndHide()
        }

        val handler = Handler(Looper.getMainLooper())
        handler.post {
            val markersList = databaseHelper.markersInitList()
            for (t in markersList)
            {
                val color = "#" + t.snippet!!.substringAfter("#")
                val type =  t.snippet!!.substringBefore("#")

                this.googleMap.addMarker(
                    t.icon(bitmapDescriptorFromVector(resources.getIdentifier(type, "drawable", requireActivity().applicationContext.packageName
                ), Color.parseColor(color)))
                    .snippet(type))
                //Toast.makeText(requireContext(), type, Toast.LENGTH_SHORT).show()
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

        this.googleMap.setOnMarkerClickListener {
            if (currentMarker != it)
            {
                clicked = false
                this.currentMarker?.isDraggable = false
                currentMarker?.alpha = 1f
            }
            currentMarker = it
            mapFragmentBinding!!.gotoFab.show()
            val markerName = it.title
            //Toast.makeText(requireContext(), "Clicked location is $markerName", Toast.LENGTH_SHORT).show()
            false
        }




        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
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

        this.googleMap.setOnMapClickListener {

            this.currentMarker?.alpha = 1f
            this.currentMarker?.isDraggable = false
            clicked = false

            currentMarker = null
            mapFragmentBinding!!.gotoFab.hide()
        }

        this.googleMap.setOnMapLongClickListener {
            inserisciPunto(it)

            this.currentMarker?.alpha = 1f
            this.currentMarker?.isDraggable = false
            clicked = false

            currentMarker = null
            mapFragmentBinding!!.gotoFab.hide()
            //Toast.makeText(requireContext(), "Cliccato sulla mappa", Toast.LENGTH_SHORT).show()
        }

        this.mapFragmentBinding!!.gotoFab.setOnClickListener {
            getMeToMarker(currentMarker!!)
        }

    }


    private fun bitmapDescriptorFromVector(res: Int, color: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(this.requireContext(), res)
        vectorDrawable!!.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
        vectorDrawable.setBounds(0, 0, 96, 96)//vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(96, 96 /*vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight,*/, Bitmap.Config.ARGB_8888)
        vectorDrawable.draw(Canvas(bitmap))
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    /*private fun BitmapDescriptorFromVector(res: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(this.requireContext(), res)

        vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        vectorDrawable.draw(Canvas(bitmap))
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }*/



    private fun inserisciPunto(point: LatLng) {

        // Create an alert builder
        val builder = AlertDialog.Builder(requireContext()) //R.style.AlertDialogStyle)

        builder.setTitle("Nuova posizione")

        // set the custom layout
        val customLayout: View = layoutInflater.inflate(R.layout.custom_dialog, null)

        //builder.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_rounded_rectangle, null)
        builder.setView(customLayout)

        builder.setPositiveButton("OK") { dialog, which -> // send data from the

            //ResourcesCompat.getDrawable(resources, R.drawable.ic_rounded_rectangle, null)
            // AlertDialog to the Activity
            val editText = customLayout.findViewById<EditText>(R.id.editText)
            editText.isSingleLine = true
           /* val res = context?.let { ContextCompat.getDrawable(it,resources.getIdentifier(r.tooltipText.toString(), "drawable", context?.packageName)) }

            val bitmap = res?.let { Bitmap.createBitmap(it.intrinsicWidth, it.intrinsicHeight, Bitmap.Config.ARGB_8888) }
*/
            //sendDialogDataToActivity(editText.text.toString())
            if (editText.text.isNotEmpty()) {
                val rb = customLayout.findViewById<RadioButton>(customLayout.findViewById<RadioGroup>(options_list).checkedRadioButtonId)

                val color = "#" + rb.tooltipText.toString().substringAfter("#")
                val type =  rb.tooltipText.toString().substringBefore("#")

                val marker = MarkerOptions().position(LatLng(point.latitude, point.longitude))
                    .icon(bitmapDescriptorFromVector(resources.getIdentifier(type, "drawable", requireActivity().applicationContext.packageName
                    ), Color.parseColor( color)))
                    .title(editText.text.toString()).snippet(type)
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
                contentValues.put(COL_TYPE_NAME, rb.tooltipText.toString()) //todo test
                //Toast.makeText(requireContext(), "Testo: " + rb.tooltipText.toString().substringAfter("#"), Toast.LENGTH_SHORT).show()
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
        //Toast.makeText(requireContext(), data, Toast.LENGTH_SHORT).show()
    }

    override fun onInfoWindowClick(p0: Marker) {
        p0.snippet
    }

    private fun setNullAndHide()
    {
        currentMarker = null
        mapFragmentBinding!!.gotoFab.hide()
    }

}