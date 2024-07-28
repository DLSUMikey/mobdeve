package com.example.posystem2

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import java.util.Date

class DatabaseHandler(context: Context) {
    private val dbHelper: MyDbHelper = MyDbHelper(context)

    fun addOrder(order: OrderModel): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DbReferences.COLUMN_ORDER_DATE, order.orderDate.time)
            put(DbReferences.COLUMN_TOTAL_AMOUNT, order.totalAmount)
        }
        val orderId = db.insert(DbReferences.TABLE_ORDERS, null, values)
        order.items.forEach { item ->
            addItem(orderId.toInt(), item)
        }
        db.close()
        return orderId
    }

    private fun addItem(orderId: Int, item: ItemModel): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DbReferences.COLUMN_IMAGE_ID, item.imageId)
            put(DbReferences.COLUMN_ITEM_NAME, item.itemName)
            put(DbReferences.COLUMN_ITEM_PRICE, item.itemPrice)
            put(DbReferences.COLUMN_ORDER_ID_FK, orderId)
        }
        val itemId = db.insert(DbReferences.TABLE_ITEMS, null, values)
        db.close()
        return itemId
    }

    fun getAllOrders(): List<OrderModel> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DbReferences.TABLE_ORDERS, null, null, null, null, null, null)
        val orders = mutableListOf<OrderModel>()
        while (cursor.moveToNext()) {
            val orderId = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ORDER_ID))
            val orderDate =
                Date(cursor.getLong(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ORDER_DATE)))
            val totalAmount =
                cursor.getDouble(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_TOTAL_AMOUNT))
            val items = getItemsByOrderId(orderId)
            orders.add(OrderModel(orderId, orderDate, totalAmount, items))
        }
        cursor.close()
        db.close()
        return orders
    }

    private fun getItemsByOrderId(orderId: Int): List<ItemModel> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DbReferences.TABLE_ITEMS,
            null,
            "${DbReferences.COLUMN_ORDER_ID_FK}=?",
            arrayOf(orderId.toString()),
            null,
            null,
            null
        )
        val items = mutableListOf<ItemModel>()
        while (cursor.moveToNext()) {
            val item = ItemModel(
                imageId = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_IMAGE_ID)),
                itemName = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ITEM_NAME)),
                itemPrice = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ITEM_PRICE))
            )
            items.add(item)
        }
        cursor.close()
        db.close()
        return items
    }

    fun addDummyProfiles() {
        val profiles = listOf(
            ProfileModel(0, "john.doe@example.com", "password123"),
            ProfileModel(0, "jane.smith@example.com", "password456"),
            ProfileModel(0, "mike.jones@example.com", "password789")
        )
        profiles.forEach { profile ->
            addProfile(profile)
        }
    }

    fun addProfile(profile: ProfileModel): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DbReferences.COLUMN_EMAIL, profile.email)
            put(DbReferences.COLUMN_PASSWORD, profile.password)
        }
        val profileId = db.insert(DbReferences.TABLE_PROFILE, null, values)
        db.close()
        return profileId
    }

    fun getProfileByEmail(email: String): ProfileModel? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DbReferences.TABLE_PROFILE, null, "${DbReferences.COLUMN_EMAIL}=?", arrayOf(email), null, null, null)
        return if (cursor.moveToFirst()) {
            val profile = ProfileModel(
                profileId = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_PROFILE_ID)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_EMAIL)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_PASSWORD))
            )
            cursor.close()
            db.close()
            profile
        } else {
            cursor.close()
            db.close()
            null
        }
    }

    fun getAllProfiles(): List<ProfileModel> {
        val profiles = mutableListOf<ProfileModel>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(DbReferences.TABLE_PROFILE, null, null, null, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                val profile = ProfileModel(
                    profileId = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_PROFILE_ID)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_EMAIL)),
                    password = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_PASSWORD))
                )
                profiles.add(profile)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return profiles
    }

    fun updateProfile(profile: ProfileModel): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DbReferences.COLUMN_EMAIL, profile.email)
            put(DbReferences.COLUMN_PASSWORD, profile.password)
        }
        val rowsUpdated = db.update(
            DbReferences.TABLE_PROFILE,
            values,
            "${DbReferences.COLUMN_PROFILE_ID}=?",
            arrayOf(profile.profileId.toString())
        )
        db.close()
        return rowsUpdated
    }

    fun deleteProfile(profileId: Int): Int {
        val db = dbHelper.writableDatabase
        val rowsDeleted = db.delete(
            DbReferences.TABLE_PROFILE,
            "${DbReferences.COLUMN_PROFILE_ID}=?",
            arrayOf(profileId.toString())
        )
        db.close()
        return rowsDeleted
    }
}

