package com.example.posystem2

import java.util.Date

data class OrderModel(
    val orderId: Int,
    val orderDate: Date,
    val totalAmount: Double,
    val items: List<ItemModel>
)


