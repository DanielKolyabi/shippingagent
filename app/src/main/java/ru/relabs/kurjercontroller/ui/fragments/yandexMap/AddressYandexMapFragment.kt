package ru.relabs.kurjercontroller.ui.fragments.yandexMap

import android.os.Bundle
import android.view.View
import ru.relabs.kurjercontroller.ui.fragments.yandexMap.base.BaseYandexMapFragment

class AddressYandexMapFragment : BaseYandexMapFragment() {

    override fun shouldSaveCameraPosition(): Boolean {
        return addresses.size > 1
    }

    var addressIds: List<AddressIdWithColor> = listOf()
    var addresses: List<AddressWithColor> = listOf()
    var deliverymanIds: List<Int> = listOf()
    override val presenter = AddressYandexMapPresenter(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            addressIds = it.getParcelableArrayList<AddressIdWithColor>("address_ids")?.toList() ?: listOf()
            if (addressIds.size < 2) {
                savedCameraPosition = null
            }
            deliverymanIds = it.getIntArray("deliveryman_ids").toList()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        presenter.loadAddresses()
    }

    fun showAddresses() {
        addresses.forEach(::showAddress)
    }

    override fun onControlListPopulation() {}

    companion object {
        @JvmStatic
        fun newInstance(addresses: List<AddressWithColor>, deliverymanIds: List<Int>) =
            AddressYandexMapFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(
                        "address_ids",
                        ArrayList(addresses.map { AddressIdWithColor(it.address.id, it.color) })
                    )
                    putIntArray("deliveryman_ids", deliverymanIds.toIntArray())
                }
            }
    }
}

