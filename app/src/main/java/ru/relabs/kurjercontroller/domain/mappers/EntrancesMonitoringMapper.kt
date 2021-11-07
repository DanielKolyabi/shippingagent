package ru.relabs.kurjercontroller.domain.mappers

import com.google.firebase.crashlytics.FirebaseCrashlytics
import ru.relabs.kurjercontroller.data.models.settings.EntrancesMonitoringSettingsResponse
import ru.relabs.kurjercontroller.domain.models.EntrancesMonitoring
import ru.relabs.kurjercontroller.domain.models.EntrancesMonitoringMode

object EntrancesMonitoringMapper {
    fun fromRaw(raw: EntrancesMonitoringSettingsResponse) = EntrancesMonitoring(
        isCounterEnabled = raw.isCounterEnabled,
        mode = when (raw.mode) {
            0 -> EntrancesMonitoringMode.DeliveryControl
            1 -> EntrancesMonitoringMode.HousesControl
            else -> {
                FirebaseCrashlytics.getInstance().recordException(RuntimeException("Unknown entrance monitoring mode"))
                EntrancesMonitoringMode.DeliveryControl
            }
        }
    )

}
