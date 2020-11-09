package ru.relabs.kurjercontroller.presentation.yandexMap.models

import ru.relabs.kurjercontroller.domain.models.Address

interface IAddressClickedConsumer {
    fun onAddressClicked(address: Address)
}
