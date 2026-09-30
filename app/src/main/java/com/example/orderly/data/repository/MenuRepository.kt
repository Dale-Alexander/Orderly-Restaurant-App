package com.example.orderly.data.repository

import android.util.Log
import com.example.orderly.data.local.Dao.MenuItemDao
import com.example.orderly.data.local.entity.MenuItemEntity
import com.example.orderly.data.remote.ViewModel.MenuItem
import com.example.orderly.data.remote.ViewModel.MenuList
import com.example.orderly.data.remote.ViewModel.toEntity
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

private val baseUrl = "https://www.themealdb.com/api/json/v1/1/"
class MenuRepository(private val menuItemDao: MenuItemDao,
                     private val ktorClient: HttpClient
)

/* These are constructor parameters - when you create  MenuRepository you must
* pass in a MenuItemDao and a MealApiService. The repository stores them as private properties
* and uses them internally. The repository is saying: "To do my job i need access to the database
* (MenuItemDao) and the API(MealApiService), give them to me when you create me.
* You cannot use List<MenuITemDTO> instead of MealApiService because List<MenuItemDto> is just data,
* it's a static list that you'd have to populate before passing in. MealApiService is a
* service that can co and fetch fresh data from the interned whenever needed.
* Think of it like:

List<MenuItemDTO> — a shopping list someone already wrote
MealApiService — a person who can go to the shop and get whatever you need
*  */
{
    suspend fun getMenuItems():Result<List<MenuItemEntity>> = runCatching{
        /* runCatching automatically wraps the result in a Result object and
        * catches any exceptions for you, so you dont need a try catch block.
        *
        * Result<List<MenuItemEntity>> - instead of returning the list directly, it returns
        * either Result.success(list) if everything worked or Result.failure(exception)
        * if something went wrong
        * Then in my screen when you call it:
        * val result = repository.getMenuItems()
result.onSuccess { menuItems ->
    // use menuItems
}.onFailure { exception ->
    // show error
}
        * */
        val cachedItems = menuItemDao.getAllMenuItems()
        Log.d("MenuRepo", "Cached Items count: ${cachedItems.size}")
        if(cachedItems.isEmpty()){
                val categoryList = listOf("Seafood", "Chicken", "Pasta", "Beef", "Side")
                categoryList.forEachIndexed { categoryIndex,category ->
                    val response = ktorClient.get("${baseUrl}filter.php"){
                        parameter("c", category)
                    }.body<MenuList>()
                    Log.d("MenuRepo", "Fetched ${response.meals?.size} items for $category")
                    response.meals?.let { meals -> // ← only insert if not null
                        val letter = ('A' + categoryIndex)

                        val itemsWithCodes = meals.mapIndexed{itemIndex, meal ->
                            val code = "$letter${(itemIndex + 1).toString().padStart(2, '0')}" //A01, A02 etc
                            meal.toEntity(category, code)
                        }
                        menuItemDao.insertAllItems(itemsWithCodes)
                    }
                    /* .map() is not the same as looping to insert one by one. Here's the
                    * distinction: .map() transforms one list into another list. It takes List<MenuItem>(DTO List)
                    * and converts it into List<MenuItemEntity>:
                    * [MenuItem, MenuItem, MenuItem] → map { it.toEntity() } → [MenuItemEntity, MenuItemEntity, MenuItemEntity] */
                }
                menuItemDao.getAllMenuItems()

            }
        else{
            cachedItems
        }
    }

    suspend fun getMenuItem(id:String): MenuItemEntity?{
        return menuItemDao.getMenuItem(id)
    }

    suspend fun deleteMenuItem(menuItem:MenuItemEntity){//when deleting Kotlin extracts the id automatically to delete however passing the id is also valid
        menuItemDao.delete(menuItem)
    }

    suspend fun addMenuItem(item:MenuItemEntity){
        menuItemDao.insertItem(item)
    }

    suspend fun getLastCodeForCategory(category:String): String? {
        return menuItemDao.getLastCodeForCategory(category)
    }

    suspend fun getApiItemsForCategory(category: String): Result<List<MenuItem>> = runCatching {
        val response = ktorClient.get("${baseUrl}filter.php") {
            parameter("c", category)
        }.body<MenuList>()
        response.meals ?: emptyList()
    }

    suspend fun getMenuItemsByCategory(category:String):List<MenuItemEntity>{
        return if(category == "All"){
            menuItemDao.getAllMenuItems()
        }
        else{
            menuItemDao.getMenuItemsByCategory(category)
        }
    }
}