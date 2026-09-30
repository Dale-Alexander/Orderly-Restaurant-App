import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.orderly.data.datastore.UserPreferences
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(navController:NavController) {

    val focusManager = LocalFocusManager.current
    var openingTimeError by remember{mutableStateOf(false)}
    var closingTimeError by remember{ mutableStateOf(false) }
    val timeRegex = Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")

    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    //this creates an instance of your UserPReferences class,
    //passing in the context so it can access the DataStore file on the device
    val coroutineScope = rememberCoroutineScope()

    var restaurantNameInput by remember { mutableStateOf("") }
    var openingTime by remember { mutableStateOf("09:00") }
    var closingTime by remember { mutableStateOf("22:00") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var preferencesLoaded by remember{ mutableStateOf(false)}

    // Load preferences from DataStore
    LaunchedEffect(Unit) {
        launch { userPreferences.restaurantName.collect { name -> restaurantNameInput = name } }
        launch { userPreferences.openingTime.collect { time -> openingTime = time } }
        launch { userPreferences.closingTime.collect { time -> closingTime = time } }
        launch { userPreferences.notificationsEnabled.collect { enabled -> notificationsEnabled = enabled;preferencesLoaded = true } }
        //preferencesLoaded becomes true after all the preferences have been loaded
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit){
                detectTapGestures{focusManager.clearFocus()}
            }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(50))
                    .background(Color(0xFF2C2C2E)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Restaurant Name
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Restaurant Name", color = Color(0xFF888888), fontSize = 13.sp)
            OutlinedTextField(
                value = restaurantNameInput,
                onValueChange = { restaurantNameInput = it },
                placeholder = { Text("Enter restaurant name") },
                modifier = Modifier.fillMaxWidth().onFocusChanged { focusState ->
                    if (!focusState.isFocused && restaurantNameInput.isNotBlank() && preferencesLoaded) {
                        coroutineScope.launch { userPreferences.saveRestaurantName(restaurantNameInput) }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF6B35),
                    unfocusedBorderColor = Color(0xFF444444),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        }

        HorizontalDivider(color = Color(0xFF2C2C2E))

        // Opening Hours
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Opening Hours (Mon - Fri)",
                color = Color(0xFF888888),
                fontSize = 13.sp
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Opens", color = Color(0xFF888888), fontSize = 11.sp)
                    OutlinedTextField(
                        value = openingTime,
                        onValueChange = {
                            openingTime = it
                            openingTimeError = !timeRegex.matches(it)
                        },
                        placeholder = { Text("09:00") },
                        isError = openingTimeError,
                        supportingText = {
                            if (openingTimeError) Text("Use HH:mm eg: 09:00", color = Color.Red, fontSize = 11.sp)
                        },
                        singleLine = true,
                        modifier = Modifier.onFocusChanged { focusState ->
                            if (!focusState.isFocused && !openingTimeError && preferencesLoaded) {
                                coroutineScope.launch { userPreferences.saveOpeningTime(openingTime) }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF6B35),
                            unfocusedBorderColor = Color(0xFF444444),
                            errorBorderColor = Color.Red,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                Text("—", color = Color(0xFF888888), fontSize = 18.sp, modifier = Modifier.padding(top = 20.dp))

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Closes", color = Color(0xFF888888), fontSize = 11.sp)
                    OutlinedTextField(
                        value = closingTime,
                        onValueChange = {
                            closingTime = it
                            closingTimeError = !timeRegex.matches(it)
                        },
                        placeholder = { Text("22:00") },
                        isError = closingTimeError,
                        supportingText = {
                            if (closingTimeError) Text("Use HH:mm e.g. 22:00", color = Color.Red, fontSize = 11.sp)
                        },
                        singleLine = true,
                        modifier = Modifier.onFocusChanged { focusState ->
                            if (!focusState.isFocused && !closingTimeError && preferencesLoaded) {
                                coroutineScope.launch { userPreferences.saveClosingTime(closingTime) }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF6B35),
                            unfocusedBorderColor = Color(0xFF444444),
                            errorBorderColor = Color.Red,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            }
        }

        HorizontalDivider(color = Color(0xFF2C2C2E))

        // Notifications
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Notifications", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Order updates and alerts", color = Color(0xFF888888), fontSize = 13.sp)
            }
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = { isChecked ->
                    if (isChecked) {
                        /* Only do the permission check when the user is turning the switch on.
                        * If they're turning it off, just save false directly - no permission check needed */
                        // Check if OS permission is actually granted
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                            != PackageManager.PERMISSION_GRANTED) {
                            // Permission denied at OS level, open system settings

                            /* Intent(settings...): Creates an intent that opens your app's page in the system settings
                            * where the user can manually toggle permissions */
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            /* .apply{data = ...}: Tells the intent which app to open in settings
                            * context.packageName is your app's package name eg: com.example.orderly */

                            context.startActivity(intent)
                            //the above actualy opens the system settings page
                        } else {//if OS permission is already granted, just flip the switch and save true
                            notificationsEnabled = true
                            coroutineScope.launch { userPreferences.saveNotificationsEnabled(true) }
                        }
                    } else {
                        notificationsEnabled = false
                        coroutineScope.launch { userPreferences.saveNotificationsEnabled(false) }
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFFFF6B35)
                )
            )
        }
    }
}