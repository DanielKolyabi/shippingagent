package ru.relabs.kurjercontroller.domain.models

data class EntrancesMonitoring(
    val isCounterEnabled: Boolean
)

enum class EntrancesMonitoringMode {
    DeliveryControl, HousesControl
}
