package com.example.posystem2

import java.util.Date

data class OrderModel(
    val orderId: Int,
    val orderDate: Date,
    val totalAmount: Double,
    val items: List<ItemModel>,
    val status: String,
    val isDeleted: Boolean = false,
    val employeeId: Int
) {
    val totalQuantity: Int
        get() = items.sumBy { it.quantity }
}



