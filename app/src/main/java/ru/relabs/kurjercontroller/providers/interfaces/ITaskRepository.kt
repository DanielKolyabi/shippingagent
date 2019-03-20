package ru.relabs.kurjercontroller.providers.interfaces

import ru.relabs.kurjercontroller.models.TaskItemModel
import ru.relabs.kurjercontroller.models.TaskModel

/**
 * Created by ProOrange on 20.03.2019.
 */
interface ITaskRepository {
    suspend fun getTasks(): List<TaskModel>
    suspend fun getTaskItems(taskId: Int): List<TaskItemModel>
    suspend fun getTask(taskId: Int): TaskModel
    suspend fun getTaskItem(taskItemId: Int): TaskItemModel
}