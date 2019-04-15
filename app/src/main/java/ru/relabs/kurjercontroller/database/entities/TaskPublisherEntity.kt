package ru.relabs.kurjercontroller.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.models.PublisherModel

/**
 * Created by ProOrange on 11.04.2019.
 */

@Entity(tableName = "task_publishers")
data class TaskPublisherEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    @ColumnInfo(name = "task_id")
    val taskId: Int,
    @ColumnInfo(name = "publisher_id")
    val publisherId: Int,
    @ColumnInfo(name = "start_distribution_date")
    val startDistributionDate: DateTime,
    @ColumnInfo(name = "end_distribution_date")
    val endDistributionDate: DateTime
) {
    fun toModel(): PublisherModel {
        return PublisherModel(
            id = publisherId,
            taskId = taskId,
            endDistributionDate = endDistributionDate,
            startDistributionDate = startDistributionDate,
            name = name
        )
    }
}