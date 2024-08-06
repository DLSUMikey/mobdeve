package com.example.posystem2

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import at.favre.lib.crypto.bcrypt.BCrypt
import java.util.Date

class DatabaseHandler(context: Context) {
    private val dbHelper: MyDbHelper = MyDbHelper(context)

    fun addOrder(order: OrderModel): Long {
        val db = dbHelper.writableDatabase
        var orderId: Long = -1

        db.beginTransaction()
        try {
            val values = ContentValues().apply {
                put(DbReferences.COLUMN_ORDER_DATE, order.orderDate.time)
                put(DbReferences.COLUMN_TOTAL_AMOUNT, order.totalAmount)
                put(DbReferences.COLUMN_STATUS, order.status)
                put(DbReferences.COLUMN_IS_DELETED, if (order.isDeleted) 1 else 0)
                put(DbReferences.COLUMN_EMPLOYEE_ID, order.employeeId)
            }
            orderId = db.insert(DbReferences.TABLE_ORDERS, null, values)

            if (orderId != -1L) {
                order.items.forEach { item ->
                    addItemToOrder(item.copy(orderId = orderId.toInt()), db)
                }
                db.setTransactionSuccessful()
            }
        } catch (e: Exception) {
            Log.e("DatabaseHandler", "Error adding order", e)
        } finally {
            db.endTransaction()
            db.close()
        }
        return orderId
    }

    private fun addItemToOrder(item: ItemModel, db: SQLiteDatabase): Long {
        val values = ContentValues().apply {
            put(DbReferences.COLUMN_ORDER_ID_FK, item.orderId)
            put(DbReferences.COLUMN_IMAGE_URI, item.imageUri)
            put(DbReferences.COLUMN_ITEM_NAME, item.itemName)
            put(DbReferences.COLUMN_ITEM_PRICE, item.itemPrice)
            put(DbReferences.COLUMN_ORDERED, 1)  // Mark the item as ordered
            put(DbReferences.COLUMN_QUANTITY, item.quantity)  // Set the quantity
        }
        return db.insert(DbReferences.TABLE_ITEMS, null, values)
    }

    fun addNewItem(item: ItemModel): Long {
        val db = dbHelper.writableDatabase
        val itemId = addItem(item, db)
        db.close()
        return itemId
    }

    private fun addItem(item: ItemModel, db: SQLiteDatabase): Long {
        val values = ContentValues().apply {
            put(DbReferences.COLUMN_ORDER_ID_FK, item.orderId)
            put(DbReferences.COLUMN_IMAGE_URI, item.imageUri)
            put(DbReferences.COLUMN_ITEM_NAME, item.itemName)
            put(DbReferences.COLUMN_ITEM_PRICE, item.itemPrice)
            put(DbReferences.COLUMN_ORDERED, 0)  // Ensure new items are marked as not ordered
            put(DbReferences.COLUMN_QUANTITY, item.quantity)  // Set the quantity
        }
        return db.insert(DbReferences.TABLE_ITEMS, null, values)
    }

    fun updateItem(item: ItemModel): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DbReferences.COLUMN_IMAGE_URI, item.imageUri)
            put(DbReferences.COLUMN_ITEM_NAME, item.itemName)
            put(DbReferences.COLUMN_ITEM_PRICE, item.itemPrice)
            put(DbReferences.COLUMN_ORDERED, if (item.ordered) 1 else 0)  // Update the ordered status
            put(DbReferences.COLUMN_QUANTITY, item.quantity)  // Update the quantity
        }
        val rowsUpdated = db.update(
            DbReferences.TABLE_ITEMS,
            values,
            "${DbReferences.COLUMN_ITEM_ID}=?",
            arrayOf(item.orderId.toString())
        )
        db.close()
        return rowsUpdated
    }

    fun getAllOrders(): List<OrderModel> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DbReferences.TABLE_ORDERS,
            null,
            "${DbReferences.COLUMN_IS_DELETED} = ?",
            arrayOf("0"),  // Only fetch non-deleted orders
            null,
            null,
            null
        )
        val orders = mutableListOf<OrderModel>()
        while (cursor.moveToNext()) {
            val orderId = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ORDER_ID))
            val orderDate = Date(cursor.getLong(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ORDER_DATE)))
            val totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_TOTAL_AMOUNT))
            val status = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_STATUS)) ?: "In Progress"
            val employeeId = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_EMPLOYEE_ID))
            val isDeleted = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_IS_DELETED)) == 1
            val items = getItemsByOrderId(orderId)
            orders.add(OrderModel(orderId, orderDate, totalAmount, items, status, isDeleted, employeeId))
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
                orderId = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ORDER_ID_FK)),
                imageUri = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_IMAGE_URI)),
                itemName = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ITEM_NAME)),
                itemPrice = cursor.getFloat(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ITEM_PRICE)),
                quantity = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_QUANTITY)),
                ordered = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ORDERED)) == 1
            )
            items.add(item)
        }
        cursor.close()
        db.close()
        return items
    }

    fun addDummyProfiles() {
        val profiles = listOf(
            ProfileModel(
                id = 0,
                email = "doe@gmail.com",
                password = BCrypt.withDefaults().hashToString(12, "12345".toCharArray()),
                firstName = "John",
                lastName = "Doe",
                phoneNumber = "123-456-7890",
                userType = "Admin"
            )
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
            put(DbReferences.COLUMN_FIRST_NAME, profile.firstName)
            put(DbReferences.COLUMN_LAST_NAME, profile.lastName)
            put(DbReferences.COLUMN_PHONE_NUMBER, profile.phoneNumber)
            put(DbReferences.COLUMN_USER_TYPE, profile.userType)
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
                id = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_PROFILE_ID)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_EMAIL)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_PASSWORD)),
                firstName = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_FIRST_NAME)),
                lastName = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_LAST_NAME)),
                phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_PHONE_NUMBER)),
                userType = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_USER_TYPE))
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

    fun getProfileById(profileId: Int): ProfileModel? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DbReferences.TABLE_PROFILE,
            null,
            "${DbReferences.COLUMN_PROFILE_ID}=?",
            arrayOf(profileId.toString()),
            null,
            null,
            null
        )
        return if (cursor.moveToFirst()) {
            val profile = ProfileModel(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_PROFILE_ID)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_EMAIL)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_PASSWORD)),
                firstName = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_FIRST_NAME)),
                lastName = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_LAST_NAME)),
                phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_PHONE_NUMBER)),
                userType = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_USER_TYPE))
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
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_PROFILE_ID)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_EMAIL)),
                    password = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_PASSWORD)),
                    firstName = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_FIRST_NAME)),
                    lastName = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_LAST_NAME)),
                    phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_PHONE_NUMBER)),
                    userType = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_USER_TYPE))
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
            // Only hash the password if it is not already hashed
            if (!profile.password.startsWith("$2a$")) {
                put(DbReferences.COLUMN_PASSWORD, BCrypt.withDefaults().hashToString(12, profile.password.toCharArray()))
            } else {
                put(DbReferences.COLUMN_PASSWORD, profile.password)
            }
            put(DbReferences.COLUMN_FIRST_NAME, profile.firstName)
            put(DbReferences.COLUMN_LAST_NAME, profile.lastName)
            put(DbReferences.COLUMN_PHONE_NUMBER, profile.phoneNumber)
            put(DbReferences.COLUMN_USER_TYPE, profile.userType)
        }
        val rowsUpdated = db.update(
            DbReferences.TABLE_PROFILE,
            values,
            "${DbReferences.COLUMN_PROFILE_ID}=?",
            arrayOf(profile.id.toString())
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
            val db = dbHelper.writableDatabase

            // Create a new order
            val orderValues = ContentValues().apply {
                put(DbReferences.COLUMN_ORDER_DATE, Date().time)
                put(DbReferences.COLUMN_TOTAL_AMOUNT, 60.0)
            }
            val orderId = db.insert(DbReferences.TABLE_ORDERS, null, orderValues).toInt()

            // Create dummy items and associate them with the order
            val items = listOf(
                ItemModel(orderId, "android.resource://com.example.posystem2/drawable/ic_launcher_background", "Item 1", 10.0f, 1),
                ItemModel(orderId, "android.resource://com.example.posystem2/drawable/ic_launcher_background", "Item 2", 20.0f, 1),
                ItemModel(orderId, "android.resource://com.example.posystem2/drawable/ic_launcher_background", "Item 3", 30.0f, 1)
            )
            items.forEach { item ->
                addItem(item, db)
            }
            db.close()
        }
    }

    fun getAllItems(): List<ItemModel> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DbReferences.TABLE_ITEMS, null, "ordered = ?", arrayOf("0"), null, null, null)
        val items = mutableListOf<ItemModel>()
        while (cursor.moveToNext()) {
            val item = ItemModel(
                orderId = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ORDER_ID_FK)),
                imageUri = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_IMAGE_URI)),
                itemName = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ITEM_NAME)),
                itemPrice = cursor.getFloat(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ITEM_PRICE)),
                quantity = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_QUANTITY)),  // Fetch the quantity
                ordered = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ORDERED)) == 1
            )
            items.add(item)
        }
        cursor.close()
        db.close()
        return items
    }

    fun updateOrderStatus(order: OrderModel): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DbReferences.COLUMN_STATUS, order.status)
        }
        val rowsUpdated = db.update(
            DbReferences.TABLE_ORDERS,
            values,
            "${DbReferences.COLUMN_ORDER_ID}=?",
            arrayOf(order.orderId.toString())
        )
        db.close()
        return rowsUpdated
    }

    fun getCompletedOrders(): List<OrderModel> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DbReferences.TABLE_ORDERS,
            null,
            "${DbReferences.COLUMN_STATUS} = ?",
            arrayOf("Completed"),
            null,
            null,
            null
        )
        val orders = mutableListOf<OrderModel>()
        while (cursor.moveToNext()) {
            val orderId = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ORDER_ID))
            val orderDate = Date(cursor.getLong(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ORDER_DATE)))
            val totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_TOTAL_AMOUNT))
            val status = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_STATUS))
            val employeeId = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_EMPLOYEE_ID))
            val isDeleted = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_IS_DELETED)) == 1
            val items = getItemsByOrderId(orderId)
            orders.add(OrderModel(orderId, orderDate, totalAmount, items, status, isDeleted, employeeId))
        }
        cursor.close()
        db.close()
        return orders
    }

    fun getAllItemsIncludingUnordered(): List<ItemModel> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DbReferences.TABLE_ITEMS,
            null,
            null,
            null,
            null,
            null,
            null
        )
        val items = mutableListOf<ItemModel>()
        while (cursor.moveToNext()) {
            val item = ItemModel(
                orderId = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ORDER_ID_FK)),
                imageUri = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_IMAGE_URI)),
                itemName = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ITEM_NAME)),
                itemPrice = cursor.getFloat(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ITEM_PRICE)),
                quantity = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_QUANTITY)),
                ordered = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ORDERED)) == 1
            )
            items.add(item)
        }
        cursor.close()
        db.close()
        return items
    }



}
