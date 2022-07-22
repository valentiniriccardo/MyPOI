package com.valentini.mypoi.ui.main

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.util.DisplayMetrics
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.valentini.mypoi.MainActivity

open class UtilityClass {

    companion object {
        @JvmStatic
        fun bitmapDescriptorFromVector(activity: MainActivity, res: Int, color: Int): BitmapDescriptor {
            val displaymetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displaymetrics)
            val value = activity.resources.displayMetrics.density
            val vectorDrawable = ContextCompat.getDrawable(activity.baseContext, res)
            //Toast.makeText(activity.baseContext, "" + value, Toast.LENGTH_LONG).show()
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
    }
}