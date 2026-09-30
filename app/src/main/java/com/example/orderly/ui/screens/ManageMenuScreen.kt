package com.example.orderly.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.orderly.data.local.AppDatabase
import com.example.orderly.data.local.entity.MenuItemEntity
import com.example.orderly.data.remote.KtorClient
import com.example.orderly.data.remote.ViewModel.MenuItem
import com.example.orderly.data.remote.ViewModel.toEntity
import com.example.orderly.data.repository.MenuRepository
import kotlinx.coroutines.launch

@Composable
fun ManageMenuScreen(navController: NavController){
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember{
        MenuRepository(AppDatabase.getDatabase(context).menuItemDao(), KtorClient.client)
    }

    val categories = listOf("Seafood", "Chicken", "Pasta", "Beef")
    var selectedCategory by remember{mutableStateOf(categories[0])}
    var apiItems by remember{mutableStateOf<List<MenuItem>>(emptyList())}
    var roomIds by remember{mutableStateOf<Set<String>>(emptySet())}
    var isLoading by remember{mutableStateOf(false)}
    var expandedItemId by remember{mutableStateOf<String?>(null)}

    var priceInput by remember{mutableStateOf("")}

    fun loadCategory(category:String){
        scope.launch{
            isLoading =true
            repository.getApiItemsForCategory(category).fold(
                onSuccess = {
                    meals -> apiItems = meals
                    roomIds = repository.getMenuItemsByCategory(category).map{it.id}.toSet()
                },
                onFailure = {}
            )
            isLoading = false
        }
    }

    LaunchedEffect(selectedCategory){
        loadCategory(selectedCategory)
    }
    //price dailog

    //main UI
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212)).padding(16.dp)){
        Row(verticalAlignment = Alignment.CenterVertically){
            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(50))
                .background(Color(0xFF2C2C2E)),
                contentAlignment = Alignment.Center){
                IconButton(onClick = {navController.popBackStack()}){
                    Icon(Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column{
                Text("Manage Menu", color = Color.White, fontSize = 20.sp,fontWeight = FontWeight.Bold)
                Text("Add or remove items", color = Color.Gray, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)){
            categories.forEach{ category ->
                val selected = category == selectedCategory
                Button(onClick = {selectedCategory = category
                                 expandedItemId = null
                                 priceInput = ""},
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if(selected) Color(0xFFFF6B35) else Color(0xFF2C2C2E)
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ){
                    Text(category, color = Color.White, fontSize = 13.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if(isLoading){
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                CircularProgressIndicator(color = Color(0xFFFF6B35))
            }
        }
        else{
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)){
                items(apiItems, key = {it.id}){
                    item ->
                    val isInMenu = item.id in roomIds
                    val isExpanded = expandedItemId == item.id
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1C1C1E)).padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)){
                        AsyncImage(model = item.imageUrl,
                            contentDescription = item.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)))

                            Text(item.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))


                        Button(onClick = {
                            if(isInMenu) {
                                scope.launch {
                                    val entity = repository.getMenuItem(item.id) ?: return@launch
                                    repository.deleteMenuItem(entity)
                                    roomIds = roomIds - item.id
                                }
                            }
                            else{
                                expandedItemId = if(isExpanded) null else item.id
                                priceInput = ""
                            }
                        },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if(isInMenu) Color(0xFF3A1A1A) else Color(0xFF1A3A1A)),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ){
                            Text(text = if(isInMenu) "Remove" else "Add", color =  if(isInMenu) Color(0xFFFF4444) else Color(0xFF44CC44), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if(isExpanded){
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically){
                            OutlinedTextField(
                                value = priceInput,
                                onValueChange = { priceInput = it },
                                placeholder = { Text("Price e.g. 12.99") },
                                prefix = { Text("€", color = Color.White) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF6B35),
                                    unfocusedBorderColor = Color(0xFF444444),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Button(
                                onClick = {
                                    val price = priceInput.toDoubleOrNull() ?: return@Button
                                    scope.launch {
                                        val categoryIndex = categories.indexOf(selectedCategory)
                                        val letter = ('A' + categoryIndex)
                                        val lastCode = repository.getLastCodeForCategory(selectedCategory)
                                        val nextNumber = lastCode?.drop(1)?.toIntOrNull()?.plus(1) ?: 1
                                        //drop the letter and add 1 since you cant assign the same code to multiple items, when adding you need to increment the code
                                        val code = "$letter${nextNumber.toString().padStart(2, '0')}"
                                        val entity = item.toEntity(selectedCategory, code).copy(price = price)
                                        repository.addMenuItem(entity)
                                        roomIds = roomIds + item.id
                                        expandedItemId = null
                                        priceInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Confirm", color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

    }



}