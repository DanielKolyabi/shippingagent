package ru.relabs.kurjercontroller.ui.fragments.yandexMap

import ru.relabs.kurjercontroller.models.TaskModel

/**
 * Created by ProOrange on 06.06.2019.
 */

sealed class YandexMapModel {
    var selected: Boolean = false

    object MyPosition : YandexMapModel()
    object CommonLayer : YandexMapModel()
    object PredefinedAddressesLayer : YandexMapModel()
    data class TaskLayer(val task: TaskModel, var loading: Boolean = false) : YandexMapModel()
}