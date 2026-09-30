package com.example.orderly.data.remote.ViewModel

data class DashboardData(
    val totalOrders:Int,
    val totalRevenue:Double,
    val averageOrderValue:Double,
    val recentOrders:List<OrderWithItems>,
    val topSellingItems: List<TopSellingItem>
)
