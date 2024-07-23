package com.example.posystem2

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val imageId: Int,
    val itemName: String,
    val itemPrice: Int,
    val orderId: Int  // Foreign key to OrderEntity
)
