import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.orderly.data.local.AppDatabase
import com.example.orderly.data.local.entity.MenuItemEntity
import com.example.orderly.data.remote.KtorClient
import com.example.orderly.data.repository.MenuRepository
import com.example.orderly.ui.components.FoodCategoryList
import com.example.orderly.ui.components.MenuItemList
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.launch


@Composable
fun MenuScreen(navController: NavController) {
    var menuItems by remember { mutableStateOf<List<MenuItemEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            val repository = MenuRepository(
                menuItemDao = AppDatabase.getDatabase(context).menuItemDao(),
                ktorClient = KtorClient.client // use your singleton
            )

            val result = repository.getMenuItems()
            result.fold(
                onSuccess = { items ->
                    Log.d("MenuScreen", "Total items: ${items.size}")
                    Log.d("MenuScreen", "Categories found: ${items.map { it.category }.distinct()}")
                    menuItems = items
                    isLoading = false
                },
                onFailure = { exception ->
                    Log.e("MenuScreen", "Error loading menu", exception) // ← add this
                    error = exception.message
                    isLoading = false
                }
            )
        }
    }

    when{
        isLoading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFFFF6B35))
        }
        error != null -> Text(error!!)
        else->{
            Column(modifier = Modifier.fillMaxSize().padding(8.dp)){
                FoodCategoryList(menuItems = menuItems, navController = navController)
            }
        }
    }
}