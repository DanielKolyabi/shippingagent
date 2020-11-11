package ru.relabs.kurjercontroller.presentation.yandexMap.models

import ru.relabs.kurjercontroller.domain.models.Address
import ru.relabs.kurjercontroller.domain.models.TaskItem

interface INewItemsAddedConsumer {
    fun onItemsAdded(taskItems: List<TaskItem>)
}
