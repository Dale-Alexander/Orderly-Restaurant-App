package com.example.orderly.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.Bitmap
import android.widget.RemoteViews
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.example.orderly.R
import com.example.orderly.data.local.AppDatabase
import com.example.orderly.data.repository.OrdersRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.orderly.data.datastore.UserPreferences
import kotlinx.coroutines.flow.first

/**
 * Implementation of App Widget functionality.
 */
class OrderlyWidget : AppWidgetProvider() {
    //inherits from AppWidgetProvider which is Android's base class for widgets. It handles
    //widget lifecylce events
    override fun onUpdate(
        //called every hour based on updatePeriodMills and when the widget is first added
        //upudatePeriodMills can be seen in orderly_widget_info
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
        /* appWidgetIds is an array because the user might have multiple instances
        * of your widget to their home screen */
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
            /* Loops through every widget instance and updates each one seperately */
        }
    }

    override fun onEnabled(context: Context) {
        //this is called when the first instance of the widget is added to the home screen.
        //Good place to start any background work
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        //Called when the last instance of the widget is removed from the home screen.
        //Good place to clean up any background work
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    //internal means accessible anywhere within the same module(your app) but not from
    //outside. It's outside the class so it can be called as a standalone function(without the class name as a prefix)
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    CoroutineScope(Dispatchers.IO).launch{

        /* CoroutineScope creates a scope to launch coroutines in. Since
        * AppWidgetProvider is not a ViewModel or Activity, it has no built in
        * couroutine scope so you create one manually.
        * Dispatchers.IO tells the coroutine which thread to run on. There are 3 main
        * dispatchers:
        *   Dispatchers.IO - background thread for database/network operations
        *   Dispatchers.Main - main UI thread
        *
        * You use Dispatchers.IO here because youre doing a database query
        *   Dispatchers.Default - background thread for heavy CPU work like sorting large lists */
        val repository = OrdersRepository(AppDatabase.getDatabase(context).orderDao())
        val restaurantName = UserPreferences(context).restaurantName.first()
        val topSellingItemId = repository.getTopSellingItemIdToday()
        val topSellingItemImageUrl = topSellingItemId?.let{
            AppDatabase.getDatabase(context).menuItemDao().getMenuItem(topSellingItemId)?.imageUrl
        }
        var totalOrdersToday = 0
        var totalRevenueToday = 0.0
        var topItem = "N/A"



        repository.getWidgetData().fold(
            onSuccess = {data ->
                totalOrdersToday = data.todayOrders
                totalRevenueToday = data.todayRevenue
                topItem = data.topSellingItem.name
            },
            onFailure = {}
        )

        val bitmap = topSellingItemImageUrl?.let{loadImage(context, it)}

        val views = RemoteViews(context.packageName, R.layout.orderly_widget)
        /* RemoteViews is a special view system for widgets - widgets run in a seperate process
        * from your app(the home screen launcher process), so you cant use normal view references. RemoteViews
        * is Android. RemoteViews is Android's way of updating views across processes.
        * You reference views by their id and set values ugin methods like setTextViewText
        *
        * Views - in Android, "views" means UI components. Every TextView, LinearLayout,
        * Button etc is a View. So RemoteViews is literally a container of views that can be sent to another
        * process(the home screen). Think of it as a snapshot of your UI that gets delivered
        * to the launcher*/
        views.setTextViewText(R.id.widget_orders, "${totalOrdersToday}")
        views.setTextViewText(R.id.widget_revenue, "%.2f".format(totalRevenueToday))
        views.setTextViewText(R.id.widget_top_item, topItem)
        views.setTextViewText(R.id.widget_title, restaurantName)

            //the above widgets can be seen in orderly_widget.xml in layout package


        if(bitmap != null){
            views.setImageViewBitmap(R.id.widget_top_item_image, bitmap)
        }
        else{
            views.setImageViewResource(R.id.widget_top_item_image, R.drawable.baseline_broken_image_24)
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
        //pushes the updated RemoteViews to the home screen launcher so it actually renders the new data
    }

}
fun loadImage(context:Context, imageUrl:String): Bitmap? {
    return Glide.with(context)
        .asBitmap()
        .load(imageUrl)
        .submit()
        .get()
}