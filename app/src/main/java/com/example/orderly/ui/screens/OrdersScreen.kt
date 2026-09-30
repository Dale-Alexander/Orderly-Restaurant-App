package com.example.orderly.ui.screens

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

import androidx.navigation.NavController
import com.example.orderly.data.local.AppDatabase
import com.example.orderly.data.remote.ViewModel.OrderWithItems
import com.example.orderly.data.repository.OrdersRepository
import com.example.orderly.ui.components.OrderList

@Composable
fun OrdersScreen(navController: NavController){
    var orderList by remember {mutableStateOf<List<OrderWithItems>>(emptyList())}
    var isLoading by remember {mutableStateOf(false)}
    var error by remember {mutableStateOf<String?>(null)}

    val context = LocalContext.current

    LaunchedEffect(Unit){
       isLoading = true
        val repository = OrdersRepository(AppDatabase.getDatabase(context).orderDao())
        val orderWithItemsList = repository.getOrdersWithItems()
        orderWithItemsList.fold(
            onSuccess = { orderItems -> orderList = orderItems;isLoading = false},
            onFailure = {exception -> error = exception.message; isLoading = false}
        )
    }
    when{
        isLoading -> CircularProgressIndicator()
        error != null -> Text(error!!)
        //error!! is the not null assertion operator. It tells Kotlin
        //It tells Kotlin "I know this is not null, trust me" (due to the shorthand if statement which is error != null ...)
        //and forcefully unwraps the nullable value. If it actually is null at runtime it will crash. You need it here
        //because error is String? but Text only accepts String
        else -> OrderList(orders = orderList)
    }
}
