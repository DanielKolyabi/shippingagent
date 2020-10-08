package ru.relabs.kurjercontroller.domain.providers

import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskId
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.domain.models.TaskItemModel

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


    fun getTaskItemPhotoFolderById(taskItemID: Int): File {
        val taskDir = File(photoDir, taskItemID.toString())
        if (!taskDir.exists()) taskDir.mkdirs()
        return taskDir
    }

    fun getTaskItemPhotoFile(taskItem: TaskItem, uuid: UUID): File {
        return getTaskItemPhotoFileByID(taskItem.id.id, uuid)
    }

    fun getTaskItemPhotoFile(taskItem: TaskItemModel, uuid: UUID): File {
        return getTaskItemPhotoFileByID(taskItem.id, uuid)
    }

    fun getTaskItemPhotoFileByID(taskItemID: Int, uuid: UUID): File {
        return File(getTaskItemPhotoFolderById(taskItemID), "$uuid.jpg")
    }

    fun getTaskRasterizeMapFile(task: Task): File {
        return getTaskRasterizeMapFileById(task.id)
    }

    fun getTaskRasterizeMapFileById(taskId: TaskId): File {
        return File(mapDir, "${taskId.id}.jpg")
    }

    fun getUpdateFile(): File {
        return File(updatesPath, "update.apk")
    }
}