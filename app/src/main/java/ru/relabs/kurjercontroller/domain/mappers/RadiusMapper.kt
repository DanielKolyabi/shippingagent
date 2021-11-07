package ru.relabs.kurjercontroller.domain.mappers

import ru.relabs.kurjercontroller.data.models.settings.RadiusResponse
import ru.relabs.kurjercontroller.domain.models.AllowedCloseRadius

object RadiusMapper {
    fun fromRaw(raw: RadiusResponse): AllowedCloseRadius = when (raw.closeAnyDistance) {
        true -> AllowedCloseRadius.NotRequired(raw.radius, raw.photoAnyDistance)
        else -> AllowedCloseRadius.Required(raw.radius, raw.photoAnyDistance)
    }
}