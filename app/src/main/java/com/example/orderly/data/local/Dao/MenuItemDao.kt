package com.example.orderly.data.local.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.orderly.data.local.entity.MenuItemEntity

@Dao
interface MenuItemDao{
    @Query("SELECT * FROM menu_items")
    suspend fun getAllMenuItems() : List<MenuItemEntity>

    @Query("SELECT * FROM menu_items WHERE category = :category")
    suspend fun getMenuItemsByCategory(category:String) : List<MenuItemEntity>

    @Query("SELECT * FROM menu_items WHERE id = :itemId")
    suspend fun getMenuItem(itemId:String): MenuItemEntity?

    @Query("SELECT * FROM menu_items WHERE code =:code")
    suspend fun getMenuItemByCode(code:String):MenuItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)//replace instead of Abort here because if the user removes an item and re-adds it, you want it to overwrite rather than throw an exception
    suspend fun insertItem(item:MenuItemEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)//ABORT throws an exception if a duplicate primary key is inserted
    suspend fun insertAllItems(items:List<MenuItemEntity>)

    @Query("SELECT code FROM menu_items WHERE category = :category ORDER BY 1 DESC LIMIT 1")
    suspend fun getLastCodeForCategory(category:String):String?

    @Delete
    suspend fun delete(item:MenuItemEntity)
}