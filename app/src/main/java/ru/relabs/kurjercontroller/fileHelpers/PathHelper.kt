package ru.relabs.kurjercontroller.fileHelpers

import android.os.Environment
import ru.relabs.kurjercontroller.domain.models.Entrance
import ru.relabs.kurjercontroller.domain.models.EntrancePhotoModel
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.utils.deleteIfEmpty
import java.io.File
import java.util.*

/**
 * Created by ProOrange on 03.09.2018.
 */

object PathHelper {
    val dataPath = Environment.getExternalStorageDirectory().path + File.separator + "deliverycontr" + File.separator
    val photoPath = dataPath + "photos" + File.separator

    @Deprecated("Use PathsProvider")
    fun getTaskItemFolderById(taskItemID: Int): File {
        val taskDir = File(photoPath + File.separator + taskItemID)
        if (!taskDir.exists()) taskDir.mkdirs()
        return taskDir
    }

    @Deprecated("Use PathsProvider")
    fun getEntrancePhotoFile(taskItem: TaskItem, entrance: Entrance, uuid: UUID): File {
        return getEntrancePhotoFileByID(taskItem.id.id, entrance.number.number, uuid.toString())
    }

    @Deprecated("Use PathsProvider")
    fun getEntranceFolder(taskItemID: Int, entranceNumber: Int): File {
        val entranceDir = File(getTaskItemFolderById(taskItemID), entranceNumber.toString())
        if (!entranceDir.exists()) entranceDir.mkdirs()
        return entranceDir
    }

    @Deprecated("Use PathsProvider")
    fun getEntrancePhotoFileByID(taskItemID: Int, entranceNumber: Int, uuid: String): File {
        return File(getEntranceFolder(taskItemID, entranceNumber), "$uuid.jpg")
    }

    @Deprecated("Use PathsProvider")
    fun getUpdateFile(): File {
        return File(dataPath, "update.apk")
    }

    @Deprecated("Use Database Repository")
    fun deletePhoto(entrancePhoto: EntrancePhotoModel) {
        val file = File(entrancePhoto.realPath ?: entrancePhoto.URI.path)
        val entranceFolder = file.parentFile
        val taskItemFolder = entranceFolder.parentFile
        file.delete()
        entranceFolder.deleteIfEmpty()
        taskItemFolder.deleteIfEmpty()
    }
}