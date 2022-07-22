package com.valentini.mypoi.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.valentini.mypoi.MainActivity
import com.valentini.mypoi.R
import com.valentini.mypoi.R.font.inter_bold
import com.valentini.mypoi.R.id.options_list
import com.valentini.mypoi.database.*
import com.valentini.mypoi.databinding.MapFragmentBinding
import java.util.*


class MapFragment(private val canUsePositionPermission: Boolean) : OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, Fragment() {

    lateinit var googleMap: GoogleMap
    private lateinit var pageViewModel: PageViewModel
    var mapFragmentBinding: MapFragmentBinding? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var currentMarker: Marker? = null
    lateinit var databaseHelper: DatabaseHelper
    private val binding get() = mapFragmentBinding!!
    private var clicked = false
    private var canUsePosition = false

    companion object {

        private const val ARG_SECTION_NUMBER = "section_number"
        fun newInstance(sectionNumber: Int, usePosition: Boolean): MapFragment {
            return MapFragment(usePosition).apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.canUsePosition = canUsePositionPermission
        this.pageViewModel = ViewModelProvider(this)[PageViewModel::class.java].apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }
        this.databaseHelper = DatabaseHelper(requireContext())
    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        this.mapFragmentBinding = MapFragmentBinding.inflate(inflater, container, false)
        this.mapFragmentBinding!!.gotoFab.hide()


        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment? //IMPORTANTE CHE SIA CHILD E NON PARENT
        mapFragment?.getMapAsync(this@MapFragment)

        if (!canUsePositionPermission) mapFragmentBinding!!.gpsFab.hide()
        this.mapFragmentBinding!!.gpsFab.setOnClickListener {
            if (!canUsePositionPermission) {
                //Toast.makeText(this@MapFragment.context, "Permesso posizione negato", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                location.latitude,
                                location.longitude
                            ), 16.0f
                        ), 2000, null
                    )
                } ?: kotlin.run {
                    // Handle Null case or Request periodic location update https://developer.android.com/training/location/receive-location-updates
                }
            }
        }
        return binding.root
    }


    @SuppressLint("ResourceType")
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        if (canUsePositionPermission) this.googleMap.isMyLocationEnabled = true
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {
                googleMap.setMapStyle(MapStyleOptions(resources.getString(R.string.style_json_light)))
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                googleMap.setMapStyle(MapStyleOptions(resources.getString(R.string.style_json_dark)))
            }
        }


        this.googleMap.setOnMarkerDragListener(object : OnMarkerDragListener {

            lateinit var oldMarker: Marker

            override fun onMarkerDragStart(marker: Marker) {
                oldMarker = currentMarker!!
            }

            override fun onMarkerDrag(marker: Marker) {}
            override fun onMarkerDragEnd(marker: Marker) {
                currentMarker = marker //Serve per aggiornare la posizione
                (activity as MainActivity).updateMarkerInList(oldMarker, currentMarker!!)
                databaseHelper.markerCoordinatesUpdate(currentMarker!!)
            }
        })

        this.googleMap.setInfoWindowAdapter(object :
            GoogleMap.InfoWindowAdapter { //todo metodo a parte
            override fun getInfoWindow(marker: Marker): View {

                val info = LinearLayout(this@MapFragment.context)
                info.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_rounded_rectangle, null)
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
                snippet.isVisible = false //debug
                info.addView(title)
                info.addView(snippet)

                return info
            }

            override fun getInfoContents(marker: Marker): View? {
                return null
            }

        })
        this.googleMap.setOnInfoWindowLongClickListener {
            //todo
            it.isDraggable = false
            it.alpha = 1f
            currentMarker?.alpha = 1f
            currentMarker = it
            clicked = false
            modificaPunto(it)
            //setNullAndHide() //todo test
        }

        this.googleMap.setOnInfoWindowClickListener {

            if (!clicked) {
                this.currentMarker?.isDraggable = true
                this.currentMarker?.alpha = 0.33f
                clicked = true
            } else {
                this.currentMarker?.isDraggable = false
                this.currentMarker?.alpha = 1f
                clicked = false
            }

        }

        val handler = Handler(Looper.getMainLooper())
        handler.post {
            val markersList = databaseHelper.markersInitList()
            for (t in markersList) {
                val prop = t.snippet!!.split("#")

                val id = prop[0] //Non utilizzato ma rende chiara la situazione
                val color = prop[1]
                val type = prop[2]


                //Toast.makeText(requireContext(), "Colore: $color Tipo: $type", Toast.LENGTH_LONG).show()

                val m = this.googleMap.addMarker(
                    t.icon(
                        bitmapDescriptorFromVector(
                            resources.getIdentifier(
                                type, "drawable", requireActivity().applicationContext.packageName
                            ), Color.parseColor("#$color")
                        )
                    )
                        .snippet(t.snippet)
                )
                (activity as MainActivity).insertMarkerInList(m!!)
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
            //Ho già chiesto il permesso, se non accetta l'applicazione funzionerà in modalità senza tracciamento
            return
        }

        this.googleMap.setOnMarkerClickListener {
            if (currentMarker != it) {
                clicked = false
                this.currentMarker?.isDraggable = false
                currentMarker?.alpha = 1f
            }
            currentMarker = it
            mapFragmentBinding!!.gotoFab.show()
            false
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
            ////Toast.makeText(requireContext(), "Cliccato sulla mappa", //Toast.LENGTH_SHORT).show()
        }

        this.mapFragmentBinding!!.gotoFab.setOnClickListener {
            getMeToMarker(currentMarker!!)
        }

        (activity as MainActivity).googleMapFragment = this@MapFragment

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
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
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            location.latitude,
                            location.longitude
                        ), 13.0f
                    ), 2000, null
                )
            } ?: kotlin.run {
                // Handle Null case or Request periodic location update https://developer.android.com/training/location/receive-location-updates
            }
        }

    }

    //2.815 oneplus
    //1.65 mi4a


    private fun bitmapDescriptorFromVector(res: Int, color: Int): BitmapDescriptor {
        val displaymetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displaymetrics)
        val value = resources.displayMetrics.density
        val vectorDrawable = ContextCompat.getDrawable(this.requireContext(), res)
        //Toast.makeText(requireContext(), "" + value, Toast.LENGTH_LONG).show()
        vectorDrawable!!.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
        vectorDrawable.setBounds(
            0,
            0,
            44 * value.toInt(),
            44 * value.toInt()
        )//vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(
            44 * value.toInt(),
            44 * value.toInt() /*vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight,*/,
            Bitmap.Config.ARGB_8888
        )
        vectorDrawable.draw(Canvas(bitmap))
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun inserisciPunto(point: LatLng) {

        val dialog1: AlertDialog = AlertDialog.Builder(context)
            .setTitle(R.string.app_name)
            .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
            .setNegativeButton(android.R.string.cancel, null)
            .setCancelable(false)
            .create()

        val customLayout: View = layoutInflater.inflate(R.layout.add_place_dialog, null)
        dialog1.setView(customLayout)
        val editText = customLayout.findViewById<EditText>(R.id.addplace_edittext)

        dialog1.setOnShowListener {
            val button_neg = (dialog1 as AlertDialog).getButton(AlertDialog.BUTTON_NEGATIVE)
            button_neg.setOnClickListener {
                dialog1.dismiss()
            }
            val button_pos = (dialog1 as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            button_pos.setOnClickListener { // TODO Do something


                //editText.isSingleLine = true

                if (editText.text.toString() != "") {
                    val rb = customLayout.findViewById<RadioButton>(
                        customLayout.findViewById<RadioGroup>(options_list).checkedRadioButtonId
                    )

                    val color = rb.tooltipText.toString().substringBefore("#")
                    val type = rb.tooltipText.toString().substringAfter("#")

                    val marker = MarkerOptions().position(LatLng(point.latitude, point.longitude))
                        .icon(
                            bitmapDescriptorFromVector(
                                resources.getIdentifier(
                                    type, "drawable", requireActivity().applicationContext.packageName
                                ), Color.parseColor("#$color")
                            )
                        )
                        .title(editText.text.toString())


                    val contentValues = ContentValues()
                    contentValues.put(COL_NAME, marker.title)
                    contentValues.put(COL_LATITUDE, marker.position.latitude)
                    contentValues.put(COL_LONGITUDE, marker.position.longitude)
                    contentValues.put(COL_TYPE_NAME_COLOR, rb.tooltipText.toString())
                    val new_id: Int = databaseHelper.insertMarker(contentValues)
                    marker.snippet("$new_id#$color#$type")

                    val new_marker = googleMap.addMarker(marker)
                    googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                marker.position.latitude,
                                marker.position.longitude
                            ), 16.0f
                        ), 2000, null
                    )
                    (activity as MainActivity).insertMarkerInList(new_marker!!)
                    dialog1.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Il nome non può essere vuoto", Toast.LENGTH_LONG).show()
                }
            }

        }
        dialog1.show()


       /*
        // Create an alert builder
        val builder = AlertDialog.Builder(requireContext()) //R.style.AlertDialogStyle)

        builder.setTitle("Nuova posizione")
        builder.create()

        // set the custom layout
        val customLayout: View = layoutInflater.inflate(R.layout.add_place_dialog, null)
        builder.setView(customLayout)



        builder.setPositiveButton("OK") { dialog, which -> // send data from the


            val editText = customLayout.findViewById<EditText>(R.id.editText)
            editText.isSingleLine = true

            if (editText.text.isNotEmpty()) {
                val rb = customLayout.findViewById<RadioButton>(
                    customLayout.findViewById<RadioGroup>(options_list).checkedRadioButtonId
                )

                val color = rb.tooltipText.toString().substringBefore("#")
                val type = rb.tooltipText.toString().substringAfter("#")

                val marker = MarkerOptions().position(LatLng(point.latitude, point.longitude))
                    .icon(
                        bitmapDescriptorFromVector(
                            resources.getIdentifier(
                                type, "drawable", requireActivity().applicationContext.packageName
                            ), Color.parseColor("#$color")
                        )
                    )
                    .title(editText.text.toString())


                val contentValues = ContentValues()
                contentValues.put(COL_NAME, marker.title)
                contentValues.put(COL_LATITUDE, marker.position.latitude)
                contentValues.put(COL_LONGITUDE, marker.position.longitude)
                contentValues.put(COL_TYPE_NAME_COLOR, rb.tooltipText.toString())
                val new_id: Int = databaseHelper.insertMarker(contentValues)
                marker.snippet("$new_id#$color#$type")

                val new_marker = googleMap.addMarker(marker)
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            marker.position.latitude,
                            marker.position.longitude
                        ), 16.0f
                    ), 2000, null
                )
                (activity as MainActivity).insertMarkerInList(new_marker!!)
                //dialog.dismiss()
            } else
            {
                dialog.show()
                Toast.makeText(requireContext(), "Fro", Toast.LENGTH_SHORT).show()
           }

        }



        builder.setNegativeButton("Annulla") { dialog, which ->
            dialog.dismiss()
        }



        val dialog = builder.create()

        dialog.setCancelable(false)
        dialog.show()*/


    }


    private fun modificaPunto(oldmarker: Marker) {

        val dialog1: AlertDialog = AlertDialog.Builder(context)
            .setTitle(R.string.app_name)
            .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
            .setNegativeButton(android.R.string.cancel, null)
            .setNeutralButton("Elimina", null)
            .setCancelable(false)
            .create()
        val customLayout: View = layoutInflater.inflate(R.layout.modify_place_dialog, null)
        dialog1.setView(customLayout)
        val editText = customLayout.findViewById<EditText>(R.id.addplace_edittext)
        editText.setText(oldmarker.title)

        val id = this.resources.getIdentifier("R.id.rb_mod_" + oldmarker.snippet!!.split("#")[2], "id", requireActivity().packageName)

        val viewId = context?.resources?.getIdentifier("rb_mod_" + oldmarker.snippet!!.split("#")[2], "id", context?.packageName)

        val checked_radio_button = customLayout.findViewById<RadioButton>(viewId!!)
        checked_radio_button?.isChecked = true

        dialog1.setOnShowListener {
            val button_neg = (dialog1 as AlertDialog).getButton(AlertDialog.BUTTON_NEGATIVE)
            button_neg.setOnClickListener {
                dialog1.dismiss()
            }

            val button_neu = (dialog1 as AlertDialog).getButton(AlertDialog.BUTTON_NEUTRAL)
            button_neu.setOnClickListener {
                currentMarker?.isVisible = false
                clicked = false
                databaseHelper.markerRemove(currentMarker!!)
                (activity as MainActivity).removeMarkerInList(currentMarker!!)
                currentMarker!!.remove()
                setNullAndHide()
                dialog1.dismiss()

            }

            val button_pos = (dialog1 as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            button_pos.setOnClickListener { // TODO Do something


                //editText.isSingleLine = true

                if (editText.text.toString() != "") {
                    val rb = customLayout.findViewById<RadioButton>(
                        customLayout.findViewById<RadioGroup>(options_list).checkedRadioButtonId
                    )
                    val id_ = oldmarker.snippet!!.substringBefore("#")
                    val color = rb.tooltipText.toString().split("#")[0]
                    val type = rb.tooltipText.toString().split("#")[1]

                    oldmarker.isVisible = false
                    val markeroptions = MarkerOptions().position(LatLng(oldmarker.position.latitude, oldmarker.position.longitude))
                        .icon(
                            bitmapDescriptorFromVector(
                                resources.getIdentifier(
                                    type, "drawable", requireActivity().applicationContext.packageName
                                ), Color.parseColor("#$color")
                            )
                        )
                        .title(editText.text.toString())
                        .snippet("$id_#$color#$type")
                    val contentValues = ContentValues()
                    contentValues.put(COL_NAME, markeroptions.title)
                    contentValues.put(COL_ID, markeroptions.snippet!!.substringBefore("#"))
                    contentValues.put(COL_TYPE_NAME_COLOR, rb.tooltipText.toString()) //todo test
                    ////Toast.makeText(requireContext(), "Testo: " + rb.tooltipText.toString().substringAfter("#"), //Toast.LENGTH_SHORT).show()
                    val newmarker = googleMap.addMarker(markeroptions)
                    databaseHelper.markerDataUpdate(newmarker!!)

                    (activity as MainActivity).updateMarkerInList(oldmarker, newmarker)
                    oldmarker.remove()

                    googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                newmarker.position.latitude,
                                newmarker.position.longitude
                            ), 15f
                        ), 1000, null
                    )
                    dialog1.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Il nome non può essere vuoto", Toast.LENGTH_LONG).show()
                }
            }

        }
        dialog1.show()

        // Create an alert builder
      /*  val builder = AlertDialog.Builder(requireContext()) //R.style.AlertDialogStyle)

        builder.setTitle("Modifica posizione")

        // set the custom layout
        val customLayout: View = layoutInflater.inflate(R.layout.modify_place_dialog, null)
        builder.setView(customLayout)

        val name_editText = customLayout.findViewById<EditText>(R.id.addplace_edittext)

        name_editText.setText(oldmarker.title)

        //val id = this.resources.getIdentifier("R.id.rb_mod_" + oldmarker.snippet!!.split("#")[2], "id", requireActivity().packageName)

        val viewId = context?.resources?.getIdentifier("rb_mod_" + oldmarker.snippet!!.split("#")[2], "id", context?.packageName)

        val checked_radio_button = customLayout.findViewById<RadioButton>(viewId!!)
        checked_radio_button?.isChecked = true

        //Toast.makeText(requireContext(), "$viewId + ${R.id.rb_mod_casa}", Toast.LENGTH_LONG).show()
        builder.setPositiveButton("OK") { dialog, which -> // send data from the

            //name_editText.setText("This sets the text.", TextView.BufferType.EDITABLE)

            name_editText.isSingleLine = true

            if (name_editText.text.isNotEmpty()) {
                val rb = customLayout.findViewById<RadioButton>(
                    customLayout.findViewById<RadioGroup>(options_list).checkedRadioButtonId
                )
                val id = oldmarker.snippet!!.substringBefore("#")
                val color = rb.tooltipText.toString().split("#")[0]
                val type = rb.tooltipText.toString().split("#")[1]

                oldmarker.isVisible = false
                val markeroptions = MarkerOptions().position(LatLng(oldmarker.position.latitude, oldmarker.position.longitude))
                    .icon(
                        bitmapDescriptorFromVector(
                            resources.getIdentifier(
                                type, "drawable", requireActivity().applicationContext.packageName
                            ), Color.parseColor("#$color")
                        )
                    )
                    .title(name_editText.text.toString())
                    .snippet("$id#$color#$type")
                val contentValues = ContentValues()
                contentValues.put(COL_NAME, markeroptions.title)
                contentValues.put(COL_ID, markeroptions.snippet!!.substringBefore("#"))
                contentValues.put(COL_TYPE_NAME_COLOR, rb.tooltipText.toString()) //todo test
                ////Toast.makeText(requireContext(), "Testo: " + rb.tooltipText.toString().substringAfter("#"), //Toast.LENGTH_SHORT).show()
                val newmarker = googleMap.addMarker(markeroptions)
                databaseHelper.markerDataUpdate(newmarker!!)

                (activity as MainActivity).updateMarkerInList(oldmarker, newmarker)
                oldmarker.remove()

                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            newmarker.position.latitude,
                            newmarker.position.longitude
                        ), 16.0f
                    ), 1000, null
                )
            }
        }

        builder.setNeutralButton("Elimina") { dialog, which ->
            currentMarker?.isVisible = false
            clicked = false
            databaseHelper.markerRemove(currentMarker!!)
            (activity as MainActivity).removeMarkerInList(currentMarker!!)
            currentMarker!!.remove()
            setNullAndHide()
        }

        builder.setNegativeButton("Annulla") { dialog, which ->
            dialog.dismiss()
        }

        val dialog = builder.create()

        dialog.setCancelable(false)
        dialog.show()
*/
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
        ////Toast.makeText(requireContext(), data, //Toast.LENGTH_SHORT).show()
    }

    override fun onInfoWindowClick(p0: Marker) {
        p0.snippet
    }

    fun setNullAndHide() {
        currentMarker = null
        mapFragmentBinding!!.gotoFab.hide()
    }

}