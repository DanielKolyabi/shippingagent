package ru.relabs.kurjercontroller.data.models

import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.data.database.models.ApartmentResult
import ru.relabs.kurjercontroller.data.models.tasks.ApartmentResultRequest
import ru.relabs.kurjercontroller.domain.models.GPSCoordinatesModel
import java.util.*

/**
 * Created by ProOrange on 07.09.2018.
 */

data class TaskItemReportRequest(
    @SerializedName("task_id") val taskId: Int,
    @SerializedName("task_item_id") val taskItemId: Int, //iddot
    @SerializedName("idnd") val idnd: Int,
    @SerializedName("entrance_number") val entranceNumber: Int,
    @SerializedName("start_appartaments") val startAppartaments: Int,
    @SerializedName("end_appartaments") val endAppartaments: Int,
    @SerializedName("floors") val floors: Int,
    @SerializedName("description") val description: String,
    @SerializedName("code") val code: String,
    @SerializedName("key") val key: String,
    @SerializedName("euro_key") val euroKey: String,
    @SerializedName("is_delivery_wrong") val isDeliveryWrong: Boolean,
    @SerializedName("has_lookup_post") val hasLookupPost: Boolean,
    @SerializedName("token") val token: String,
    @SerializedName("apartment_results") val apartmentResult: List<ApartmentResultRequest>,
    @SerializedName("close_time") val closeTime: DateTime,
    @SerializedName("photos") val photos: Map<String, PhotoReportRequest>,
    @SerializedName("publisher_id") val publisherId: Int,
    @SerializedName("mailbox_type") val mailboxType: Int,
    @SerializedName("lat") val lat: Double,
    @SerializedName("long") val long: Double,
    @SerializedName("gps_time") val gpsTime: DateTime,
    @SerializedName("entrance_closed") val entranceClosed: Boolean
)

data class PhotoReportRequest(
    @SerializedName("hash") val hash: String,
    @SerializedName("gps") val gps: GPSCoordinatesModel
)