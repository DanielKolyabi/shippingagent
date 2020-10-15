package ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.models

import ru.relabs.kurjercontroller.domain.models.Task

/**
 * Created by ProOrange on 06.06.2019.
 */

sealed class YandexMapModel {
    var selected: Boolean = false

    object MyPosition : YandexMapModel()
    object CommonLayer : YandexMapModel()
    object PredefinedAddressesLayer : YandexMapModel()
    data class TaskLayer(val task: Task, var loading: Boolean = false) : YandexMapModel()
    data class LoadDeliverymans(var loading: Boolean = false): YandexMapModel()
}