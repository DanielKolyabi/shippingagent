package ru.relabs.kurjercontroller.ui.fragments.yandexMap

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.CancelableScope
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.models.TaskModel
import ru.relabs.kurjercontroller.ui.fragments.yandexMap.base.BaseYandexMapPresenter

class AddressYandexMapPresenter(override val fragment: AddressYandexMapFragment): BaseYandexMapPresenter(fragment) {
    override fun onTaskLayerSelected(taskModel: TaskModel) {}

    override fun onCommonLayerSelected() {}

    fun loadAddresses() {
        bgScope.launch {

            fragment.addresses = fragment.addressIds.mapNotNull {
                val address = application().tasksRepository.getAddress(it.id) ?: return@mapNotNull null
                return@mapNotNull AddressWithColor(address, it.color)
            }.distinctBy {
                it.address.idnd
            }

            withContext(Dispatchers.Main) {
                fragment.showAddresses()
                fragment.makeFocus(fragment.addresses.map { it.address })
            }
        }
    }
}
