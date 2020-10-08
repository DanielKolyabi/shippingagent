package ru.relabs.kurjercontroller.data.api

import com.google.gson.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.joda.time.DateTime
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.relabs.kurjercontroller.BuildConfig
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit


class ApiProvider(deliveryUrl: String) {
    val httpClient: OkHttpClient
    val practisApi: ControlApi

    private val LOG_LEVEL = if(BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.BASIC

    private val timeoutInterceptor = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()

            if (request.url.toString().matches(Regex(".*/api/v1/controller/tasks/[0-9]*/report.*"))) {
                return chain.withConnectTimeout(5, TimeUnit.SECONDS)
                    .withReadTimeout(120, TimeUnit.SECONDS)
                    .withWriteTimeout(120, TimeUnit.SECONDS)
                    .proceed(request)
            } else if (request.url.toString().matches(Regex(".*/api/v1/request_coords.*"))) {
                return chain.withConnectTimeout(5, TimeUnit.SECONDS)
                    .withReadTimeout(25, TimeUnit.SECONDS)
                    .withWriteTimeout(5, TimeUnit.SECONDS)
                    .proceed(request)
            } else if (request.url.toString().matches(Regex(".*/api/v1/controller/tasks.*"))) {
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

    init {
        httpClient = buildClient()
        practisApi = buildRetrofit(httpClient, deliveryUrl).create()
    }

    private fun buildRetrofit(client: OkHttpClient, baseUrl: String): Retrofit {
        val builder = Retrofit.Builder()
        builder.client(client)
        builder.baseUrl(baseUrl)
        builder.addConverterFactory(GsonConverterFactory.create(buildGson()))
        return builder.build()
    }

    private fun buildGson(): Gson {
        return GsonBuilder()
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
    }

    private fun buildClient() = OkHttpClient.Builder()
        .apply {
            if(BuildConfig.DEBUG){
                addInterceptor(HttpLoggingInterceptor().apply { level = LOG_LEVEL })
            }
        }
        .addInterceptor(timeoutInterceptor)
        .build()
}