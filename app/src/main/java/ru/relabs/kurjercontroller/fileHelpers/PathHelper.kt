package ru.relabs.kurjer.files

import android.os.Environment
import java.io.File
import java.util.*

/**
 * Created by ProOrange on 03.09.2018.
 */

object PathHelper {
    val dataPath = Environment.getExternalStorageDirectory().path + File.separator + "deliverycontr" + File.separator
    val photoPath = dataPath + "photos" + File.separator

    fun getTaskItemPhotoFolderById(taskItemID: Int): File {
        val taskDir = File(photoPath + File.separator + taskItemID)
        if (!taskDir.exists()) taskDir.mkdirs()
        return taskDir
    }

    fun getTaskItemPhotoFileByID(taskItemID: Int, uuid: UUID): File {
        return File(getTaskItemPhotoFolderById(taskItemID), uuid.toString() + ".jpg")
    }

    fun getUpdateFile(): File {
        return File(dataPath, "update.apk")
    }
}