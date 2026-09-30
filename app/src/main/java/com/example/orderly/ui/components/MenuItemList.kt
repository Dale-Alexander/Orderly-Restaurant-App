package com.example.orderly.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.orderly.data.local.entity.MenuItemEntity
import com.example.orderly.data.remote.ViewModel.MenuItem

@Composable
fun MenuItemList(menuItems:List<MenuItemEntity>, modifier: Modifier = Modifier, navController: NavController){
    val newItemsList = menuItems.chunked(2)
    LazyColumn(modifier = modifier.fillMaxSize().padding(7.dp)){
        items(newItemsList){
                rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
                rowItems.forEach {
                        menuItem ->
                    MenuItem(menuItem = menuItem, modifier = Modifier.weight(1f).fillMaxHeight(), navController = navController)
                }
                if(rowItems.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}