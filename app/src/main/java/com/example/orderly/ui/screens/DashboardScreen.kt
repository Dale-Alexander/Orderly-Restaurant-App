package com.example.orderly.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.navigation.NavController
import com.example.orderly.data.local.AppDatabase
import com.example.orderly.data.remote.ViewModel.DashboardData
import com.example.orderly.data.repository.OrdersRepository
import com.example.orderly.ui.components.OrderCard

@Composable
fun MetricCard(label:String,
               value:String,
               modifier:Modifier = Modifier,
               valueSize:Int = 20){
    Column(modifier = modifier.clip(RoundedCornerShape(12.dp))
        .background(Color(0xFF1C1C1E)).padding(16.dp)){
        Text(text = label, color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = value,
            color = Color.White,
            fontSize = valueSize.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun DashboardScreen(navController: NavController){
    var dashboardData by remember {mutableStateOf<DashboardData?>(null)}
    var isLoading by remember{mutableStateOf(true)}
    val context = LocalContext.current

    LaunchedEffect(Unit){
        val ordersRepository = OrdersRepository(AppDatabase.getDatabase(context).orderDao())
        ordersRepository.getDashboardData().fold(
            onSuccess = {dashboardData = it; isLoading = false},
            onFailure = {isLoading = false}
        )
    }

    if(isLoading){
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121212)),
            contentAlignment = Alignment.Center){
            CircularProgressIndicator(color = Color(0xFFFF6B35))
        }
        return
    }

    dashboardData?.let{data ->
        LazyColumn(modifier = Modifier.fillMaxSize().background(Color(0xFF121212)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)){
            item {
                Text(
                    text = "Dashboard",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Overview",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { navController.navigate("Settings") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))
                    ) {
                        Text("Settings", color = Color.White, fontSize = 13.sp)
                    }
                    Button(
                        onClick = { navController.navigate("ManageMenu") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))
                    ) {
                        Text("Manage Menu", color = Color.White, fontSize = 13.sp)
                    }
                }
            }

            //metric cards
            item{
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)){
                    MetricCard(label = "Total Orders",
                        value = "${data.totalOrders}",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(label = "Total Revenue",
                        value = "%.2f".format(data.totalRevenue),
                        modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)){
                    MetricCard(label = "Average Order Value",
                        value = "%.2f".format(data.averageOrderValue),
                        modifier = Modifier.weight(1f))
                    MetricCard(label = "Top Item",
                        value = data.topSellingItems.firstOrNull()?.name ?: "N/A",
                        modifier = Modifier.weight(1f),
                        valueSize = 13)
                }
            }

            //top selling items

            item{
                Text(text = "Top Selling Items",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(10.dp))
                Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1C1C1E)).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (data.topSellingItems.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No items sold yet",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        data.topSellingItems.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.size(24.dp).clip(CircleShape)
                                            .background(
                                                if (index == 0) Color(0xFFFF6B35) else Color(
                                                    0xFF2C2C2E
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = item.name,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = "${item.totalQuantity} sold",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                            if (index < data.topSellingItems.size - 1) {
                                HorizontalDivider(color = Color(0xFF2C2C2E))
                            }
                        }
                    }
                }
                }

            //recent orders
            item {
                Text(
                    text = "Recent Orders",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                if(data.recentOrders.isEmpty()){
                    Box(modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1C1C1E))
                        .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center){
                        Text(text = "No orders yet",
                            color = Color.Gray,
                            fontSize = 13.sp)
                    }
                }
            }
                if(data.recentOrders.isNotEmpty()){
                    items(data.recentOrders){
                        order -> OrderCard(orderWithItems = order)
                    }
                }

        }
    }
}