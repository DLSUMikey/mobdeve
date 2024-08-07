package com.example.posystem2

data class ItemModel(
    val itemId: Int? = null,
    val imageUri: String,
    val itemName: String,
    val itemPrice: Float,
    var quantity: Int = 1,
    var initialStock: Int = 0,
    var amountSold: Int = 0
)

