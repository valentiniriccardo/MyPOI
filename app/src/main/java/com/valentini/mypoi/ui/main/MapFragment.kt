package com.valentini.mypoi.ui.main

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
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
import com.valentini.mypoi.databinding.MapFragmentBinding
import com.valentini.mypoi.databinding.MyplacesFragmentBinding
import java.util.*


class MapFragment : OnMapReadyCallback, Fragment() {

    lateinit var googleMap: GoogleMap

    private lateinit var pageViewModel: PageViewModel
    private var _binding: MapFragmentBinding? = null
    private var _myplacesbinding : MyplacesFragmentBinding? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentMarker: Marker? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MapFragmentBinding.inflate(inflater, container, false)
        _binding!!.gotoFab.hide()
            Toast.makeText(requireContext(), "Sto creando!", Toast.LENGTH_SHORT).show()
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment? //IMPORTANTE CHE SIA CHILD E NON PARENT
        mapFragment?.getMapAsync(this@MapFragment)
        _binding!!.gpsFab.setOnClickListener {
            getMyPosition() //Se clicco sull'icona vado alle mappe
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        this.googleMap = googleMap
        val success =
            googleMap.setMapStyle(MapStyleOptions(resources.getString(R.string.style_json)))
        if (!success) {
            Log.e(TAG, "Style parsing failed.");
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
            _binding!!.gotoFab.show()
            val markerName = marker.title
            Toast.makeText(requireContext(), "Clicked location is $markerName", Toast.LENGTH_SHORT).show()
            false
        }


        this.googleMap.isMyLocationEnabled = true
        this.googleMap.uiSettings.isCompassEnabled = true
        this.googleMap.uiSettings.isMyLocationButtonEnabled = false
        this.googleMap.uiSettings.isIndoorLevelPickerEnabled = true
        this.googleMap.uiSettings.isMapToolbarEnabled = false
        this.googleMap.setOnMapClickListener { point -> inserisciPunto(point)
            currentMarker = null
            _binding!!.gotoFab.hide()
            Toast.makeText(requireContext(), "Cliccato sulla mappa", Toast.LENGTH_SHORT).show()
        }
        _binding!!.gotoFab.setOnClickListener {
            getMeToMarker(currentMarker!!)
        }

        //Toast.makeText(this@MapFragment.context, "Ciaaooo pronto!", Toast.LENGTH_SHORT).show()
        //googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
        //TEST
        val como = LatLng(45.808060, 9.085176)
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(12.0f))
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(como))


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
            sendDialogDataToActivity(editText.text.toString())
            if (editText.text.isNotEmpty())
            {
                val marker = MarkerOptions().position(LatLng(point.latitude, point.longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                    .title(editText.text.toString()).snippet("we")
                googleMap.addMarker(marker)
            }
        }

        builder.setNegativeButton("Annulla") { dialog, which ->
        }
        // create and show
        // the alert dialog
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()

        /*val alert: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())

        alert.setTitle("Nuovo posto")
        alert.setMessage("Inserisci un nome per il posto")

        val input = EditText(this.requireContext())
        input.includeFontPadding = true
        input.typeface = Typeface.DEFAULT_BOLD
        var nomeBuca: String
        alert.setView(input)
        alert.setCancelable(false)
        alert.setPositiveButton("Fatto") { dialog, whichButton ->
            val value: Editable? = input.text
            nomeBuca = value.toString()
            if (nomeBuca != "") {
                val marker = MarkerOptions().position(LatLng(point.latitude, point.longitude))
                    .title(nomeBuca).snippet("we")
                googleMap.addMarker(marker)
            }
        }
        alert.setNegativeButton("Annulla",
            DialogInterface.OnClickListener { dialog, whichButton ->
                // Canceled.
            })
        alert.show()*/

    }

    private fun getMyPosition() {
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
            //public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location ->
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude,location.longitude), 16.0f), 1500, null)
            }

            //MarkerOptions().position(LatLng(ll.latitude, location.longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
            //    .title("Io").snippet("we")
            //googleMap.addMarker(marker)

    }

    private fun getMeToMarker(marker : Marker) {

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