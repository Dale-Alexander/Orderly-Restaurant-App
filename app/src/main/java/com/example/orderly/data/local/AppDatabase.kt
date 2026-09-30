package com.example.orderly.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.orderly.data.local.Dao.MenuItemDao
import com.example.orderly.data.local.Dao.orderDao
import com.example.orderly.data.local.entity.MenuItemEntity
import com.example.orderly.data.local.entity.OrderEntity
import com.example.orderly.data.local.entity.OrderItemEntity

@Database(entities = [MenuItemEntity::class, OrderEntity::class, OrderItemEntity::class],
    //Tells Room this is the database class and lists every table that should exist
    //Every @Entity class you want in the database must be listed here
    version = 3,//The current version of the database schema. If you ever change
    //your entities(add a column, rename a table etc) you must increment this number and provide a migration
    exportSchema = false)//Tells Room not to export database schema to a JSON file.
//In production you would set this to true
abstract class AppDatabase : RoomDatabase(){
    /* abstract because Room generates the actual implementation behind the scenes.
    * It extends roomDatabase which gives it all the database functionality */
    abstract fun menuItemDao():MenuItemDao
    abstract fun orderDao(): orderDao
    /* Abstract functions that return your DAOs. Again bstract because Room generates
    * the actual implementation. this is how you access you DAOs throughout the app */

    companion object {
        /* Everything inside here belongs to the class itself rather than na instance of it.
        * Similar to static in java */
        @Volatile
        private var INSTANCE: AppDatabase? = null
        /* @Volatile means any change to INSTANCE is immediately visible to all threads which
        * prevents one thread reading a stale value value while another is writing to it. Starts as
        * null because no database exists yet */
        fun getDatabase(context:Context) : AppDatabase{
            /* The function you call anywhere in your app to get the database instance.
            * Takes a Context because Room needs it to create the database file on the device.
            * Think of context as a handle to you Android app's environment. It gives you access to
            * app-level resources and system services. Room needs it specifically to know
            * Where to create the database file on the device's storage and which app the database belongs to.
            * context.applicationContext is used instead of just context to avoid memory leaks -
            * a regular context might be tied to an Activity which coudld be destroyed but applicationContext
            * lives as long as the app does so its always safe to use. Think of it like giving
            * Room a key to your app's storage so it knows where to save the database file */
            return INSTANCE ?: synchronized(this){
                /* This means that if INSTANCE is not null return it, otherwise run the
                * synchronized block. This is the core of the singleton pattern - if the database
                * already exists return it. "synchronized(this)" - Ensures only one thread can execute this block at a time.
                * Prevents two threads from both finding INSTANCE as null and both trying to create the database simultaneously*/
                val instance = Room.databaseBuilder(context.applicationContext,
                    AppDatabase::class.java,
                    "orderly_database").fallbackToDestructiveMigration().build()
                /* fallbackToDestructiveMigration wipes and recreates the database with the new schemas when the version changes -
                * so you'll lose your cached menu items and need to uninstall or clear data again. */

                /* This actually creates the database. context.applicationContext avoid memory leaks,
                * AppDatabase::class.java tells Room which class to implement and
                * "orderly_database" is the name of the SQLite file created on the device.
                * "AppDatabase::class.java" - ou're passing the AppDatabase class itself as a parameter, not an instance of it.
                * AppDatabase::class -> Kotlin's way of referencing a class
                * .java -> converts it to Java class because Room was built with Java in mind and
                * expects a Java class reference. Room needs this so it knows which class to
                * generate the implementation for. It reads AppDatabase::class.java and says "Ok,
                * I need to generate a working implementation of this abstract class, creating all the DAO
                * implementations and database connection code". Think of it like handing Room
                * a blue print of your database so it knows what to build */
                INSTANCE = instance
                instance
                /* Saves the newly created instance to INSTANCE so its reused next time, then returns it.
                * instance on its own is Kotlin's way of returning the last expression in a block */
            }
            /* In short this whole thing is a singleton database builder.
            * It's saying: "Create the database once and only once. If it already exists
            * return the existing one, if it doesnt exist create it for the first time.Always return the same single
            * instance no matter how many times  or from where in the app you ask for it" */
        }
    }
}