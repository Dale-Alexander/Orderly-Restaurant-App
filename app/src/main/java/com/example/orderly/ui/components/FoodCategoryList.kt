package com.example.orderly.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.orderly.data.local.entity.MenuItemEntity
import com.example.orderly.data.remote.ViewModel.MenuItem

@Composable
fun FoodCategoryList(modifier: Modifier = Modifier, menuItems:List<MenuItemEntity>, navController:NavController){
    var selectedCategory by remember {mutableStateOf("All")}
    val categoryList = listOf<String>("All", "Pasta", "Seafood", "Beef", "Chicken")
    val filteredItems = if(selectedCategory == "All")
        menuItems
    else{
        menuItems.filter{it.category == selectedCategory}
    }
    Column {
        Row(modifier.fillMaxWidth()) {
            categoryList.forEach {
                FoodCategoryButton(
                    modifier = Modifier.weight(1f),
                    text = it,
                    isSelected = it == selectedCategory,
                    onClick = { selectedCategory = it })
            }
        }
        MenuItemList(menuItems = filteredItems, navController = navController)
    }
}