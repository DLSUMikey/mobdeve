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
                    val itemValues = ContentValues().apply {
                        put(DbReferences.COLUMN_ORDER_ID, orderId)
                        put(DbReferences.COLUMN_ITEM_ID, item.itemId)
                        put(DbReferences.COLUMN_QUANTITY, item.quantity)
                    }
                    db.insert(DbReferences.TABLE_ORDER_ITEMS, null, itemValues)

                    updateItemStockAndSales(item.copy(itemId = item.itemId), db)
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


    private fun updateItemStockAndSales(item: ItemModel, db: SQLiteDatabase): Int {
        val cursor = db.query(
            DbReferences.TABLE_ITEMS,
            arrayOf(DbReferences.COLUMN_AMOUNT_SOLD),
            "${DbReferences.COLUMN_ITEM_ID}=?",
            arrayOf(item.itemId.toString()),
            null,
            null,
            null
        )

        var currentAmountSold = 0
        if (cursor.moveToFirst()) {
            currentAmountSold =
                cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_AMOUNT_SOLD))
        }
        cursor.close()

        val newAmountSold = currentAmountSold + item.quantity
        val values = ContentValues().apply {
            put(DbReferences.COLUMN_AMOUNT_SOLD, newAmountSold)
        }

        return db.update(
            DbReferences.TABLE_ITEMS,
            values,
            "${DbReferences.COLUMN_ITEM_ID}=?",
            arrayOf(item.itemId.toString())
        )
    }


    fun addNewItem(item: ItemModel): Long {
        val db = dbHelper.writableDatabase
        val itemId = addItem(item, db)
        db.close()
        return itemId
    }

    private fun addItem(item: ItemModel, db: SQLiteDatabase): Long {
        val values = ContentValues().apply {
            put(DbReferences.COLUMN_IMAGE_URI, item.imageUri)
            put(DbReferences.COLUMN_ITEM_NAME, item.itemName)
            put(DbReferences.COLUMN_ITEM_PRICE, item.itemPrice)
            put(DbReferences.COLUMN_ORDERED, 0)  // Ensure new items are marked as not ordered
            put(DbReferences.COLUMN_QUANTITY, item.quantity)  // Set the quantity
            put(DbReferences.COLUMN_INITIAL_STOCK, item.initialStock)  // Set the initial stock
            put(DbReferences.COLUMN_AMOUNT_SOLD, item.amountSold)  // Set the amount sold
        }
        return db.insert(DbReferences.TABLE_ITEMS, null, values)
    }

    fun updateItem(item: ItemModel): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DbReferences.COLUMN_IMAGE_URI, item.imageUri)
            put(DbReferences.COLUMN_ITEM_NAME, item.itemName)
            put(DbReferences.COLUMN_ITEM_PRICE, item.itemPrice)
            put(DbReferences.COLUMN_QUANTITY, item.quantity)  // Update the quantity
        }
        val rowsUpdated = db.update(
            DbReferences.TABLE_ITEMS,
            values,
            "${DbReferences.COLUMN_ITEM_ID}=?",
            arrayOf(item.itemId.toString())
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
            val orderDate =
                Date(cursor.getLong(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ORDER_DATE)))
            val totalAmount =
                cursor.getDouble(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_TOTAL_AMOUNT))
            val status = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_STATUS))
                ?: "In Progress"
            val employeeId =
                cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_EMPLOYEE_ID))
            val isDeleted =
                cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_IS_DELETED)) == 1
            val items = getOrderedItemsForOrder(orderId)
            orders.add(
                OrderModel(
                    orderId,
                    orderDate,
                    totalAmount,
                    items,
                    status,
                    isDeleted,
                    employeeId
                )
            )
        }
        cursor.close()
        db.close()
        return orders
    }


    private fun getOrderedItems(): List<ItemModel> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DbReferences.TABLE_ITEMS,
            null,
            "${DbReferences.COLUMN_ORDERED}=?",
            arrayOf("1"),
            null,
            null,
            null
        )
        val items = mutableListOf<ItemModel>()
        while (cursor.moveToNext()) {
            val item = ItemModel(
                itemId = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ITEM_ID)),
                imageUri = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_IMAGE_URI)),
                itemName = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ITEM_NAME)),
                itemPrice = cursor.getFloat(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ITEM_PRICE)),
                quantity = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_QUANTITY)),
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
        val cursor = db.query(
            DbReferences.TABLE_PROFILE,
            null,
            "${DbReferences.COLUMN_EMAIL}=?",
            arrayOf(email),
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
                put(
                    DbReferences.COLUMN_PASSWORD,
                    BCrypt.withDefaults().hashToString(12, profile.password.toCharArray())
                )
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
            val storedHash =
                cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_PASSWORD))
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

            // Create dummy items independently
            val items = listOf(
                ItemModel(
                    imageUri = "android.resource://com.example.posystem2/drawable/ic_launcher_background",
                    itemName = "Item 1",
                    itemPrice = 10.0f
                ),
                ItemModel(
                    imageUri = "android.resource://com.example.posystem2/drawable/ic_launcher_background",
                    itemName = "Item 2",
                    itemPrice = 20.0f
                ),
                ItemModel(
                    imageUri = "android.resource://com.example.posystem2/drawable/ic_launcher_background",
                    itemName = "Item 3",
                    itemPrice = 30.0f
                )
            )

            items.forEach { item ->
                addItem(item, db)
            }
            db.close()
        }
    }

    fun getAllItems(): List<ItemModel> {
        val db = dbHelper.readableDatabase
        val cursor =
            db.query(DbReferences.TABLE_ITEMS, null, "ordered = ?", arrayOf("0"), null, null, null)
        val items = mutableListOf<ItemModel>()
        while (cursor.moveToNext()) {
            val item = ItemModel(
                itemId = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ITEM_ID)),
                imageUri = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_IMAGE_URI)),
                itemName = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ITEM_NAME)),
                itemPrice = cursor.getFloat(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ITEM_PRICE)),
                quantity = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_QUANTITY)),  // Fetch the quantity
            )
            items.add(item)
        }
        cursor.close()
        db.close()
        return items
    }

    private fun getOrderedItemsForOrder(orderId: Int): List<ItemModel> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DbReferences.TABLE_ORDER_ITEMS,
            arrayOf(DbReferences.COLUMN_ITEM_ID, DbReferences.COLUMN_QUANTITY),
            "${DbReferences.COLUMN_ORDER_ID}=?",
            arrayOf(orderId.toString()),
            null,
            null,
            null
        )
        val items = mutableListOf<ItemModel>()
        while (cursor.moveToNext()) {
            val itemId = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ITEM_ID))
            val quantity = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_QUANTITY))
            val item = getItemById(itemId)?.copy(quantity = quantity)
            if (item != null) {
                items.add(item)
            }
        }
        cursor.close()
        db.close()
        return items
    }

    private fun getItemById(itemId: Int): ItemModel? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DbReferences.TABLE_ITEMS,
            null,
            "${DbReferences.COLUMN_ITEM_ID}=?",
            arrayOf(itemId.toString()),
            null,
            null,
            null
        )
        return if (cursor.moveToFirst()) {
            val item = ItemModel(
                itemId = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ITEM_ID)),
                imageUri = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_IMAGE_URI)),
                itemName = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ITEM_NAME)),
                itemPrice = cursor.getFloat(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ITEM_PRICE)),
                quantity = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_QUANTITY))
            )
            cursor.close()
            db.close()
            item
        } else {
            cursor.close()
            db.close()
            null
        }
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
            val orderDate =
                Date(cursor.getLong(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ORDER_DATE)))
            val totalAmount =
                cursor.getDouble(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_TOTAL_AMOUNT))
            val status = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_STATUS))
            val employeeId =
                cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_EMPLOYEE_ID))
            val isDeleted =
                cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_IS_DELETED)) == 1
            val items = getOrderedItemsForOrder(orderId)
            orders.add(
                OrderModel(
                    orderId,
                    orderDate,
                    totalAmount,
                    items,
                    status,
                    isDeleted,
                    employeeId
                )
            )
        }
        cursor.close()
        db.close()
        return orders
    }


    fun getAllItemsIncludingUnordered(): List<ItemModel> {
        val db = dbHelper.readableDatabase
        val itemsSold = getAggregatedItemsSold()
        val items = mutableListOf<ItemModel>()
        val cursor = db.query(
            DbReferences.TABLE_ITEMS,
            null,
            null,
            null,
            null,
            null,
            null
        )

        try {
            while (cursor.moveToNext()) {
                val itemName =
                    cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ITEM_NAME))
                val item = ItemModel(
                    itemId = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ITEM_ID)),
                    imageUri = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_IMAGE_URI)),
                    itemName = itemName,
                    itemPrice = cursor.getFloat(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ITEM_PRICE)),
                    quantity = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_QUANTITY)),
                    initialStock = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_INITIAL_STOCK)),
                    amountSold = itemsSold[itemName] ?: 0 // Use the aggregated data
                )
                items.add(item)
                Log.d(
                    "DatabaseHandler",
                    "Fetched Item: ${item.itemName}, Initial Stock: ${item.initialStock}, Amount Sold: ${item.amountSold}"
                )
            }
        } finally {
            cursor.close()
            db.close()
        }
        return items
    }


    fun getAggregatedItemsSold(): Map<String, Int> {
        val query = """
        SELECT i.${DbReferences.COLUMN_ITEM_NAME}, SUM(oi.${DbReferences.COLUMN_QUANTITY}) as total_sold
        FROM ${DbReferences.TABLE_ORDER_ITEMS} oi
        JOIN ${DbReferences.TABLE_ITEMS} i ON oi.${DbReferences.COLUMN_ITEM_ID} = i.${DbReferences.COLUMN_ITEM_ID}
        GROUP BY i.${DbReferences.COLUMN_ITEM_NAME}
    """

        val cursor = dbHelper.readableDatabase.rawQuery(query, null)
        val itemsSold = mutableMapOf<String, Int>()

        try {
            while (cursor.moveToNext()) {
                val itemName =
                    cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ITEM_NAME))
                val totalSold = cursor.getInt(cursor.getColumnIndexOrThrow("total_sold"))
                itemsSold[itemName] = totalSold
            }
        } finally {
            cursor.close()
        }
        return itemsSold
    }


    fun updateItemStockByName(itemName: String, newStock: Int) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DbReferences.COLUMN_INITIAL_STOCK, newStock)
            put(DbReferences.COLUMN_AMOUNT_SOLD, 0) // Reset amount sold to zero
        }
        val rowsUpdated = db.update(
            DbReferences.TABLE_ITEMS,
            values,
            "${DbReferences.COLUMN_ITEM_NAME}=?",
            arrayOf(itemName)
        )
        Log.d(
            "DatabaseHandler",
            "Rows updated: $rowsUpdated, Item Name: $itemName, New Stock: $newStock, Amount Sold: 0"
        )
        db.close()
    }
}
