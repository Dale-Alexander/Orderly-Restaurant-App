package com.example.orderly.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.example.orderly.data.datastore.UserPreferences
import com.example.orderly.data.local.AppDatabase
import com.example.orderly.data.local.entity.OrderEntity
import com.example.orderly.data.local.entity.OrderItemEntity
import com.example.orderly.data.repository.OrdersRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class SmsReceiver: BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent){
        /* intent: The intent is what Android passes to your
        * BroadcastReceiver when an SMS arrives. Think of it as a package
        * that Android delivers to your app containing all the informationo about
        * the incoming SMS. Its not just for SMS - intents are Android's
        * general way of passing data between components(activities, services, receivers).
        * In this case the intent's action is SMS_RECEIVED and it carries the actual message
        * data inside its extras */
        val messages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Telephony.Sms.Intents.getMessagesFromIntent(intent)
        } else {
            val bundle = intent.extras ?: return
            /* this returns a bundle which is essentially a key-value map that
            * Android uses to pass around. Think of it like a bag where each item has a label.
            * In the SMS case, one of those labels is "pdus" which holds the actual message data
            *  */
            @Suppress("DEPRECATION")
            /* pdus: PDU stands for Protocol Data Unit - it's the raw binary format
            * that SMS messages are transmitted in over the mobile network. When your phone
            * receives an SMS, the network doesnt send it as plain text - it sends it as a PDU
            * which contains:
            * The sender's number
            * The message text,
            * Timestamp,
            * Encoding information,
            * other metadata
            *
            * There can be multiple PDUs because long SMS messages get split into multiple parts
            * over the network, which is why its an array
            *  */
            (bundle["pdus"] as? Array<*>)?.map { pdu ->
                /* Array<*>: This is Kotlin's star projection, meaning "an array of something
                * but we dont know exactly what type". Android Bundle system returns it as a generic Array<*> because
                * at the bundle level Android doesnt know the specific type(it comes out as Any?
                * and when you cast it with as? Array<*> youre saying "this should be an array of something"). That's why you need to
                * cast each element with pdu as ByteArray - youre telling Kotlin, "trust me,
                * each element in this array is actually a ByteArray"
                *
                * It is nullable because not every intent carries extra data. In the SMS case, it always should have data,
                * but Android cant guarantee that - the intent might have been malformed, or in rare cases
                * the system might send an empty intent/ */
                SmsMessage.createFromPdu(pdu as ByteArray)
                /* ByteArray: Since a PDU is raw binary data(not text), each PDU
                * is represented as a ByteArray, literally an array of bytes(0s and 1s grouped into bytes).
                * SmsMessage.createFromPdu takes that raw binary data and decodes it
                * into a proper SmsMessage object that you can work with */
            }?.toTypedArray()
        } ?: return//if there is no extra information in the bundle, do nothing and exit

        val senderNumber = messages[0].originatingAddress ?: return
        /* originatingAddress: This is the sender's phone number extracted from the PDU,
        * It's called address rather than number because in telephony, phone numbers are technically
        * called addresses. It's nullable because in some cases (like messages
        * from short codes or certain network messages), the originating address might not be present */
        val message = messages.joinToString("") { it.messageBody }.trim()
        /* as mentioned earlier messages is an array of SmsMessage objects. Long SMS messages
        * get split into multiple parts over the network, so one SMS could arrive as multiple
        * SmsMessage objects in the array
        *
        * it.messageBody: instead of joining the SmsMessage objects
        * themselves(which wouldnt make sense), it extracts the text content from each one first*/

        Log.d("SmsReceiver", "Sender: $senderNumber")



        CoroutineScope(Dispatchers.IO).launch{
            when{
                message.uppercase() == "MENU" -> handleMenu(context, senderNumber)
                message.uppercase() == "ORDER" -> handleEmptyOrder(context, senderNumber)
                message.uppercase().startsWith("ORDER") -> handleOrder(context, message, senderNumber)
                else -> handleUnknown(context, senderNumber)
                }
            }
        }
    }

    fun handleUnknown(context:Context, senderNumber:String) {
        CoroutineScope(Dispatchers.IO).launch {
            val restaurantName = UserPreferences(context).restaurantName.first()
            sendSms(
                senderNumber, """
                    Welcome to $restaurantName! 🍽️
                    ─────────────────────
                    Reply MENU to see our menu
                    Reply ORDER to place an order
        """.trimIndent(), context
            )
        }
    }

    fun sendSms(senderNumber: String, message: String, context: Context) {
        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }
        val parts = smsManager?.divideMessage(message)
        if (parts != null && parts.size > 1) {
            smsManager.sendMultipartTextMessage(senderNumber, null, parts, null, null)
        } else {
            smsManager?.sendTextMessage(senderNumber, null, message, null, null)
        }
        /* 1. the recipient's phone number
        * 2. the SMS service center address. null means use the default one from the SIM card
        * 3. the actual message
        * 4. sentIntent - a PendingIntent that fires when the message is sent. null means we dont care about the sent confirmation
        * 5. a PendingIntent that fires when the message is delivered to the recipient. null means we dont care about delivery confirmation*/

        /* a single SMS can only hold 160 characters and your menu is way longer than that. You need to use sendMultipartTextMessage instead:
        * divideMessage automatically splits the message into 160 character chunks and sendMultipartTextMessage sends them all as one logical message. Did your mum receive anything?
        *  */


    }

    suspend fun handleEmptyOrder(context: Context, senderNumber: String) {
        val userPreferences = UserPreferences(context)
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val openingTime = userPreferences.openingTime.first()
        val closingTime = userPreferences.closingTime.first()
        val now = LocalTime.now()
        val opening = LocalTime.parse(openingTime, formatter)
        val closing = LocalTime.parse(closingTime, formatter)

        if(now.isBefore(opening) || now.isAfter(closing)){
            sendSms(senderNumber,"Sorry we're currently closed. We're open from $openingTime to $closingTime", context)
            return
        }


        sendSms(senderNumber, """
        To place an order reply with:
        ─────────────────────────────
        ORDER
        Name: [your name]
        [quantity]x [code]
        
        EXAMPLE:
        ORDER
        Name: John
        1x A01
        2x B03
    """.trimIndent(), context)
    }

    suspend fun handleMenu(context: Context, senderNumber: String) {
        val db = AppDatabase.getDatabase(context)
        val restaurantName = UserPreferences(context).restaurantName.first()
        val categories = listOf("Seafood", "Chicken", "Pasta", "Beef")

        val menuText = StringBuilder()
        menuText.appendLine("${restaurantName.uppercase()} MENU")
        menuText.appendLine("─────────────")

        categories.forEach { category ->
            val items = db.menuItemDao().getMenuItemsByCategory(category).take(3)
            if (items.isNotEmpty()) {
                menuText.appendLine()
                menuText.appendLine(category.uppercase())
                items.forEach { item ->
                    menuText.appendLine("${item.code} - ${item.name} - €${"%.2f".format(item.price)}")
                }
            }
        }

        menuText.appendLine()
        menuText.appendLine("Reply ORDER to place an order")

        sendSms(senderNumber, menuText.toString(), context)
    }

    suspend fun handleOrder(context:Context, message:String, senderNumber:String){
        val userPreferences = UserPreferences(context)
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val openingTime = userPreferences.openingTime.first()
        val closingTime = userPreferences.closingTime.first()
        val opening = LocalTime.parse(openingTime, formatter)
        val closing = LocalTime.parse(closingTime, formatter)
        //.parse() brings them back to LocalTime type
        val now = LocalTime.now()

        if(now.isBefore(opening) || now.isAfter(closing)){
            sendSms(senderNumber, "Sorry, we're currently closed. We're open from $openingTime to $closingTime", context)
            return
        }

        val db = AppDatabase.getDatabase(context)
        val lines = message.lines().map{it.trim()}.filter{it.isNotEmpty()}
        /* message.lines() splits the message into a list by line breaks
        * ORDER\NName: John\n1x
        *  becomes ["ORDER", "Name: John", "1x"]
        *
        * .trim() removes any leading whitespace from each element,
        * .filter{it.isNotEmpty()} removes any blank lines*/

        val nameLine = lines.find{it.uppercase().startsWith("NAME:")}

        if(nameLine == null){
            sendSms(senderNumber, """
                Missing name! Please use the format:
                ORDER
                Name: [your name]
                [quantity]x [code]
            """.trimIndent(), context)
            return
        }
        val customerName = nameLine.substringAfter(":").trim()

        //extract order lines(everything except ORDER and Name: )
        val orderLines = lines.filter{
            !it.uppercase().startsWith("ORDER") && !it.uppercase().startsWith("NAME:")
        }

        if(orderLines.isEmpty()){
            handleEmptyOrder(context, senderNumber)
            return
        }

        val orderItems = mutableListOf<OrderItemEntity>()
        var totalPrice = 0.0
        val confirmationItems = StringBuilder()

        for(line in orderLines){
            //Match format "1x A01"
            val regex = Regex("""(\d+)x\s+([A-Z]\d{2})""", RegexOption.IGNORE_CASE)
            val match = regex.find(line)

            if(match == null){
                sendSms(senderNumber, "Invalid format: \"$line\" Please use format: 1x A01", context)
                return
            }

            val quantity = match.groupValues[1].toInt() // -> 1
            val code = match.groupValues[2].uppercase() // A01

            /* The parentheses in the regex create capture groups -
            * they isolate specific parts of the match. groupValues is a list where:
            * groupValues[0] = the entire match "1x A01"
            * groupValues[1] = the first capture group eg: "1"
            * groupValues[2] = "A01"
            */

            val menuItem = db.menuItemDao().getMenuItemByCode(code)
            if(menuItem == null){
                sendSms(senderNumber, "Item code $code does not exist. Reply MENU to see available items", context)
                return
            }

            val itemTotal = quantity * menuItem.price
            totalPrice += itemTotal
            confirmationItems.appendLine("${quantity}x${menuItem.name} - €${"%.2f".format(itemTotal)}")

            orderItems.add(
                OrderItemEntity(
                    orderId = 0,
                    itemId = menuItem.id,
                    quantity = quantity,
                    price = menuItem.price
                )
            )
        }

        val order = OrderEntity(
            customerName = customerName,
            totalPrice = totalPrice
        )

        val repository = OrdersRepository(db.orderDao())
        repository.insertOrder(order, orderItems)

        sendSms(senderNumber, """
                    Order confirmed! ✅
                    ───────────────────
                    Name: $customerName
                    
                    ${confirmationItems.toString().trim()}
                    
                    Total: €${"%.2f".format(totalPrice)}
                    
                    Thank you for ordering from Orderly!
        """.trimIndent(), context)
    }
