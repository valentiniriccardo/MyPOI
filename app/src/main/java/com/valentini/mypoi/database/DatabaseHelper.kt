package com.valentini.mypoi.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Handler
import android.os.Looper
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*
import java.util.concurrent.Executors


const val DATABASE_VERSION = 7
const val DATABASE_NAME = "my_poi.db"
const val TABLE_NAME = "myplaces"
const val COL_ID = "id"
const val COL_NAME = "name"
const val COL_LATITUDE = "latitude"
const val COL_LONGITUDE = "longitude"
const val COL_TYPE_NAME_COLOR = "typeandcolor"

open class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private var db: SQLiteDatabase = this.writableDatabase

    //val result = db.insert(TABLE_NAME, null, null)

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE $TABLE_NAME" +
                    "( " +
                    "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COL_NAME varchar(255) NOT NULL, " +
                    "$COL_LATITUDE DOUBLE, " +
                    "$COL_LONGITUDE DOUBLE, " +
                    "$COL_TYPE_NAME_COLOR varchar(64));"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    @SuppressLint("Range", "Recycle")
    fun insertMarker(contentValues: ContentValues): Int {
        //TODO
        db = this.writableDatabase
        db.insert(TABLE_NAME, null, contentValues)
        val res: Cursor = db.rawQuery(
            "SELECT $COL_ID FROM $TABLE_NAME WHERE $COL_NAME = '${contentValues.get(COL_NAME)}' AND $COL_LATITUDE = ${
                contentValues.get(
                    COL_LATITUDE
                )
            } AND $COL_LONGITUDE = ${
                contentValues.get(
                    COL_LONGITUDE
                )
            }", null
        )
        res.moveToFirst()
        return res.getInt(0)
    }

    @SuppressLint("Range") //Why is this like this?
    fun markersInitList(): LinkedList<MarkerOptions> {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        val ls = LinkedList<MarkerOptions>()
        db = this.readableDatabase
        val res: Cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)
        res.moveToFirst()

        while (!res.isAfterLast) {
            val markerOptionsToAdd =
                MarkerOptions()
                    .title(res.getString(res.getColumnIndex(COL_NAME)))
                    .position(LatLng(
                            res.getDouble(res.getColumnIndex(COL_LATITUDE)),
                            res.getDouble(res.getColumnIndex(COL_LONGITUDE))))
                    .snippet(res.getString(res.getColumnIndex(COL_ID)) + "#" + res.getString(
                            res.getColumnIndex(COL_TYPE_NAME_COLOR)))

            ls.add(markerOptionsToAdd)
            res.moveToNext()
        }
        res.close()
        println(COL_TYPE_NAME_COLOR)
        return ls
    }

    fun markerCoordinatesUpdate(marker: Marker) {
        db = this.writableDatabase
        db.execSQL(
            "UPDATE $TABLE_NAME SET $COL_LATITUDE = " + marker.position.latitude + ", $COL_LONGITUDE = " + marker.position.longitude + " WHERE $COL_ID = " + marker.snippet!!.substringBefore(
                "#"
            ) + ";"
        )
    }

    fun markerDataUpdate(marker: Marker) {
        db = this.writableDatabase
        db.execSQL(
            "UPDATE $TABLE_NAME SET $COL_NAME = '" + marker.title + "', $COL_TYPE_NAME_COLOR = '" + marker.snippet!!.substringAfter("#") + "' WHERE $COL_ID = " + marker.snippet!!.substringBefore(
                "#"
            ) + ";"
        )
    }

    fun markerRemove(marker: Marker) {
        db = this.writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME WHERE $COL_NAME = '" + marker.title + "' AND $COL_LATITUDE = " + marker.position.latitude + " AND $COL_LONGITUDE = " + marker.position.longitude + "")
    }


}