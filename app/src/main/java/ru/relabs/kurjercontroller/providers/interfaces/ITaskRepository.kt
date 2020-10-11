package ru.relabs.kurjercontroller.providers.interfaces

import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskItem

/**
 * Created by ProOrange on 20.03.2019.
 */
interface ITaskRepository {
    suspend fun getTasks(): List<Task>
    suspend fun getTaskItems(taskId: Int): List<TaskItem>
    suspend fun getTask(taskId: Int): Task?
    suspend fun getTaskItem(taskItemId: Int): TaskItem?

    suspend fun closeAllTasks()
}