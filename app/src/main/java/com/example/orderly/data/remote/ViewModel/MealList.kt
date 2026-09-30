package com.example.orderly.data.remote.ViewModel

import kotlinx.serialization.Serializable

@Serializable
data class MenuList(
    val meals: List<MenuItem>? = null
)
/* What @Serializable does: When your app gets a response from the API,
* it arrives as raw JSON text - it needs to be converted into a Kotlin object
* (MenuList) in order to be able to use it */