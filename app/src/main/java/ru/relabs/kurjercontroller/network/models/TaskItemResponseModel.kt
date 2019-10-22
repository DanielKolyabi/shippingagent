package ru.relabs.kurjercontroller.network.models

import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.models.TaskItemModel
import java.util.*

data class TaskItemResponseModel(
    val id: Int,
    @SerializedName("task_id")
    val taskId: Int,
    @SerializedName("default_report_type")
    val defaultReportType: Int,
    val required: Boolean,
    @SerializedName("order_name")
    val publisherName: String,
    @SerializedName("close_time")
    val closeTime: Date?,

    val address: AddressResponseModel,
    val entrances: List<EntranceResponseModel>,

    @SerializedName("note")
    val notes: List<String>,

    @SerializedName("deliveryman_id")
    val deliverymanId: Int,

    @SerializedName("wrong_method")
    val wrongMethod: Boolean
) {
    fun toModel(): TaskItemModel {
        return TaskItemModel(
            id = id,
            taskId = taskId,
            publisherName = publisherName,
            required = required,
            defaultReportType = defaultReportType,
            notes = notes,
            entrances = entrances.map { it.toModel() }.toMutableList(),
            address = address.toModel(),
            closeTime = closeTime?.let { DateTime(it) },
            deliverymanId = deliverymanId,
            isNew = false,
            wrongMethod = wrongMethod
        )
    }

}
