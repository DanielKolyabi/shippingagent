package ru.relabs.kurjercontroller.presentation.fragments.yandexMap.models

import ru.relabs.kurjercontroller.domain.models.TaskModel

/**
 * Created by ProOrange on 06.06.2019.
 */

sealed class YandexMapModel {
    var selected: Boolean = false

    object MyPosition : YandexMapModel()
    object CommonLayer : YandexMapModel()
    object PredefinedAddressesLayer : YandexMapModel()
    data class TaskLayer(val task: TaskModel, var loading: Boolean = false) : YandexMapModel()
    data class LoadDeliverymans(var loading: Boolean = false): YandexMapModel()
}