package ru.relabs.kurjercontroller.ui.fragments.yandexMap.base

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.CancelableScope
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.models.TaskModel
import ru.relabs.kurjercontroller.ui.fragments.yandexMap.AddressWithColor
import ru.relabs.kurjercontroller.ui.fragments.yandexMap.AddressYandexMapFragment

abstract class BaseYandexMapPresenter(open val fragment: BaseYandexMapFragment) {
    val bgScope = CancelableScope(Dispatchers.Default)

    fun onMyPositionClicked() {
        fragment.moveCameraToUser()
    }

    abstract fun onTaskLayerSelected(taskModel: TaskModel)
    abstract fun onCommonLayerSelected()
}
