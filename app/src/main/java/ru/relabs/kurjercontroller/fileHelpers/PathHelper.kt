package ru.relabs.kurjer.files

import android.os.Environment
import java.io.File
import java.util.*

/**
 * Created by ProOrange on 03.09.2018.
 */

object PathHelper {
    val dataPath = Environment.getExternalStorageDirectory().path + File.separator + "deliveryman" + File.separator
    val photoPath = dataPath + "photos" + File.separator
    val mapPath = dataPath + "maps" + File.separator

    init {
        val mapDir = File(mapPath + File.separator)
        if (!mapDir.exists()) mapDir.mkdirs()
    }

    fun getTaskItemPhotoFolderById(taskItemID: Int): File {
        val taskDir = File(photoPath + File.separator + taskItemID)
        if (!taskDir.exists()) taskDir.mkdirs()
        return taskDir
    }

    fun getTaskItemPhotoFileByID(taskItemID: Int, uuid: UUID): File {
        return File(getTaskItemPhotoFolderById(taskItemID), uuid.toString() + ".jpg")
    }

    fun getTaskRasterizeMapFileById(taskId: Int): File {
        val mapDir = File(mapPath)
        if (!mapDir.exists()) mapDir.mkdirs()
        return File(mapDir, taskId.toString() + ".jpg")
    }

    fun getUpdateFile(): File {
        return File(dataPath, "update.apk")
    }
}