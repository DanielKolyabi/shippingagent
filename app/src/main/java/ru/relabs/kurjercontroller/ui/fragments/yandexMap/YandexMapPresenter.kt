package ru.relabs.kurjercontroller.ui.fragments.yandexMap

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.CancelableScope
import ru.relabs.kurjercontroller.application

class YandexMapPresenter(val fragment: YandexMapFragment) {
    val bgScope = CancelableScope(Dispatchers.Default)

    fun loadAddresses() {
        bgScope.launch {

            fragment.addresses = fragment.addressIds.mapNotNull {
                val address = application().tasksRepository.getAddress(it.id) ?: return@mapNotNull null
                return@mapNotNull YandexMapFragment.AddressWithColor(address, it.colorId)
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
