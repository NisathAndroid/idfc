package com.test.idfc_demo

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private const val DATABASE_NAME = "IDFCCustomerLatLong.db"
private const val DATABASE_VERSION = 1
private const val TABLE_NAME = "CustomerData"
private const val COLUMN_ID = "id"
private const val COLUMN_LAT = "lat"
private const val COLUMN_LONG = "lon"

class DataBaseHelper(private val context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null,
    DATABASE_VERSION
) {
    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE $TABLE_NAME(
             $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                            $COLUMN_LAT TEXT,
                            $COLUMN_LONG TEXT
            )
        """.trimIndent()
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertData(latLanItems: LatLanItems): Long {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_LAT, latLanItems.lat)
            put(COLUMN_LONG, latLanItems.lan)
        }
        val result = db.insert(TABLE_NAME, null, contentValues)
        db.close()
        return result
    }

    fun getAllData(): MutableList<LatLanItems> {
        val list: MutableList<LatLanItems> = mutableListOf()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)
        if (cursor.moveToFirst()) {
            do {
                val latitude = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAT))
                val longitude = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LONG))
                list.add(LatLanItems(latitude, longitude, ""))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return list
    }
    fun deleteAllData(): Int {
        val db = writableDatabase
       val result= db.delete(TABLE_NAME, null,null)
        db.close()
        return result
    }

}