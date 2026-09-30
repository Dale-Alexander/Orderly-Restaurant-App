package com.example.orderly.data.local.Dao

import androidx.room.Dao
import androidx.room.Index
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.orderly.data.local.entity.MenuItemEntity
import com.example.orderly.data.local.entity.OrderEntity
import com.example.orderly.data.local.entity.OrderItemEntity
import com.example.orderly.data.remote.ViewModel.OrderWithItems
import com.example.orderly.data.remote.ViewModel.TopSellingItem

@Dao
interface orderDao{
    /* This is the raw DAO, it only gives you direct database access with no extra logic */
    @Transaction
    @Query("SELECT * FROM orders")
    suspend fun getAllOrders():List<OrderWithItems>
    /* under the hood this is basically doing 2 join operations(look at OrderDao)
    * but it does them as 3 seperate queries because first the room fetches the orders first
    * and then fetches the related rows seperately
    * SELECT * FROM orders
SELECT * FROM order_items WHERE orderId IN (1, 2, 3...)
SELECT * FROM menu_items WHERE id IN ("abc", "xyz"...)
*
*
* @Transaction is needed because those 3 queries need to be executed as one atomic
* operation. Without it, another thread could insert or modify data between the 3 queries,
* meaning Query1 fetches orders, another thread inserts a new order item and query 2 fetches order items - now includes data that didnt exist when Query ran 1
* So @Transaction locks the database for the duration of all 3 queries so they all see the same snapshot*/

    @Query("SELECT SUM(totalPrice) FROM orders")
    suspend fun getTotalRevenue():Double

    @Query("SELECT COUNT(*) FROM orders")
    suspend fun getTotalOrders():Int

    @Query("SELECT AVG(totalPrice) FROM orders")
    suspend fun getAveragePrice():Double

    @Query("""
        SELECT oi.itemId
        FROM orderItem oi 
        JOIN orders o 
        ON oi.orderId = o.id
        WHERE date(o.createdAt / 1000, 'unixepoch') = date('now')
        GROUP BY oi.itemId
        ORDER BY SUM(oi.quantity) DESC
        LIMIT 1
    """)
    //o.createdAt/ 1000: convert from milliseconds to seconds
    /* unixepoch is a SQLite modifier that tells the date() function how to
    * interpret the number youre passing in. It means treat this number as a Unix
    * timestamp(i.e seconds elapsed since January 1st 1970)
    *
    * All together it is saying convert this millisecond timestamp to a date and check if it equals today's date */

    suspend fun getTopSellingItemIdToday():String?

    @Query("""
        SELECT SUM(oi.quantity) [totalQuantity],mi.name 
        FROM menu_items mi
        JOIN orderItem oi
        ON oi.itemId = mi.id
        GROUP BY oi.itemId
        ORDER BY [totalQuantity] DESC
        LIMIT 5
    """)
    suspend fun getTopSellingItems():List<TopSellingItem>

    @Query("""
        SELECT SUM(oi.quantity) [totalQuantity], mi.name
        FROM orderItem oi 
        JOIN menu_items mi 
        ON mi.id = oi.itemId
        GROUP BY oi.itemId
        ORDER BY [totalQuantity] DESC
        LIMIT 1
    """)
    suspend fun getTopSellingItem(): TopSellingItem

    @Query("SELECT * FROM orders ORDER BY createdAt DESC LIMIT 5")
    suspend fun getRecentOrders():List<OrderWithItems>

    @Insert()
    suspend fun insertOrder(order:OrderEntity):Long // Long is the correct return type for Room's @Insert

    @Insert
    suspend fun insertOrderItems(items:List<OrderItemEntity>)

    @Query("SELECT COUNT(*) FROM ORDERS WHERE createdAt >= :startOfDay")
    suspend fun getTotalOrdersToday(startOfDay:Long):Int

    @Query("SELECT SUM(totalPrice) FROM orders WHERE createdAt >= :startOfDay")
    suspend fun getTotalRevenueToday(startOfDay:Long):Double

}