package com.example.orderly.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.orderly.data.local.AppDatabase
import com.example.orderly.data.local.entity.MenuItemEntity
import com.example.orderly.data.remote.ViewModel.SelectedSide
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun MenuItemDetailScreen(navController: NavController, menuItemId: String, onAddToCart:(MenuItemEntity, Int, List<SelectedSide>) -> Unit) {// Unit means it returns nothing but just performs an action like void in Java
    var menuItem by remember { mutableStateOf<MenuItemEntity?>(null) }
    var sides by remember { mutableStateOf<List<MenuItemEntity>>(emptyList()) }
    var selectedSides by remember { mutableStateOf<List<SelectedSide>>(emptyList()) }
    var quantity by remember { mutableStateOf(1) }
    val context = LocalContext.current

    LaunchedEffect(menuItemId) {
        val db = AppDatabase.getDatabase(context).menuItemDao()
        menuItem =
            db.getMenuItem(menuItemId)//this returns a nullable MenuITemEntity because a menu item with that id might not exist
        sides = db.getMenuItemsByCategory("Side").take(5)
        Log.d("DetailScreen", "Sides count: ${sides.size}")
        Log.d("DetailScreen", "Sides: ${sides.map { it.name }}")
    }
    menuItem?.let { foodItem ->//"if this is not null, run this block with it"
        val sidesTotal = selectedSides.sumOf { it.menuItem.price * it.quantity }
        val orderTotal = sidesTotal + (foodItem.price * quantity)
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {

                //image
                item {
                    /* This is a lazyColumn DSL function that adds a single item to the
                        * list. You build its content using item{} - adds one single composable,
                        * items(list){}  - adds one composable per item in a list */
                    Box {
                        AsyncImage(
                            model = foodItem.imageUrl,
                            contentDescription = foodItem.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(280.dp)
                        )


                        //back button
                        Box(
                            modifier = Modifier.padding(16.dp).size(36.dp)
                                .clip(shape = CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable { navController.popBackStack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                //content
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFF121212))
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = foodItem.name,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = "€%.2f".format(foodItem.price),
                                color = Color(0xFFFF6B35),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = foodItem.category ?: "Unknown",
                            color = Color(0xFFFF6B35),
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))


                        Text(
                            text = "Description",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = foodItem.description,
                            color = Color.Gray,
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        //quantity selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Quantity",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(32.dp).clip(shape = CircleShape)
                                        .background(Color(0xFF2C2C2E))
                                        .clickable { if (quantity > 1) quantity-- },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("-", color = Color.White, fontSize = 18.sp)
                                }
                                Text(
                                    text = "${quantity}",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier.size(32.dp).clip(shape = CircleShape)
                                        .background(Color(0xFF2C2C2E))
                                        .clickable { quantity++ },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", color = Color.White, fontSize = 18.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                        if (sides.isNotEmpty()) {
                            Text(
                                "Add Sides",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            sides.forEach { side ->
                                val selectedSide =
                                    selectedSides.find { it.menuItem.id == side.id }//at first it is null
                                val qty = selectedSide?.quantity ?: 0
                                /* this value isnt actually incremented/decremented as you might think.
                                * You dont do qty++, qty-- manually. It reads the current quantity from selectedSides.
                                * The actual update happens when you reassign selectedSides inside clickable blocks
                                * - and since selectedSides is a var with mutableStateOf, Compose will recompose and qty will
                                * be recalculated with the new value on the next recomposition.The same is true for selectedSide */
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                        /* .weight(1f) is added to Column which is a child of Row. It tells
                                        * the Column to take up all the remaining space after the buttons have
                                        * taken their fixed size. Without it the Column would expand as wide as its content needs,
                                        * potentially pushing the buttons off screen when the text is long.
                                        * The button here is referring to the "+" Icon
                                        * The Padding adds 12dp of space on the right side of the column so if a side name
                                        * is too long, they will be spaced out between each other */
                                        Text(
                                            text = side.name,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "+%.2f".format(side.price),
                                            color = Color(0xFFFF6B35),
                                            fontSize = 12.sp
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        if (qty > 0) {
                                            Box(
                                                modifier = Modifier.size(28.dp).clip(CircleShape)
                                                    .background(Color(0xFF2C2C2E))
                                                    .clickable {
                                                        selectedSides = if (qty == 1) {
                                                            selectedSides.filter { it.menuItem.id != side.id }//quantity hits 0, remove entirely
                                                        } else {
                                                            selectedSides.map {
                                                                if (it.menuItem.id == side.id) it.copy(
                                                                    quantity = it.quantity - 1
                                                                ) //decrement
                                                                else it
                                                            }
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("-", color = Color.White, fontSize = 16.sp)
                                            }
                                            Text(
                                                text = "${qty}",
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Box(
                                            modifier = Modifier.size(28.dp)
                                                .clip(shape = CircleShape)
                                                .background(Color(0xFFFF6B35))
                                                .clickable {
                                                    selectedSides = if (selectedSide == null) {
                                                        //not in list yet add it with quantity
                                                        selectedSides + SelectedSide(side, 1)
                                                    } else {
                                                        selectedSides.map {
                                                            if (it.menuItem.id == side.id) it.copy(
                                                                quantity = it.quantity + 1
                                                            ) //already exists, incremenet
                                                            else it
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                "+",
                                                color = Color.White,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        Button(
                            onClick = {
                                onAddToCart(foodItem, quantity, selectedSides)
                                navController.popBackStack()
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35))
                        ) {
                            Text(
                                text = "Add to Order  •  €%.2f".format(orderTotal),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}