package com.example.posystem2

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.google.type.Date

class OrderDao(context: Context) {
    private val dbHelper = MyDbHelper(context)

    fun insertOrder(order: OrderEntity): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DbReferences.COLUMN_ORDER_DATE, order.orderDate.time)
            put(DbReferences.COLUMN_TOTAL_AMOUNT, order.totalAmount)
        }
        val orderId = db.insert(DbReferences.TABLE_ORDERS, null, values)
        db.close()
        return orderId
    }

    fun getAllOrders(): List<OrderEntity> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DbReferences.TABLE_ORDERS, null, null, null, null, null, null)
        val orders = mutableListOf<OrderEntity>()
        while (cursor.moveToNext()) {
            val order = OrderEntity(
                orderId = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ORDER_ID)),
                orderDate = java.util.Date(cursor.getLong(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ORDER_DATE))),
                totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_TOTAL_AMOUNT))
            )
            orders.add(order)
        }
        cursor.close()
        db.close()
        return orders
    }
}
