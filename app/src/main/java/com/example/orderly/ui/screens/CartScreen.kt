package com.example.orderly.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.orderly.data.datastore.UserPreferences
import com.example.orderly.data.local.AppDatabase
import com.example.orderly.data.local.entity.OrderEntity
import com.example.orderly.data.local.entity.OrderItemEntity
import com.example.orderly.data.remote.ViewModel.CartItem
import com.example.orderly.data.repository.OrdersRepository
import com.example.orderly.utils.sendNotification
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun PaymentRow(
    label: String,
    value: String,
    labelColor: Color = Color.Gray,
    valueColor: Color = Color.White,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = labelColor, fontSize = 13.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(text = value, color = valueColor, fontSize = 13.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun CartScreen(
    navController: NavController,
    onClearCart:() -> Unit,
    cartItems: List<CartItem>
    ){

    val focusManager = LocalFocusManager.current
    var customerName by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val subTotal = cartItems.sumOf{item -> (item.menuItem.price * item.quantity) + item.sides.sumOf{side -> side.quantity * side.menuItem.price}}
    val tax = subTotal * 0.10
    val total = subTotal + tax

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121212)).pointerInput(Unit){detectTapGestures{focusManager.clearFocus()}}){
        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ){
            item{
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically){
                    Box(modifier = Modifier.size(36.dp)
                        .clip(CircleShape).background(Color(0xFF2C2C2E))
                        .clickable{navController.popBackStack()},
                        contentAlignment = Alignment.Center){
                        Icon(Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                            )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = "Your Order Summary",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                        )
                    Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            items(cartItems){cartItem ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)){
                    AsyncImage(
                        model = cartItem.menuItem.imageUrl,
                        contentDescription = cartItem.menuItem.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(60.dp).clip(RoundedCornerShape(10.dp))
                    )
                    Column(modifier = Modifier.weight(1f)){
                        Text(text = cartItem.menuItem.name,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold)
                        if(cartItem.sides.isNotEmpty()){
                            Text(text = cartItem.sides.joinToString(", "){it.menuItem.name},
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                        Text(text = "€%.2f".format((cartItem.menuItem.price * cartItem.quantity) + (cartItem.sides.sumOf{it.menuItem.price * it.quantity})),
                            color = Color(0xFFFF6B35),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold)
                    }
                    Text(text = "x${cartItem.quantity}",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
                HorizontalDivider(color = Color(0xFF2C2C2E))
            }
            item{
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Payment Summary",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1C1C1E))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ){
                    PaymentRow("Subtotal", "€%.2f".format(subTotal))
                    PaymentRow("Tax & Service (10%)", "€%.2f".format(tax))
                    HorizontalDivider(color = Color(0xFF2C2C2E))
                    PaymentRow(
                        label = "Total Payment",
                        value = "€%.2f".format(total),
                        labelColor = Color.White,
                        valueColor = Color(0xFFFF6B35),
                        bold = true
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Your Details",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    placeholder = { Text("Enter your name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF6B35),
                        unfocusedBorderColor = Color(0xFF444444),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        Row(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
            .background(Color(0xFF121212))
            .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)){
            Button(
                onClick = {onClearCart()},
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E)))
                {
                    Text("Clear Cart", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            Button(onClick = {
                scope.launch{
                    val userPreferences = UserPreferences(context)
                    val openingTime = userPreferences.openingTime.first()
                    val closingTime = userPreferences.closingTime.first()
                    val formatter = DateTimeFormatter.ofPattern("HH:mm")

                    val now = LocalTime.now()
                    val opening = LocalTime.parse(openingTime, formatter)
                    val closing = LocalTime.parse(closingTime, formatter)

                    if(now.isBefore(opening) || now.isAfter(closing)){
                        Toast.makeText(context, "We're currently closed. We're open from $openingTime till $closingTime", Toast.LENGTH_LONG).show()
                        return@launch
                    }


                    val repository = OrdersRepository(AppDatabase.getDatabase(context).orderDao())
                    val order = OrderEntity(customerName = customerName, totalPrice = total)
                    val items = cartItems.flatMap { cartItem ->
                        /* If an array contains subarrays within it, .flatMap flattens it into 1 array */
                        val mainItem = OrderItemEntity(
                            orderId = 0,
                            /*orderId shouldnt be set to order.id. 0 is actually correct here
                             order.id(not orderId) is 0 at this point because the OrderEntity hasnt beein inserted yet -
                              Room hasnt generated the real id. The actual id gets assigned inside insertOrder in the
                              repository. So you pass orderId = 0 as a placeholder and the repository replaces it
                              with the real generated id before inserting the items. You can set it to -1, 999
                              or any number, it doesnt matter because insertOrder always overwrites it. */
                            itemId = cartItem.menuItem.id,
                            quantity = cartItem.quantity,
                            price = cartItem.menuItem.price
                        )
                        val sideItems = cartItem.sides.map{
                            side -> OrderItemEntity(
                                orderId = 0,
                                itemId = side.menuItem.id,
                                quantity = side.quantity,
                                price = side.menuItem.price
                            )
                        }
                        listOf(mainItem) + sideItems
                        /* mainItem is a single OrderItemEntity, sideItems is a List<OrderItemEntity>.
                        * You cant do mainItem + sideItems because you cant add a single item to a list directly.
                        * So you wrap mainItem in listOF */
                    }
                    repository.insertOrder(order, items).fold(
                        onSuccess = {
                            sendNotification(context = context, title = "New Order Recieved!", message = "You have a new order. Check your orders")
                            onClearCart()
                            navController.navigate("Orders")
                        },
                        onFailure = { Log.e("Cart", "Failed to place order", it)}
                    )
                }
            },
            modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35))
            ){
                Text("Order Now", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}