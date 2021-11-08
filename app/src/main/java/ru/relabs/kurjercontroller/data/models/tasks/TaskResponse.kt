package ru.relabs.kurjercontroller.data.models.tasks

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Created by ProOrange on 05.09.2018.
 */

data class TaskResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("initiator") val initiator: String,
    @SerializedName("state") val state: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("start_сontrol_date") val startControlDate: Date,
    @SerializedName("end_сontrol_date") val endControlDate: Date,
    @SerializedName("description") val description: String,
    @SerializedName("iteration") val iteration: Int,
    @SerializedName("first_examined_device_id") val firstExaminedDeviceId: String?,
    @SerializedName("items") val items: List<TaskItemResponse>,
    @SerializedName("publishers") val publishers: List<TaskPublisherResponse>,
    @SerializedName("storages") val storages: List<StorageResponse>,
    @SerializedName("filters") val filters: FiltersResponse,
    @SerializedName("filtered") val filtered: Boolean,
    @SerializedName("entrance_monitoring_mode") val entranceMonitoringMode: Int
)