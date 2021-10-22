package edu.itq.antwort.Classes

import edu.itq.antwort.Classes.Constants.Companion.BASE_URL
import edu.itq.antwort.Interfaces.NotificationAPI
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {

    companion object {

        private val retrofit by lazy {

            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        }//retrofit

        val api: NotificationAPI by lazy {

            retrofit.create(NotificationAPI::class.java)

        }//api

    }//companion object

}//class
