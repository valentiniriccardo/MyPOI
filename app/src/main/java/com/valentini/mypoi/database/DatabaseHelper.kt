package com.valentini.mypoi.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.valentini.mypoi.MainActivity
import com.valentini.mypoi.R
import java.util.*
import java.util.concurrent.Executors


const val DATABASE_VERSION = 8
const val DATABASE_NAME = "sqlite_data.db"
const val TABLE_NAME = "myplaces"
const val COL_ID = "id"
const val COL_NAME = "name"
const val COL_LATITUDE = "latitude"
const val COL_LONGITUDE = "longitude"
const val COL_TYPE_NAME = "type"
//TESTING
const val COL_COLOR = "color"

open class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    private var db : SQLiteDatabase = this.writableDatabase

    //val result = db.insert(TABLE_NAME, null, null)

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $TABLE_NAME" +
                "( " +
                "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_NAME varchar(255) NOT NULL, " +
                "$COL_LATITUDE DOUBLE, " +
                "$COL_LONGITUDE DOUBLE, " +
                "$COL_TYPE_NAME varchar(64) NOT NULL, " +
                "$COL_COLOR INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertMarker(contentValues: ContentValues){
        //TODO
        db = this.writableDatabase
        db.insert(TABLE_NAME, null, contentValues)

    }

    @SuppressLint("Range") //INDAGARE //TODO PERCHé
    fun markersInitList() : LinkedList<MarkerOptions>
    {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        val ls = LinkedList<MarkerOptions>()
        db = this.readableDatabase
        val res: Cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)
        res.moveToFirst()

        while (!res.isAfterLast) {
            val tempmo =
                MarkerOptions()
                    .title(res.getString(res.getColumnIndex(COL_NAME)))
                    .position(LatLng(res.getDouble(res.getColumnIndex(COL_LATITUDE)), res.getDouble(res.getColumnIndex(COL_LONGITUDE))))
                    .snippet((res.getString(res.getColumnIndex(COL_TYPE_NAME))))
            ls.add(tempmo)
            res.moveToNext();
        }

        println(COL_TYPE_NAME)
        return ls
    }


}