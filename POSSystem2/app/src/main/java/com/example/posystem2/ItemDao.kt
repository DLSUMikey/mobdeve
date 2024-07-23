package com.example.posystem2

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase

class ItemDao(context: Context) {
    private val dbHelper = MyDbHelper(context)

    fun insertItem(item: ItemEntity): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DbReferences.COLUMN_IMAGE_ID, item.imageId)
            put(DbReferences.COLUMN_ITEM_NAME, item.itemName)
            put(DbReferences.COLUMN_ITEM_PRICE, item.itemPrice)
            put(DbReferences.COLUMN_ORDER_ID_FK, item.orderId)
        }
        val itemId = db.insert(DbReferences.TABLE_ITEMS, null, values)
        db.close()
        return itemId
    }

    fun getItemsByOrderId(orderId: Int): List<ItemEntity> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DbReferences.TABLE_ITEMS, null, "${DbReferences.COLUMN_ORDER_ID_FK}=?", arrayOf(orderId.toString()), null, null, null)
        val items = mutableListOf<ItemEntity>()
        while (cursor.moveToNext()) {
            val item = ItemEntity(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ITEM_ID)),
                imageId = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_IMAGE_ID)),
                itemName = cursor.getString(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ITEM_NAME)),
                itemPrice = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ITEM_PRICE)),
                orderId = cursor.getInt(cursor.getColumnIndexOrThrow(DbReferences.COLUMN_ORDER_ID_FK))
            )
            items.add(item)
        }
        cursor.close()
        db.close()
        return items
    }
}
