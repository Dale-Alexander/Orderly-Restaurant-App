package com.example.orderly.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FoodCategoryButton(modifier: Modifier = Modifier, text:String,isSelected:Boolean = false, onClick: () -> Unit = {}){
    Button(onClick=onClick,modifier = modifier.height(42.dp).padding(4.dp), shape = RoundedCornerShape((10.dp)),contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp) ,colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) Color(0xFFFF6B35) else Color(0xFF2C2C2E))) {
        Text(text = text, color = if(isSelected) Color.White else Color.Gray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}