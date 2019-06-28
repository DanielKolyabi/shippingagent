package ru.relabs.kurjercontroller.network.models

import ru.relabs.kurjercontroller.models.TaskModel

/**
 * Created by ProOrange on 22.05.2019.
 */

data class FilteredTaskDataResponseModel(

    val items: List<TaskItemResponseModel>,
    val publishers: List<TaskPublisherResponseModel>,
    val storages: List<StorageResponseModel>
) {
    fun toModel(filteredTask: TaskModel): TaskModel {
        return TaskModel(
            id = filteredTask.id,
            state = filteredTask.state,
            startControlDate = filteredTask.startControlDate,
            endControlDate = filteredTask.endControlDate,
            description = filteredTask.description,
            initiator = filteredTask.initiator,
            userId = filteredTask.userId,
            storages = storages.map { it.toModel() },
            taskItems = items.map { it.copy(taskId = filteredTask.id).toModel() }.toMutableList(),
            publishers = publishers.map { it.copy(taskId = filteredTask.id).toModel() },
            taskFilters = filteredTask.taskFilters,
            iteration = filteredTask.iteration,
            firstExaminedDeviceId = filteredTask.firstExaminedDeviceId,
            filtered = filteredTask.filtered,
            isOnline = false
        )
    }
}