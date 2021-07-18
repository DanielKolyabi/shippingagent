package ru.relabs.kurjercontroller.data.api

import okhttp3.MultipartBody
import retrofit2.http.*
import ru.relabs.kurjercontroller.data.models.FiltersRequest
import ru.relabs.kurjercontroller.data.models.SearchFiltersRequest
import ru.relabs.kurjercontroller.data.models.TaskItemReportRequest
import ru.relabs.kurjercontroller.data.models.UpdatesResponse
import ru.relabs.kurjercontroller.data.models.auth.AuthResponse
import ru.relabs.kurjercontroller.data.models.common.SettingsResponse
import ru.relabs.kurjercontroller.data.models.common.StatusResponse
import ru.relabs.kurjercontroller.data.models.tasks.*

interface ControlApi {
    @POST("api/v1/controller/auth")
    @FormUrlEncoded
    suspend fun login(
        @Field("login") login: String, @Field("password") password: String, @Field("device_id") deviceId: String,
        @Field("current_time") currentTime: String
    ): AuthResponse

    @POST("api/v1/controller/auth/token")
    @FormUrlEncoded
    suspend fun loginByToken(
        @Field("token") token: String,
        @Field("device_id") deviceId: String,
        @Field("current_time") currentTime: String
    ): AuthResponse

    @GET("api/v1/controller/tasks")
    suspend fun getTasks(@Query("token") token: String, @Query("current_time") currentTime: String): List<TaskResponse>

    //
//        @POST("api/v1/tasks/{taskItemId}/report")
//        @Multipart
//        suspend fun sendTaskReport(@Path("taskItemId") taskItemId: Int, @Query("token") token: String, @Part("data") data: TaskItemReportModel, @Part photos: List<MultipartBody.Part>): Deferred<StatusResponse>
//
//        @GET("api/v1/update")
//        suspend fun getUpdateInfo(): Deferred<UpdateInfoResponse>
//

    @POST("api/v1/device_imei")
    suspend fun sendDeviceImei(
        @Header("X-TOKEN") token: String,
        @Query("device_imei") imei: String
    )

    @POST("api/v1/push_token")
    suspend fun sendPushToken(@Query("token") token: String, @Query("push_token") pushToken: String)

    @GET("api/v1/controller/update")
    suspend fun getUpdateInfo(): UpdatesResponse

    @GET("api/v1/controller/settings")
    suspend fun getSettings(@Header("X-TOKEN") token: String): SettingsResponse

    @GET("api/v1/request_coords")
    suspend fun requestUserPosition(@Query("user_id") id: Int): UserLocationsResponse

    @POST("api/v1/coords")
    suspend fun sendGPS(
        @Query("token") token: String,
        @Query("lat") lat: Double,
        @Query("long") long: Double,
        @Query("time") time: String
    )

    @POST("api/v1/controller/tasks/{taskItemId}/report")
    @Multipart
    suspend fun sendTaskReport(
        @Path("taskItemId") taskItemId: Int,
        @Query("token") token: String,
        @Part("data") data: TaskItemReportRequest,
        @Part photos: List<MultipartBody.Part>
    )

    @GET("api/v1/controller/keys")
    suspend fun getAvailableEntranceKeys(@Query("token") token: String): List<String>

    @GET("api/v1/controller/euro_keys")
    suspend fun getAvailableEntranceEuroKeys(@Query("token") token: String): List<String>

    @POST("api/v1/controller/tasks/filters")
    suspend fun searchFilters(@Query("token") token: String, @Body req: SearchFiltersRequest): List<FilterResponse>

    @POST("api/v1/controller/tasks/filtered_count")
    suspend fun countFilteredTasks(@Query("token") token: String, @Body req: FiltersRequest): FilteredTasksCountResponse

    @POST("api/v1/controller/tasks/filtered")
    suspend fun getFilteredTaskItems(@Query("token") token: String, @Body req: FiltersRequest): FilteredTaskDataResponse

    @GET("api/v1/controller/has_online")
    suspend fun hasOnlineAccess(@Query("token") token: String): StatusResponse
}