package com.example.orderly.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orderly.data.remote.ViewModel.OrderWithItems
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderCard(orderWithItems: OrderWithItems){
    val order = orderWithItems.order
    val items = orderWithItems.items

    Card(modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
    ){
        Column(modifier = Modifier.padding(12.dp)){
                Text(text = "Order #${order.id}",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )

            Text(
                text = order.customerName,
                color = Color(0xFFFF6B35),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = formatTime(order.createdAt),
                color = Color.Gray,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF2C2C2E))
            Spacer(modifier = Modifier.height(10.dp))

            items.forEach { orderWithMenuItem ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${orderWithMenuItem.orderItem.quantity}x ${orderWithMenuItem.menuItem.name}",
                        color = Color.White,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "€%.2f".format(orderWithMenuItem.orderItem.price),
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFF2C2C2E))
                    Spacer(modifier = Modifier.height(10.dp))

                    //total

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
                        Text(text = "Total",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(text = "€%.2f".format(order.totalPrice),
                            color = Color(0xFFFF6B35),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp)
                    }
                }
            }
        }

fun formatTime(timestamp:Long):String{
    val format = SimpleDateFormat("dd-MM-yyyy, HH:mm", Locale.getDefault())
    return format.format(Date(timestamp))
}