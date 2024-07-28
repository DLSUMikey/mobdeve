package com.example.posystem2

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import at.favre.lib.crypto.bcrypt.BCrypt
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
            ProfileModel(0, "john.doe@example.com", BCrypt.withDefaults().hashToString(12, "password123".toCharArray())),
            ProfileModel(0, "jane.smith@example.com", BCrypt.withDefaults().hashToString(12, "password456".toCharArray())),
            ProfileModel(0, "mike.jones@example.com", BCrypt.withDefaults().hashToString(12, "password789".toCharArray()))
        )

        profiles.forEach { profile ->
            addProfile(profile, shouldHashPassword = false)
        }
    }

    fun addProfile(profile: ProfileModel, shouldHashPassword: Boolean = true): Long {
        val db = dbHelper.writableDatabase
        val passwordToStore = if (shouldHashPassword) {
            BCrypt.withDefaults().hashToString(12, profile.password.toCharArray())
        } else {
            profile.password
        }
        val values = ContentValues().apply {
            put(DbReferences.COLUMN_EMAIL, profile.email)
            put(DbReferences.COLUMN_PASSWORD, passwordToStore)
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
        val hashedPassword = BCrypt.withDefaults().hashToString(12, profile.password.toCharArray())
        val values = ContentValues().apply {
            put(DbReferences.COLUMN_EMAIL, profile.email)
            put(DbReferences.COLUMN_PASSWORD, hashedPassword)
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

    fun validateLogin(email: String, password: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DbReferences.TABLE_PROFILE,
            arrayOf(DbReferences.COLUMN_PASSWORD),
            "${DbReferences.COLUMN_EMAIL}=?",
            arrayOf(email),
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()) {
            val storedHash = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_PASSWORD))
            val result = BCrypt.verifyer().verify(password.toCharArray(), storedHash)
            cursor.close()
            db.close()
            result.verified
        } else {
            cursor.close()
            db.close()
            false
        }
    }

    fun addDummyItems() {
        val existingItems = getAllItems()
        if (existingItems.isEmpty()) {
            val items = listOf(
                ItemModel(1, "Item 1", 10),
                ItemModel(2, "Item 2", 20),
                ItemModel(3, "Item 3", 30)
            )
            items.forEach { item ->
                addItem(0, item) // Assuming orderId 0 for dummy items
            }
        }
    }

    fun getAllItems(): List<ItemModel> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DbReferences.TABLE_ITEMS, null, null, null, null, null, null)
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


}


