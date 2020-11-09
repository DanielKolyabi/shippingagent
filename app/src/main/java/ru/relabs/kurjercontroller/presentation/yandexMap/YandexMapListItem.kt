package ru.relabs.kurjercontroller.presentation.yandexMap

import ru.relabs.kurjercontroller.domain.models.Task

sealed class YandexMapListItem(val selected: Boolean) {
    class MyPosition(selected: Boolean) : YandexMapListItem(selected)
    class CommonLayer(selected: Boolean) : YandexMapListItem(selected)
    class PredefinedAddressesLayer(selected: Boolean) : YandexMapListItem(selected)
    class TaskLayer(val task: Task, val loading: Boolean, selected: Boolean) : YandexMapListItem(selected)
    class LoadDeliverymans(val loading: Boolean, selected: Boolean) : YandexMapListItem(selected)
}