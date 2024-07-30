package com.example.posystem2

data class ItemModel(
    val orderId: Int,
    val imageUri: String,
    val itemName: String,
    val itemPrice: Int,
    val ordered: Boolean = false
)


