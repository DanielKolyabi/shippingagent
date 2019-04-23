package ru.relabs.kurjercontroller.network

import com.google.gson.*
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.joda.time.DateTime
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ru.relabs.kurjercontroller.BuildConfig
import ru.relabs.kurjercontroller.network.models.AuthResponseModel
import ru.relabs.kurjercontroller.network.models.StatusResponse
import ru.relabs.kurjercontroller.network.models.TaskItemReportModel
import ru.relabs.kurjercontroller.network.models.TaskResponseModel
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit


/**
 * Created by ProOrange on 23.08.2018.
 */
object DeliveryServerAPI {

    private val interceptor = HttpLoggingInterceptor()

    init {
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
    }

    val timeoutInterceptor = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()

            if (request.url().toString().matches(Regex(".*/api/v1/controller/tasks/[0-9]*/report.*"))) {
                return chain.withConnectTimeout(5, TimeUnit.SECONDS)
                    .withReadTimeout(120, TimeUnit.SECONDS)
                    .withWriteTimeout(120, TimeUnit.SECONDS)
                    .proceed(request)
            } else if (request.url().toString().matches(Regex(".*/api/v1/controller/tasks.*"))) {
                return chain.withConnectTimeout(5, TimeUnit.SECONDS)
                    .withReadTimeout(7, TimeUnit.MINUTES)
                    .withWriteTimeout(10, TimeUnit.SECONDS)
                    .proceed(request)
            } else {
                return chain.withConnectTimeout(5, TimeUnit.SECONDS)
                    .withReadTimeout(15, TimeUnit.SECONDS)
                    .withWriteTimeout(10, TimeUnit.SECONDS)
                    .proceed(request)
            }
        }
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .addInterceptor(timeoutInterceptor)
        .build()

    var gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        .registerTypeAdapter(DateTime::class.java, object : JsonSerializer<DateTime> {
            override fun serialize(src: DateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
                return JsonPrimitive(src?.millis)
            }
        })
        .registerTypeAdapter(DateTime::class.java, object : JsonDeserializer<DateTime> {
            override fun deserialize(
                json: JsonElement?,
                typeOfT: Type?,
                context: JsonDeserializationContext?
            ): DateTime {
                return DateTime(json?.asLong)
            }
        })
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_URL)
        .client(client)
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    interface IDeliveryServerAPI {
        @POST("api/v1/controller/auth")
        @FormUrlEncoded
        fun login(
            @Field("login") login: String, @Field("password") password: String, @Field("device_id") deviceId: String, @Field(
                "current_time"
            ) currentTime: String
        ): Deferred<AuthResponseModel>

        @POST("api/v1/controller/auth/token")
        @FormUrlEncoded
        fun loginByToken(@Field("token") token: String, @Field("device_id") deviceId: String, @Field("current_time") currentTime: String): Deferred<AuthResponseModel>

        @GET("api/v1/controller/tasks")
        fun getTasks(@Query("token") token: String, @Query("current_time") currentTime: String): Deferred<List<TaskResponseModel>>

        //
//        @POST("api/v1/tasks/{id}/report")
//        @Multipart
//        fun sendTaskReport(@Path("id") taskItemId: Int, @Query("token") token: String, @Part("data") data: TaskItemReportModel, @Part photos: List<MultipartBody.Part>): Deferred<StatusResponse>
//
//        @GET("api/v1/update")
//        fun getUpdateInfo(): Deferred<UpdateInfoResponse>
//
        @POST("api/v1/push_token")
        fun sendPushToken(@Query("token") token: String, @Query("push_token") pushToken: String): Deferred<StatusResponse>

        @POST("api/v1/coords")
        fun sendGPS(@Query("token") token: String, @Query("lat") lat: Double, @Query("long") long: Double, @Query("time") time: String): Deferred<StatusResponse>

        @POST("api/v1/controller/tasks/{id}/report")
        @Multipart
        fun sendTaskReport(@Path("id") taskItemId: Int, @Query("token") token: String, @Part("data") data: TaskItemReportModel, @Part photos: List<MultipartBody.Part>): Deferred<StatusResponse>

        @GET("api/v1/controller/keys")
        fun getAvailableEntranceKeys(@Query("token") token: String): Deferred<List<String>>

        @GET("api/v1/controller/euro_keys")
        fun getAvailableEntranceEuroKeys(@Query("token") token: String): Deferred<List<String>>
    }

    val api = retrofit.create(IDeliveryServerAPI::class.java)
}