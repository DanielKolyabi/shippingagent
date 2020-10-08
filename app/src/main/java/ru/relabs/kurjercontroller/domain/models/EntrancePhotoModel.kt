package ru.relabs.kurjercontroller.domain.models

import android.net.Uri
import ru.relabs.kurjercontroller.data.database.entities.EntrancePhotoEntity
import ru.relabs.kurjercontroller.fileHelpers.PathHelper
import java.util.*

/**
 * Created by ProOrange on 03.09.2018.
 */
data class EntrancePhotoModel(
    val id: Int,
    val uuid: String,
    val taskItem: TaskItem,
    val entrance: Entrance,
    val gps: GPSCoordinatesModel,
    val realPath: String?,
    val isEntrancePhoto: Boolean
) {
    fun toEntity(): EntrancePhotoEntity =
        EntrancePhotoEntity(
            0,
            uuid,
            gps,
            taskItem.taskId.id,
            taskItem.id.id,
            taskItem.address.idnd,
            entrance.number.number,
            realPath,
            isEntrancePhoto
        )

    val URI: Uri
        get() = Uri.fromFile(PathHelper.getEntrancePhotoFile(taskItem, entrance, UUID.fromString(uuid)))
}