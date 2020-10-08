package ru.relabs.kurjercontroller.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.domain.mappers.database.DatabasePublisherMapper
import ru.relabs.kurjercontroller.domain.models.TaskPublisher
import ru.relabs.kurjercontroller.domain.models.PublisherId
import ru.relabs.kurjercontroller.domain.models.TaskId

/**
 * Created by ProOrange on 11.04.2019.
 */

@Entity(
    tableName = "task_publishers",
    indices = [Index(value = ["publisher_id", "task_id"], unique = true)]
)
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
)