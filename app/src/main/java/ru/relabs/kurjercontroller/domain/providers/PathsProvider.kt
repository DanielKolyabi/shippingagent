package ru.relabs.kurjercontroller.domain.providers

import ru.relabs.kurjercontroller.domain.models.*

import java.io.File
import java.util.*

class PathsProvider(
    private val filesRootDir: File
){
    private val updatesPath = File(filesRootDir, "updates").apply {
        mkdirs()
    }
    private val photoDir = File(filesRootDir, "photos").apply {
        mkdirs()
    }
    private val mapDir = File(filesRootDir, "maps").apply {
        mkdirs()
    }

    fun getTaskItemFolderById(taskItemID: Int): File {
        val taskDir = File(photoDir, "$taskItemID")
        if (!taskDir.exists()) taskDir.mkdirs()
        return taskDir
    }

    fun getEntranceFolder(taskItemID: Int, entranceNumber: Int): File {
        val entranceDir = File(getTaskItemFolderById(taskItemID), entranceNumber.toString())
        if (!entranceDir.exists()) entranceDir.mkdirs()
        return entranceDir
    }

    fun getEntrancePhotoFileByID(taskItemID: TaskItemId, entranceNumber: EntranceNumber, uuid: String): File {
        return File(getEntranceFolder(taskItemID.id, entranceNumber.number), "$uuid.jpg")
    }

    fun getEntrancePhotoFile(taskItem: TaskItem, entrance: Entrance, uuid: UUID): File {
        return getEntrancePhotoFileByID(taskItem.id, entrance.number, uuid.toString())
    }

    fun getUpdateFile(): File {
        return File(updatesPath, "update.apk")
    }
}