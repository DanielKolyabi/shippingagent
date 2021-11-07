package ru.relabs.kurjercontroller.domain.mappers

import ru.relabs.kurjercontroller.data.models.common.SettingsResponse
import ru.relabs.kurjercontroller.domain.models.AppSettings
import ru.relabs.kurjercontroller.domain.models.EntrancesMonitoring
import ru.relabs.kurjercontroller.domain.models.GpsRefreshTimes

object SettingsMapper {
    fun fromRaw(raw: SettingsResponse) = AppSettings(
        radius = RadiusMapper.fromRaw(raw.radius),
        gpsRefreshTimes = GpsRefreshTimes(
            close = raw.gpsRefreshTimes.close,
            photo = raw.gpsRefreshTimes.photo
        ),
        entrancesMonitoring = EntrancesMonitoringMapper.fromRaw(raw.entrancesMonitoring)
    )
}