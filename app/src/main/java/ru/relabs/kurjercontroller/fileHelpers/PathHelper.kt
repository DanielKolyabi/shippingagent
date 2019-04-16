package ru.relabs.kurjercontroller.fileHelpers

import android.os.Environment
import ru.relabs.kurjercontroller.models.EntranceModel
import ru.relabs.kurjercontroller.models.EntrancePhotoModel
import ru.relabs.kurjercontroller.models.TaskItemModel
import java.io.File
import java.util.*

/**
 * Created by ProOrange on 03.09.2018.
 */

object PathHelper {
    val dataPath = Environment.getExternalStorageDirectory().path + File.separator + "deliverycontr" + File.separator
    val photoPath = dataPath + "photos" + File.separator

    fun getTaskItemFolderById(taskItemID: Int): File {
        val taskDir = File(photoPath + File.separator + taskItemID)
        if (!taskDir.exists()) taskDir.mkdirs()
        return taskDir
    }

    fun getEntrancePhotoFile(taskItem: TaskItemModel, entrance: EntranceModel, uuid: UUID): File {
        return getEntrancePhotoFileByID(taskItem.id, entrance.number, uuid.toString())
    }

    fun getEntranceFolder(taskItemID: Int, entranceNumber: Int): File {
        val entranceDir = File(getTaskItemFolderById(taskItemID), entranceNumber.toString())
        if (!entranceDir.exists()) entranceDir.mkdirs()
        return entranceDir
    }

    fun getEntrancePhotoFileByID(taskItemID: Int, entranceNumber: Int, uuid: String): File {
        return File(getEntranceFolder(taskItemID, entranceNumber), "$uuid.jpg")
    }

    fun getUpdateFile(): File {
        return File(dataPath, "update.apk")
    }
}