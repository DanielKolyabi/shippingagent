package ru.relabs.kurjercontroller.providers.interfaces

import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.domain.models.TaskModel

/**
 * Created by ProOrange on 20.03.2019.
 */
interface ITaskRepository {
    suspend fun getTasks(): List<TaskModel>
    suspend fun getTaskItems(taskId: Int): List<TaskItem>
    suspend fun getTask(taskId: Int): TaskModel?
    suspend fun getTaskItem(taskItemId: Int): TaskItem?

    suspend fun closeAllTasks()
}