package ru.relabs.kurjercontroller.network.models

import com.google.gson.annotations.SerializedName
import ru.relabs.kurjercontroller.models.TaskItemModel

data class TaskItemResponseModel(
    val id: Int,
    @SerializedName("task_id")
    val taskId: Int,
    @SerializedName("default_report_type")
    val defaultReportType: Int,
    val required: Boolean,
    @SerializedName("order_name")
    val publisherName: String,


    val address: AddressResponseModel,
    val entrances: List<EntranceResponseModel>,

    @SerializedName("note")
    val notes: List<String>
) {
    fun toModel(): TaskItemModel {
        return TaskItemModel(
            id = id,
            taskId = taskId,
            publisherName = publisherName,
            required = required,
            defaultReportType = defaultReportType,
            notes = notes,
            entrances = entrances.map { it.toModel() },
            address = address.toModel()
        )
    }

}
