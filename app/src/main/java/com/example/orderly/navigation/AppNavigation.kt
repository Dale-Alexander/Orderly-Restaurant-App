package com.example.orderly.navigation

import MenuScreen
import SettingsScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.orderly.data.local.entity.MenuItemEntity
import com.example.orderly.data.remote.ViewModel.CartItem
import com.example.orderly.data.remote.ViewModel.SelectedSide
import com.example.orderly.ui.screens.CartScreen
import com.example.orderly.ui.screens.DashboardScreen
import com.example.orderly.ui.screens.ManageMenuScreen
import com.example.orderly.ui.screens.MenuItemDetailScreen
import com.example.orderly.ui.screens.OrdersScreen

@Composable
fun AppNavigation(modifier: Modifier = Modifier){
    val navController = rememberNavController()
    var cartItems by remember {mutableStateOf<List<CartItem>>(emptyList())}
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val screensWithBottomBar = listOf("Home", "Orders", "Dashboard", "Menu")

    fun addToCart(menuItem: MenuItemEntity,quantity:Int, sides: List<SelectedSide>){
        val existing = cartItems.find{it.menuItem.id == menuItem.id}//at first it is null
        cartItems = if(existing != null){//item already in cart
            cartItems.map{
            if(it.menuItem.id == menuItem.id) it.copy(quantity = it.quantity + quantity)//if it matches -> copy with quantity + new quantity
            else it // dont copy anything and leave the menuItem unchanged
            }
        }
        else{
            cartItems + CartItem(menuItem = menuItem, quantity = quantity, sides = sides)
        }
    }

    fun clearCart(){
        cartItems = emptyList()
    }

    Scaffold(
        bottomBar = {if(currentRoute in screensWithBottomBar){
            NavigationBar(containerColor = Color(0xFF1C1C1E),
                tonalElevation = 0.dp){
                NavigationBarItem(selected = currentRoute == "Home",
                    onClick = {navController.navigate("Home")},
                    icon = {Icon(Icons.Default.Home, contentDescription = "Menu")},
                    label = {Text("Menu")},
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFF6B35),
                        selectedTextColor = Color(0xFFFF6B35),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0xFF2C2C2E)
                    ))
                NavigationBarItem(
                    selected = currentRoute == "Orders",
                    onClick = { navController.navigate("Orders") },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Orders") },
                    label = { Text("Orders") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFF6B35),
                        selectedTextColor = Color(0xFFFF6B35),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0xFF2C2C2E)
                    )
                )

                // cart item with badge
                NavigationBarItem(
                    selected = currentRoute == "Cart",
                    onClick = { navController.navigate("Cart") },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (cartItems.isNotEmpty()) {
                                    Badge(containerColor = Color(0xFFFF6B35)) {
                                        Text("${cartItems.size}", color = Color.White, fontSize = 10.sp)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                        }
                    },
                    label = { Text("Cart") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFF6B35),
                        selectedTextColor = Color(0xFFFF6B35),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0xFF2C2C2E)
                    )
                )

                NavigationBarItem(
                    selected = currentRoute == "Dashboard",
                    onClick = { navController.navigate("Dashboard") },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFF6B35),
                        selectedTextColor = Color(0xFFFF6B35),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0xFF2C2C2E)
                    )
                )

            }
        }
        }
    ) {paddingValues ->
        NavHost(navController = navController, startDestination = "Home", modifier = modifier.padding(paddingValues)) {
            composable("Home") { MenuScreen(navController = navController) }
            composable("Dashboard") { DashboardScreen(navController = navController) }
            composable("Orders") { OrdersScreen(navController = navController) }
            composable("ManageMenu") { ManageMenuScreen(navController = navController) }
            composable("Settings"){ SettingsScreen(navController = navController) }
            composable("MenuItemDetail/{menuItemId}") { backStackEntry ->
                val menuItemId = backStackEntry.arguments?.getString("menuItemId") ?: ""
                MenuItemDetailScreen(
                    navController = navController,
                    menuItemId = menuItemId,
                    onAddToCart = { menuItem, quantity, sides ->
                        addToCart(
                            menuItem,
                            quantity,
                            sides
                        )
                    })
            }
            composable("Cart") {
                CartScreen(
                    navController = navController,
                    cartItems = cartItems,
                    onClearCart = { clearCart() }
                )
            }
        }
    }
}