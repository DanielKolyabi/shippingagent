package ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap

import android.os.Bundle
import android.view.View
import ru.relabs.kurjercontroller.domain.models.TaskStorage
import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.base.BaseYandexMapFragment
import ru.relabs.kurjercontroller.presentation.yandexMap.models.AddressIdWithColor
import ru.relabs.kurjercontroller.presentation.yandexMap.models.AddressWithColor

class AddressYandexMapFragment : BaseYandexMapFragment() {

    override fun shouldSaveCameraPosition(): Boolean {
        return addresses.size > 1
    }

    var addressIds: List<AddressIdWithColor> = listOf()
    var addresses: List<AddressWithColor> = listOf()
    var deliverymanIds: List<Int> = listOf()
    var storages: List<TaskStorage> = listOf()
    override val presenter = AddressYandexMapPresenter(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            addressIds = it.getParcelableArrayList<AddressIdWithColor>("address_ids")?.toList() ?: listOf()
            if (addressIds.size < 2) {
                savedCameraPosition = null
            }
            deliverymanIds = it.getIntArray("deliveryman_ids")?.toList().orEmpty()
            storages = it.getParcelableArrayList<TaskStorage>("storages")?.toList() ?: listOf()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        presenter.loadAddresses()
    }

    fun showAddresses() {
        addresses.forEach(::showAddress)
        storages.forEach{
            showStorage(it.lat.toDouble(), it.long.toDouble())
        }
    }

    override fun onControlListPopulation() {}

    companion object {
        @JvmStatic
        fun newInstance(addresses: List<AddressWithColor>, deliverymanIds: List<Int>, storages: List<TaskStorage>) =
            AddressYandexMapFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(
                        "address_ids",
                        ArrayList(addresses.map {
                            AddressIdWithColor(
                                it.address.id.id,
                                it.color,
                                it.outlineColor
                            )
                        })
                    )
                    putParcelableArrayList(
                        "storages",
                        ArrayList(storages)
                    )
                    putIntArray("deliveryman_ids", deliverymanIds.toIntArray())
                }
            }
    }
}

