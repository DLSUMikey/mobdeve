object DbReferences {
    const val DATABASE_VERSION = 6
    const val DATABASE_NAME = "posystem2.db"

    const val TABLE_ORDERS = "orders"
    const val COLUMN_ORDER_ID = "order_id"
    const val COLUMN_ORDER_DATE = "order_date"
    const val COLUMN_TOTAL_AMOUNT = "total_amount"
    const val COLUMN_STATUS = "status"
    const val COLUMN_IS_DELETED = "is_deleted"
    const val COLUMN_EMPLOYEE_ID = "employee_id"

    const val TABLE_ITEMS = "items"
    const val COLUMN_ITEM_ID = "item_id"
    const val COLUMN_IMAGE_URI = "image_uri"
    const val COLUMN_ITEM_NAME = "item_name"
    const val COLUMN_ITEM_PRICE = "item_price"
    const val COLUMN_ORDERED = "ordered"
    const val COLUMN_QUANTITY = "quantity"
    const val COLUMN_INITIAL_STOCK = "initial_stock"
    const val COLUMN_AMOUNT_SOLD = "amount_sold"

    const val TABLE_PROFILE = "profile"
    const val COLUMN_PROFILE_ID = "profile_id"
    const val COLUMN_EMAIL = "email"
    const val COLUMN_PASSWORD = "password"
    const val COLUMN_FIRST_NAME = "first_name"
    const val COLUMN_LAST_NAME = "last_name"
    const val COLUMN_PHONE_NUMBER = "phone_number"
    const val COLUMN_USER_TYPE = "user_type"

    const val CREATE_ORDERS_TABLE = """
        CREATE TABLE IF NOT EXISTS $TABLE_ORDERS (
            $COLUMN_ORDER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_ORDER_DATE TEXT,
            $COLUMN_TOTAL_AMOUNT REAL,
            $COLUMN_STATUS TEXT, 
            $COLUMN_IS_DELETED INTEGER DEFAULT 0,
            $COLUMN_EMPLOYEE_ID INTEGER
        )
    """

    const val CREATE_ITEMS_TABLE = """
        CREATE TABLE IF NOT EXISTS $TABLE_ITEMS (
            $COLUMN_ITEM_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_IMAGE_URI TEXT,
            $COLUMN_ITEM_NAME TEXT,
            $COLUMN_ITEM_PRICE REAL,
            $COLUMN_ORDERED INTEGER DEFAULT 0,
            $COLUMN_QUANTITY INTEGER DEFAULT 1,
            $COLUMN_INITIAL_STOCK INTEGER DEFAULT 0, 
            $COLUMN_AMOUNT_SOLD INTEGER DEFAULT 0
        )
    """

    const val CREATE_PROFILE_TABLE = """
        CREATE TABLE IF NOT EXISTS $TABLE_PROFILE (
            $COLUMN_PROFILE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_EMAIL TEXT,
            $COLUMN_PASSWORD TEXT,
            $COLUMN_FIRST_NAME TEXT,
            $COLUMN_LAST_NAME TEXT,
            $COLUMN_PHONE_NUMBER TEXT,
            $COLUMN_USER_TYPE TEXT
        )
    """

    const val DROP_ORDERS_TABLE = "DROP TABLE IF EXISTS $TABLE_ORDERS"
    const val DROP_ITEMS_TABLE = "DROP TABLE IF EXISTS $TABLE_ITEMS"
    const val DROP_PROFILE_TABLE = "DROP TABLE IF EXISTS $TABLE_PROFILE"
}