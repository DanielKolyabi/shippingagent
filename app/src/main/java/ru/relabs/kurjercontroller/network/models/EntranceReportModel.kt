package ru.relabs.kurjercontroller.network.models

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.database.models.ApartmentResult
import ru.relabs.kurjercontroller.models.GPSCoordinatesModel
import java.util.*

/**
 * Created by ProOrange on 07.09.2018.
 */

data class TaskItemReportModel(
    @SerializedName("task_item_id")
    val taskItemId: Int, //iddot
    val idnd: Int,
    @SerializedName("entrance_number")
    val entranceNumber: Int,
    @SerializedName("start_appartaments")
    val startAppartaments: Int,
    @SerializedName("end_appartaments")
    val endAppartaments: Int,
    val floors: Int,
    val description: String,
    val code: String,
    val key: String,
    @SerializedName("euro_key")
    val euroKey: String,
    @SerializedName("is_delivery_wrong")
    val isDeliveryWrong: Boolean,
    @SerializedName("has_lookup_post")
    val hasLookupPost: Boolean,
    val token: String,
    @SerializedName("apartment_results")
    val apartmentResult: List<ApartmentResult>,
    @SerializedName("close_time")
    val closeTime: DateTime,
    val photos: Map<String, PhotoReportModel>
)

data class PhotoReportModel(
    val hash: String,
    val gps: GPSCoordinatesModel
)