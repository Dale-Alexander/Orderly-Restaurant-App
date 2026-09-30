package com.example.orderly.data.remote.ViewModel

import com.example.orderly.data.local.entity.MenuItemEntity

data class CartItem(
    val menuItem: MenuItemEntity,
    val quantity:Int,
    val sides: List<SelectedSide> = emptyList()
    )