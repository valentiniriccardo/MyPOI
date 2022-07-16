package com.valentini.mypoi.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

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
}