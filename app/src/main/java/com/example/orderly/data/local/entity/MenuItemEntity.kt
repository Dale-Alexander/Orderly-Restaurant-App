package com.example.orderly.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey//room is a wrapper of SQLite

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey val id:String,//idMeal is a string
    val name:String,
    val code:String,
    val category:String?,
    var price:Double,
    var description:String,
    var imageUrl:String
)