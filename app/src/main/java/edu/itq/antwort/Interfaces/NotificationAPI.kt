package edu.itq.antwort.Interfaces

import edu.itq.antwort.Classes.Constants.Companion.CONTENT_TYPE
import edu.itq.antwort.Classes.Constants.Companion.SERVER_KEY
import edu.itq.antwort.Classes.PushNotification
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationAPI {

    @Headers("authorization:key=$SERVER_KEY", "content-type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(

        @Body notification: PushNotification

    ): Response<ResponseBody>

}//interface