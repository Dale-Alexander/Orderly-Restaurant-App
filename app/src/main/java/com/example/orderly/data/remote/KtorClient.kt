package com.example.orderly.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.engine.cio.CIO
import kotlinx.serialization.json.Json


object KtorClient{
    /* the above line means it is a singleton - only one instance of this
    * ever exists in your app. Think of it like declaring an instance of
    * a repository to cal */
    val client: HttpClient by lazy{
        /* Declares a property called api of type MealApiService.
        Technically Kotlin could infer it automatically but it is
        good practice as it makes your code more readable . "by lazy"
         means this code inside the {} only runs once, the first time
         api is accessed - not when the app starts. Saves memory and startup time*/
        HttpClient(CIO){
            /* Android is the engine - equivalent to OkHttp that Retrofit used under the hood
            * "HttpClient" is Ktor's networking engine - its the thing
            * that actually opens a connections and sends/receives data over the internet
            * The "Android" part is the engine - it tells Ktor which underlying technology to use.
            * Think of it like a car: HttpClient is the car and Android is the engine */
            install(ContentNegotiation){
                /* replaces GsonConverterFactory - tells Ktor how to handle JSON responses
                * ContentNegotation is a Ktor plugin that handles the conversation between my app
                * and the API about data format. It sets the request header: Accept:application/json
                * This tells the API "Please send me JSON". Without it the API might not know what format you want
                * json(...) tells ContentNegotiation specifically to use JSON as the format
                * and which JSON library to use = in this case kotlinx.serialization
                * Json{...} is the actual parser configuration */
                json(Json{
                    ignoreUnknownKeys = true
                    /* IF the API returns fields your data class doesnt have, just ignore them
                    * instead of crashing */
                })
            }
        }
    }
}