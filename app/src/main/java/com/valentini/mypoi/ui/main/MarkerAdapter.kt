package com.valentini.mypoi.ui.main

import android.content.Context
import android.graphics.Color
import android.graphics.Color.parseColor
import android.graphics.PorterDuff
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.Marker
import com.google.android.material.internal.ContextUtils.getActivity
import com.valentini.mypoi.R

open class MarkerAdapter(context: Context, private val markerList: ArrayList<Marker>) :
    RecyclerView.Adapter<MarkerAdapter.MarkerViewHolder>() {

    private val context = context


    // Describes an item view and its place within the RecyclerView
    class MarkerViewHolder(context: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val markerTitleTextView: TextView = itemView.findViewById(R.id.marker_label_name)
        private val markerTypeTextView: TextView = itemView.findViewById(R.id.marker_label_type)
        private val markerLatTextView: TextView = itemView.findViewById(R.id.marker_label_latitude)
        private val markerLongTextView: TextView = itemView.findViewById(R.id.marker_label_longitude)
        private val markerIconView: ImageView = itemView.findViewById(R.id.marker_type_logo)

        private val context = context

        fun bind(marker: Marker) {

            val prop = marker.snippet!!.split("#")

            val id = prop[0] //Non utilizzato ma rende chiara la situazione
            val color = prop[1]
            val type = prop[2]

            markerTitleTextView.text = marker.title
            markerTypeTextView.text = type
            markerLatTextView.text = "Lat: ${marker.position.latitude}"
            markerLongTextView.text = "Lat: ${marker.position.longitude}"
            val image = context.resources.getIdentifier(
                        type, "drawable", context.applicationContext.packageName)
                //    ), Color.parseColor("#$color")))

            val drawable = ContextCompat.getDrawable(this.context, image)
            drawable!!.setColorFilter(parseColor("#$color"), PorterDuff.Mode.MULTIPLY)
            markerIconView.background = drawable
        }
    }

    // Returns a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarkerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.marker_item, parent, false)

        return MarkerViewHolder(context, view)
    }

    // Returns size of data list
    override fun getItemCount(): Int {
        return markerList.size
    }

    // Displays data at a certain position
    override fun onBindViewHolder(holder: MarkerViewHolder, position: Int) {
        holder.bind(markerList[position])
    }
}