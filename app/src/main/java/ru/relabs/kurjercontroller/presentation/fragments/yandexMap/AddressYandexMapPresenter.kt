package ru.relabs.kurjercontroller.presentation.fragments.yandexMap

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.domain.models.TaskModel
import ru.relabs.kurjercontroller.presentation.fragments.yandexMap.base.BaseYandexMapPresenter

class AddressYandexMapPresenter(override val fragment: AddressYandexMapFragment): BaseYandexMapPresenter(fragment) {
    override fun getDeliverymanIDs(): List<Int> {
        return fragment.deliverymanIds
    }

    override fun onPredefinedAddressesLayerSelected() {}

    override fun onTaskLayerSelected(taskModel: TaskModel) {}

    override fun onCommonLayerSelected() {}

    fun loadAddresses() {
        bgScope.launch {

            fragment.addresses = fragment.addressIds.mapNotNull {
                val address = application().tasksRepository.getAddress(it.id) ?: return@mapNotNull null
                return@mapNotNull AddressWithColor(address, it.color, it.outlineColor)
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
