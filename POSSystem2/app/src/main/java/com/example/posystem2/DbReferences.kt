package com.example.posystem2

object DbReferences {
    const val DATABASE_VERSION = 1
    const val DATABASE_NAME = "posystem2.db"

    const val TABLE_ORDERS = "orders"
    const val COLUMN_ORDER_ID = "order_id"
    const val COLUMN_ORDER_DATE = "order_date"
    const val COLUMN_TOTAL_AMOUNT = "total_amount"

    const val TABLE_ITEMS = "items"
    const val COLUMN_ITEM_ID = "item_id"
    const val COLUMN_IMAGE_ID = "image_id"
    const val COLUMN_ITEM_NAME = "item_name"
    const val COLUMN_ITEM_PRICE = "item_price"
    const val COLUMN_ORDER_ID_FK = "order_id_fk"

    const val CREATE_ORDERS_TABLE = """
        CREATE TABLE IF NOT EXISTS $TABLE_ORDERS (
            $COLUMN_ORDER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_ORDER_DATE TEXT,
            $COLUMN_TOTAL_AMOUNT REAL
        )
    """

    const val CREATE_ITEMS_TABLE = """
        CREATE TABLE IF NOT EXISTS $TABLE_ITEMS (
            $COLUMN_ITEM_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_IMAGE_ID INTEGER,
            $COLUMN_ITEM_NAME TEXT,
            $COLUMN_ITEM_PRICE INTEGER,
            $COLUMN_ORDER_ID_FK INTEGER,
            FOREIGN KEY($COLUMN_ORDER_ID_FK) REFERENCES $TABLE_ORDERS($COLUMN_ORDER_ID)
        )
    """

    const val DROP_ORDERS_TABLE = "DROP TABLE IF EXISTS $TABLE_ORDERS"
    const val DROP_ITEMS_TABLE = "DROP TABLE IF EXISTS $TABLE_ITEMS"
}
