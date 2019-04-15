package ru.relabs.kurjercontroller.network.models

import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.models.PublisherModel
import java.util.*

data class TaskPublisherResponseModel(
    val id: Int,
    val name: String,
    @SerializedName("task_id")
    val taskId: Int,
    @SerializedName("start_distribution_date")
    val startDistributionDate: Date,
    @SerializedName("end_distribution_date")
    val endDistributionDate: Date
) {
    fun toModel(): PublisherModel {
        return PublisherModel(
            id = id,
            taskId = taskId,
            name = name,
            startDistributionDate = DateTime(startDistributionDate),
            endDistributionDate = DateTime(endDistributionDate)
        )
    }

}
