package ru.relabs.kurjercontroller.models

import android.net.Uri
import ru.relabs.kurjercontroller.database.entities.EntrancePhotoEntity
import ru.relabs.kurjercontroller.fileHelpers.PathHelper
import java.util.*

/**
 * Created by ProOrange on 03.09.2018.
 */
data class EntrancePhotoModel(
    val id: Int,
    val uuid: String,
    val taskItem: TaskItemModel,
    val entranceModel: EntranceModel,
    val gps: GPSCoordinatesModel,
    val realPath: String?
) {
    fun toEntity(): EntrancePhotoEntity =
        EntrancePhotoEntity(
            0,
            uuid,
            gps,
            taskItem.taskId,
            taskItem.id,
            taskItem.address.idnd,
            entranceModel.number,
            realPath
        )

    val URI: Uri
        get() = Uri.fromFile(PathHelper.getEntrancePhotoFile(taskItem, entranceModel, UUID.fromString(uuid)))
}