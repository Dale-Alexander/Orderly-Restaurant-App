package com.example.orderly.data.remote.ViewModel

import com.example.orderly.data.local.entity.MenuItemEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MenuItem(//field names must match exactly the names from the API respone otherwise Gson wont work
    //"SerialName" is a simple way of saying the API calls it this, but i want to call it something else
    @SerialName("idMeal") val id:String,
    @SerialName("strMeal") val name:String,
    @SerialName("strCategory") val category:String? = null,
    @SerialName("strMealThumb") val imageUrl:String,//this is the image
    @SerialName("strInstructions") val instructions:String? = null//the full cooking instructions
)

fun MenuItem.toEntity(categoryName:String = category ?: "Unknown", code:String = ""): MenuItemEntity {
    return MenuItemEntity(
        id = id,
        name = name,
        category = categoryName,
        imageUrl = imageUrl,
        price = if (categoryName == "Side") (1..7).random().toDouble() else (5..25).random().toDouble(),
        description = "Lorem ipsum dolor",
        code = code
    )
}
/* .toEntity() is a mapping function that converts your MenuItem DTO into
* a MenuItemEntity. Think of it like a translator: MenuItem(API world) -> toEntity() -> MenuItemEntity(Database world)*/
