package com.example.posystem2

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDbHelper(context: Context) : SQLiteOpenHelper(context, DbReferences.DATABASE_NAME, null, DbReferences.DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(DbReferences.CREATE_ORDERS_TABLE)
        db.execSQL(DbReferences.CREATE_ITEMS_TABLE)
        db.execSQL(DbReferences.CREATE_PROFILE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(DbReferences.DROP_ORDERS_TABLE)
        db.execSQL(DbReferences.DROP_ITEMS_TABLE)
        db.execSQL(DbReferences.DROP_PROFILE_TABLE)
        onCreate(db)
    }
}
