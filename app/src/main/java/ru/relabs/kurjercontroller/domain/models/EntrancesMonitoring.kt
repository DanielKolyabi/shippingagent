package ru.relabs.kurjercontroller.domain.models

data class EntrancesMonitoring(
    val enabled: Boolean,
    val isCounterEnabled: Boolean,
    val mode: EntrancesMonitoringMode
)

enum class EntrancesMonitoringMode {
    DeliveryControl, HousesControl
}
