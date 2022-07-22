package com.valentini.mypoi.ui.main

import android.content.Context
import android.graphics.Color.parseColor
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.valentini.mypoi.MainActivity
import com.valentini.mypoi.R

open class MarkerAdapter(private val context: Context, private val markerList: ArrayList<Marker>) : RecyclerView.Adapter<MarkerAdapter.MarkerViewHolder>()
{

    class MarkerViewHolder(private val context: Context, itemView: View) : RecyclerView.ViewHolder(itemView)
    {

        private val markerTitleTextView: TextView = itemView.findViewById(R.id.marker_label_name)
        private val markerTypeTextView: TextView = itemView.findViewById(R.id.marker_label_type)
        private val markerLatTextView: TextView = itemView.findViewById(R.id.marker_label_latitude)
        private val markerLongTextView: TextView = itemView.findViewById(R.id.marker_label_longitude)
        private val markerIconView: ImageView = itemView.findViewById(R.id.marker_type_logo)
        private val markerLayout: ConstraintLayout = itemView.findViewById(R.id.layoutItem)

        fun bind(marker: Marker)
        {

            val prop = marker.snippet!!.split("#")

            val id = prop[0] //Non utilizzato ma rende chiara la situazione
            val color = prop[1]
            val type = prop[2]

            val drawable = ContextCompat.getDrawable(this.context, context.resources.getIdentifier(type, "drawable", context.applicationContext.packageName))

            markerTitleTextView.text = marker.title
            markerTypeTextView.text = type
            markerLatTextView.text = String.format("Lat: %.4f", marker.position.latitude)
            markerLongTextView.text = String.format("Long: %.4f", marker.position.longitude)

            markerLayout.setOnClickListener(){


            }

            drawable!!.setColorFilter(parseColor("#$color"), PorterDuff.Mode.MULTIPLY)
            markerIconView.background = drawable


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarkerViewHolder
    {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.marker_item, parent, false)

        return MarkerViewHolder(context, view)
    }

    override fun getItemCount(): Int
    {
        return markerList.size
    }

    override fun onBindViewHolder(holder: MarkerViewHolder, position: Int)
    {
        holder.bind(markerList[position])
    }
}