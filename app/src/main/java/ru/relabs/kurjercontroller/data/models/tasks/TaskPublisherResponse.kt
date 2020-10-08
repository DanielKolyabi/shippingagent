package ru.relabs.kurjercontroller.data.models.tasks

import com.google.gson.annotations.SerializedName
import java.util.*

data class TaskPublisherResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("task_id") val taskId: Int,
    @SerializedName("start_distribution_date") val startDistributionDate: Date,
    @SerializedName("end_distribution_date") val endDistributionDate: Date
) {
//    fun toModel(): PublisherModel {
//        return PublisherModel(
//            id = id,
//            taskId = taskId,
//            name = name,
//            startDistributionDate = DateTime(startDistributionDate),
//            endDistributionDate = DateTime(endDistributionDate)
//        )
//    }
}
