package com.valentini.mypoi.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Handler
import android.os.Looper
import com.google.android.gms.common.api.Response
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*
import java.util.concurrent.Executors

const val DATABASE_VERSION = 3
const val DATABASE_NAME = "sqlite_data.db"
const val TABLE_NAME = "myplaces"
const val COL_ID = "id"
const val COL_NAME = "name"
const val COL_LATITUDE = "latitude"
const val COL_LONGITUDE = "longitude"

open class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    private var db : SQLiteDatabase = this.writableDatabase

    //val result = db.insert(TABLE_NAME, null, null)

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $TABLE_NAME" +
                "(\n" +
                "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, \n" +
                "$COL_NAME varchar(255) NOT NULL, \n" +
                "$COL_LATITUDE DOUBLE, \n" +
                "$COL_LONGITUDE DOUBLE) \n" +
                ";")
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
        res.moveToFirst();
        while (!res.isAfterLast) {
            val tempmo =
                MarkerOptions().title(res.getString(res.getColumnIndex(COL_NAME))).position(
                    LatLng(
                        res.getDouble(
                            res.getColumnIndex(
                                COL_LATITUDE
                            )
                        ), res.getDouble(res.getColumnIndex(COL_LONGITUDE))
                    )
                )
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
            ls.add(tempmo)
            res.moveToNext();
        }
        return ls
    }
}