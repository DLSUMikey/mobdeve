package com.example.posystem2

data class ItemModel(
    val orderId: Int,
    val imageUri: String,
    val itemName: String,
    val itemPrice: Int,
    var quantity: Int = 1,  // New field to mark the quantity of the item
    val ordered: Boolean = false  // Field to mark if the item is part of an order
)



