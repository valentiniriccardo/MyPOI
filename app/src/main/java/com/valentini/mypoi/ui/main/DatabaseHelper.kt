package com.valentini.mypoi.ui.main

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

val DATABASE_VERSION = 1
val DATABASE_NAME = "sqlite_data.db"
val TABLE_NAME = "MyPlaces"
val COL_ID = "id"
val COL_NAME = "name"
val COL_LATITUDE = "latitude"
val COL_LONGITUDE = "longitude"

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){



    val db = this.writableDatabase

    //val result = db.insert(TABLE_NAME, null, null)

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE $TABLE_NAME" +
            "(\n" +
            "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, \n" +
            "$COL_NAME varchar(255) NOT NULL, \n" +
            "$COL_LATITUDE float, \n" +
            "$COL_LONGITUDE float) \n" +
            ";")

    }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }
}