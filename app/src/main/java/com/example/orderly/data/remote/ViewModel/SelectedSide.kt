package com.example.orderly.data.remote.ViewModel

import com.example.orderly.data.local.entity.MenuItemEntity

data class SelectedSide(
    val menuItem: MenuItemEntity,
    val quantity:Int
)