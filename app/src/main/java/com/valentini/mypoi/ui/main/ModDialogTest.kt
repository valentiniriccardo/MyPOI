package com.valentini.mypoi.ui.main

import android.content.ContentValues
import android.graphics.Color
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.valentini.mypoi.MainActivity
import com.valentini.mypoi.R
import com.valentini.mypoi.database.*

class ModDialogTest(activity: MainActivity, point: LatLng) : AlertDialog.Builder(activity)
{
    private var point : LatLng;
    init {
        val databaseHelper = DatabaseHelper(context)
        this.point = point
        this.setTitle("Nuova posizione")

        // set the custom layout
        val customLayout: View = activity.layoutInflater.inflate(R.layout.add_place_dialog, null) //todo check
        this.setView(customLayout)
        this.setPositiveButton("OK") { dialog, which -> // send data from the

            val editText = customLayout.findViewById<EditText>(R.id.editText)
            editText.isSingleLine = true

            if (editText.text.isNotEmpty()) {
                val rb = customLayout.findViewById<RadioButton>(
                    customLayout.findViewById<RadioGroup>(R.id.options_list).checkedRadioButtonId
                )

                val color = rb.tooltipText.toString().substringAfter("#")
                val type = rb.tooltipText.toString().substringBefore("#")

                val marker = MarkerOptions().position(LatLng(point.latitude, point.longitude))
                    .icon(
                        UtilityClass.bitmapDescriptorFromVector(activity, context.resources.getIdentifier(
                                type, "drawable", context.packageName
                            ), Color.parseColor("#$color")
                        )
                    )
                    .title(editText.text.toString())

                val contentValues = ContentValues()
                contentValues.put(COL_NAME, marker.title)
                contentValues.put(COL_LATITUDE, marker.position.latitude)
                contentValues.put(COL_LONGITUDE, marker.position.longitude)
                contentValues.put(COL_TYPE_NAME_COLOR, rb.tooltipText.toString()) //todo test
                ////Toast.makeText(requireContext(), "Testo: " + rb.tooltipText.toString().substringAfter("#"), //Toast.LENGTH_SHORT).show()
                val new_id: Int = databaseHelper.insertMarker(contentValues)
                marker.snippet("$new_id#$color#$type")
                val fragmentManager: FragmentManager = activity.supportFragmentManager
                /*val fragmentOne: SupportMapFragment = fragmentManager. .getFragmentByTag("fragmentOneTag")
                val map = activity.findViewById<SupportMapFragment>(R.id.map)
                    map.addMarker(marker)
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            marker.position.latitude,
                            marker.position.longitude
                        ), 16.0f
                    ), 2000, null
                )*/
            }
        }
    }
}