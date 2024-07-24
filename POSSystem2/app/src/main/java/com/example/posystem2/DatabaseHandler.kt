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
            val orderDate = Date(cursor.getLong(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ORDER_DATE)))
            val totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_TOTAL_AMOUNT))
            val items = getItemsByOrderId(orderId)
            orders.add(OrderModel(orderId, orderDate, totalAmount, items))
        }
        cursor.close()
        db.close()
        return orders
    }

    private fun getItemsByOrderId(orderId: Int): List<ItemModel> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DbReferences.TABLE_ITEMS, null, "${DbReferences.COLUMN_ORDER_ID_FK}=?", arrayOf(orderId.toString()), null, null, null)
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
