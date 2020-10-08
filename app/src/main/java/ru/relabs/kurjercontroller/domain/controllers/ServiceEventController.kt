package ru.relabs.kurjercontroller.domain.controllers

class ServiceEventController: BaseEventController<ServiceEvent>()

sealed class ServiceEvent{
    object Stop: ServiceEvent()
}