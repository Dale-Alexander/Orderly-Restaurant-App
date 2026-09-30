package com.example.orderly.data.repository

import com.example.orderly.data.local.AppDatabase
import com.example.orderly.data.local.Dao.MenuItemDao
import com.example.orderly.data.local.Dao.orderDao
import com.example.orderly.data.local.entity.OrderEntity
import com.example.orderly.data.local.entity.OrderItemEntity
import com.example.orderly.data.remote.ViewModel.DashboardData
import com.example.orderly.data.remote.ViewModel.OrderWithItems
import com.example.orderly.data.remote.ViewModel.WidgetData
import io.ktor.client.HttpClient

private val baseUrl = "https://www.themealdb.com/api/json/v1/1/"
class OrdersRepository(private val orderDao: orderDao
    /* OrdersRepository use case:
    * This wraps the DAO in a repository which adds logic on top like runCatching, error handling,
    * and combining multiple DAO calls like insertOrder, which calls both insertOrder
    * and insertORderItems together. If you dont add extra logic, you can just use
    * the DAO directly in the Screens */
)

/* the orders are stored in Room(local database) so they will still show offline -
* you can insert read and display orders without any interned connection.
* The only thing that needs interned in your app is the menu items which are fetched from the
* MealDB API. But even those are cached in Room after the first fetch, so after the first load they also work offline */
{
    suspend fun getOrdersWithItems(): Result<List<OrderWithItems>> = runCatching{
        orderDao.getAllOrders()
    }

    suspend fun getTopSellingItemIdToday():String? {
        return orderDao.getTopSellingItemIdToday()
    }

    suspend fun insertOrder(order: OrderEntity, items:List<OrderItemEntity>):Result<Unit> = runCatching{
        val orderId = orderDao.insertOrder(order)
        val itemsWithOrderId = items.map{it.copy(orderId = orderId.toInt())}
        /* .copy() creates a new copy of a data class with some fields changed
        * and the rest kept the same
        * Son instead of OrderItemEntity(
    orderId = orderId.toInt(), // new value
    itemId = it.itemId,        // same as original
    quantity = it.quantity,    // same as original
    price = it.price           // same as original
    )
    *
    * One can just do it.copy(orderId = orderId.toInt()).
    * .copy() is only available for data classes */

        orderDao.insertOrderItems(itemsWithOrderId)

    }
    /* Example of the above function being used:
    * // Create the order
val order = OrderEntity(
    customerName = "John",
    totalPrice = 25.50
)

// Create the items — orderId is 0 for now, repository will assign the real id
val items = listOf(
    OrderItemEntity(
        orderId = 0, // placeholder, gets replaced in repository
        itemId = "abc123",
        quantity = 2,
        price = 10.00
    ),
    OrderItemEntity(
        orderId = 0, // placeholder, gets replaced in repository
        itemId = "xyz789",
        quantity = 1,
        price = 5.50
    )


// Insert
val result = orderRepository.insertOrder(order, items)
*
* What happens inside insertOrder:

Inserts OrderEntity → Room returns orderId = 1 (auto generated)
Maps over items and sets orderId = 1 on both
Inserts both OrderItemEntity rows with the correct orderId

So the database ends up with:
orders:      id=1, customerName="John", totalPrice=25.50
order_items: orderId=1, itemId="abc123", quantity=2, price=10.00
             orderId=1, itemId="xyz789", quantity=1, price=5.50
    *  */


    suspend fun getDashboardData():Result<DashboardData> = runCatching{
        DashboardData(
            totalOrders = orderDao.getTotalOrders(),
            totalRevenue = orderDao.getTotalRevenue(),
            averageOrderValue = orderDao.getAveragePrice(),
            recentOrders = orderDao.getRecentOrders(),
            topSellingItems = orderDao.getTopSellingItems()
        )
    }

    suspend fun getWidgetData():Result<WidgetData> = runCatching{
        val startOfDay = System.currentTimeMillis() - (System.currentTimeMillis() % 864000000)
        WidgetData(
            todayOrders = orderDao.getTotalOrdersToday(startOfDay),
            todayRevenue = orderDao.getTotalRevenueToday(startOfDay),
            topSellingItem = orderDao.getTopSellingItem()
        )
    }
}