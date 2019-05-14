package ru.relabs.kurjercontroller.network.models

import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.models.TaskModel
import java.util.*

/**
 * Created by ProOrange on 11.04.2019.
 */
data class TaskResponseModel(
    val id: Int,
    val initiator: String,
    val state: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("start_сontrol_date")
    val startControlDate: Date,
    @SerializedName("end_сontrol_date")
    val endControlDate: Date,
    val description: String,
    val iteration: Int,
    @SerializedName("first_examined_device_id")
    val firstExaminedDeviceId: String?,

    val items: List<TaskItemResponseModel>,
    val publishers: List<TaskPublisherResponseModel>,
    val storages: List<String>,
    val filters: FiltersResponseModel,
    val filtered: Boolean
) {
    fun toModel(): TaskModel {
        return TaskModel(
            id = id,
            state = state,
            startControlDate = DateTime(startControlDate),
            endControlDate = DateTime(endControlDate),
            description = description,
            initiator = initiator,
            userId = userId,
            storages = storages,
            taskItems = items.map { it.toModel() },
            publishers = publishers.map{it.toModel()},
            taskFilters = filters.toModel(),
            iteration = iteration,
            firstExaminedDeviceId = firstExaminedDeviceId,
            filtered = filtered
        )
    }
}