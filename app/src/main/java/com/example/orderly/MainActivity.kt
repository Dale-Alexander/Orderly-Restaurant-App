package com.example.orderly

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.orderly.ui.theme.OrderlyTheme
import com.example.orderly.navigation.AppNavigation
import com.example.orderly.utils.sendNotification
import android.Manifest
import android.content.pm.ActivityInfo
import com.example.orderly.data.datastore.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted ->//displays the pop up
        CoroutineScope(Dispatchers.IO).launch {
            UserPreferences(this@MainActivity).saveNotificationsEnabled(isGranted)
            if (isGranted) {
                val restaurantName = UserPreferences(this@MainActivity).restaurantName.first()
                sendNotification(
                    this@MainActivity,
                    "Welcome to $restaurantName",
                    "You will now receive order updates"
                )
            } else {
                Log.d("Notification", "Permission Denied")
            }
        }
    }
        /* the lambda "isGranted -> " runs when the user responds - either granting or denying */

    private val requestSmsPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()){
        permissions ->
        permissions.entries.forEach{(permission, isGranted) ->
            Log.d("SMS", "$permission granted: $isGranted")
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        //loading the notification Manager from the OS and typecasted to NotificationManager
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        /* getSystemService retrieves a system-level service from Android OS.
        * NOTIFICATION_SERVICE tells it you want the notification manager specifically.
        * "as NotificationManager" casts it to the correct type since getSystemService returns a generic Any? */
        createNotificationChannel(notificationManager)

        requestSmsPermissionsLauncher.launch(
            arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.SEND_SMS
            )
        )

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            /* On Android 13+(TIRAMISU) apps must explicitly as the user for notification permission.
            * Below Android 13 permission is granted automatically so no need to ask */
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            OrderlyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    private fun createNotificationChannel(notificationManager: NotificationManager){
        //the channel is like a category - users can go to their phone settings
        //and turn off specific channels wihout disabling all notifications
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            /* This checks if the device is running Android 8.0 (Oreo)
            or higher. NotificationChannel was introduced in Android 8 —
             if you try to create one on an older device it will crash
              because the class doesn't exist. The if prevents that by
               only running the channel creation code on devices that
                support it. */
            val infoChannel = NotificationChannel(
                "info_channel",
                "Information Channel",//name shown to user in phone settings
                NotificationManager.IMPORTANCE_DEFAULT//how intrusive the notification is
            )
            notificationManager.createNotificationChannel(infoChannel)
        }
    }
}




