package ru.relabs.kurjercontroller.data.models.tasks

import com.google.gson.annotations.SerializedName
import ru.relabs.kurjercontroller.data.models.auth.AddressResponse
import java.util.*

data class TaskItemResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("task_id") val taskId: Int,
    @SerializedName("default_report_type") val defaultReportType: Int,
    @SerializedName("required") val required: Boolean,
    @SerializedName("order_name") val publisherName: String,
    @SerializedName("close_time") val closeTime: Date?,
    @SerializedName("address") val address: AddressResponse,
    @SerializedName("entrances") val entrances: List<TaskItemEntranceResponse>,
    @SerializedName("note") val notes: List<String>,
    @SerializedName("deliveryman_id") val deliverymanId: Int,
    @SerializedName("wrong_method") val wrongMethod: Boolean,
    @SerializedName("button_name") val buttonName: String,
    @SerializedName("required_apartaments") val requiredApartments: String,
    @SerializedName("order_id") val orderId: Int
)
