package com.example.orderly.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "orderItem", primaryKeys=["orderId", "itemId"],
    foreignKeys = [ForeignKey(
        entity = OrderEntity::class,
        parentColumns = ["id"],
        childColumns = ["orderId"],
        onDelete = ForeignKey.CASCADE
    ),
    ForeignKey(entity = MenuItemEntity::class,
        parentColumns = ["id"],
        childColumns = ["itemId"],
        onDelete = ForeignKey.CASCADE)])
data class OrderItemEntity(
    val orderId:Int,
    val itemId:String,
    val quantity:Int,
    val price:Double
)