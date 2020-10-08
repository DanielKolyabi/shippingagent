package ru.relabs.kurjercontroller.domain.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.data.database.entities.TaskPublisherEntity
import java.util.*

@Parcelize
data class PublisherId(val id: Int): Parcelable

@Parcelize
data class TaskPublisher(
    val id: PublisherId,
    val name: String,
    val taskId: TaskId,
    val startDistributionDate: Date,
    val endDistributionDate: Date
) : Parcelable {
    fun toEntity(): TaskPublisherEntity =
        TaskPublisherEntity(
            id = 0,
            name = name,
            endDistributionDate = DateTime(endDistributionDate),
            startDistributionDate = DateTime(startDistributionDate),
            taskId = taskId.id,
            publisherId = id.id
        )
}
