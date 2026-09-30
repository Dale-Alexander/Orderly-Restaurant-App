package com.example.orderly.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id:Int = 0,
    val customerName:String,
    var totalPrice: Double,
    val status:String = "Pending",
    val createdAt: Long = System.currentTimeMillis()//milliseconds since 1970
)