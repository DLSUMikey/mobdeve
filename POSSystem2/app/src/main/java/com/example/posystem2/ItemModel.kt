package com.example.posystem2

data class ItemModel(
    val orderId: Int,
    val imageUri: String,
    val itemName: String,
    val itemPrice: Float,
    var quantity: Int = 1,
    val ordered: Boolean = false
)




