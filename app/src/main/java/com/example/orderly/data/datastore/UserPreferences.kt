package com.example.orderly.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context:Context){
    companion object{
        /* companion object: Holds the preference key - these are like "labels"
        * for each value you store in DataStore, similar to Room uses column names.
        * They're in a companion object because they're shared across all instances of
        * UserPrefrences and dont need an instance to access them.
        *
        * companion object:
In Kotlin, a companion object is a block inside a class that belongs
* to the class itself rather than any instance of it. Normally when you
* create an object like UserPreferences(context), each instance has its
* own copy of everything inside it. But things inside companion object are
*  shared — there's only ever one copy regardless of how many instances you
*  create. It's Kotlin's equivalent of static in Java. You access them
*  directly via the class name: UserPreferences.RESTAURANT_NAME without
* needing to create an instance first.
        *  */
        val RESTAURANT_NAME = stringPreferencesKey("restaurant_name")
        val OPENING_TIME = stringPreferencesKey("opening_time")
        val CLOSING_TIME = stringPreferencesKey("closing_time")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")

        /*stringPreferencesKey / booleanPreferencesKey:
        DataStore needs to know two things about every value it stores:
         what to call it and what type it is. These functions create a key
         that carries both pieces of information.
         stringPreferencesKey("restaurant_name") creates a key named
         "restaurant_name" that only works with String values. If you try
         to use it with a Boolean the compiler will reject it. Think of it
          like a labelled box that only accepts one specific type of item —
           a stringPreferencesKey box only accepts strings, a
           booleanPreferencesKey box only accepts booleans.*/
    }

    val restaurantName: Flow<String> = context.dataStore.data.map{it[RESTAURANT_NAME] ?: "Orderly"}
    val openingTime:Flow<String> = context.dataStore.data.map{it[OPENING_TIME]?:"09:00"}
    val closingTime:Flow<String> = context.dataStore.data.map{it[CLOSING_TIME]?:"22:00"}
    val notificationsEnabled:Flow<Boolean> = context.dataStore.data.map{it[NOTIFICATIONS_ENABLED]?:true}

    /* DataStore doesnt return values directly - it returns a Flow, which is a stream of data
    * that emits a new value whenever the stored value changes. So if the restaurant name
    * changes, anything collecting restaurantName automatically gets the new value without having to ask for it again.
    * The map transforms the raw PReferences object into the value you want and
    * ?:"orderly" provides a default if nothing has been saced yet*/

    /*
    * context.dataStore.data:
dataStore is the actual DataStore instance tied to your app — it's the
*  physical file on the device's storage where all your preferences are
* saved. .data exposes the entire contents of that file as a
* Flow<Preferences> — a live stream of a snapshot of everything stored.
* Every time anything in the file changes, .data emits a new Preferences
*  snapshot containing all the latest values.
    *  */


    /*
    * .map{ it[RESTAURANT_NAME] ?: "Orderly" }:
.map transforms each emission from the flow. Since .data emits
*  a Preferences object containing all stored values, .map lets you
*  extract just the one you care about. it[RESTAURANT_NAME] looks up the
*  value using the key — similar to looking up a value in a Map by
* its key. ?: "Orderly" is the Elvis operator — if no value has been
*  saved yet (returns null), use "Orderly" as the default instead.
    *  */


    suspend fun saveRestaurantName(name:String){
        context.dataStore.edit{it[RESTAURANT_NAME] = name}
    }

    /*
    * context.dataStore.edit{ it[RESTAURANT_NAME] = name }:
edit opens the DataStore for writing inside a transaction — meaning all
*  changes inside the block either all succeed or all fail together, never
*  partially. The it inside the lambda is a MutablePreferences object,
* which is like a writable version of the Preferences snapshot. Setting
* it[RESTAURANT_NAME] = name updates the value for that key, and when the
*  edit block finishes DataStore automatically writes the changes to disk.
    *  */
    suspend fun saveOpeningTime(time:String){
        context.dataStore.edit{it[OPENING_TIME] = time}
    }
    suspend fun saveClosingTime(time:String){
        context.dataStore.edit{it[CLOSING_TIME] = time}
    }
    suspend fun saveNotificationsEnabled(enabled:Boolean){
        context.dataStore.edit{it[NOTIFICATIONS_ENABLED] = enabled}
    }
}

