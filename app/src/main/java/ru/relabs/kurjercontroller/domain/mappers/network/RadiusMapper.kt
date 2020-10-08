package ru.relabs.kurjercontroller.domain.mappers.network

import ru.relabs.kurjercontroller.data.models.radius.RadiusResponse
import ru.relabs.kurjercontroller.domain.models.AllowedCloseRadius

object RadiusMapper {
    fun fromRaw(raw: RadiusResponse): AllowedCloseRadius = when (raw.locked) {
        true -> AllowedCloseRadius.Required(raw.radius)
        else -> AllowedCloseRadius.NotRequired(raw.radius)
    }
}