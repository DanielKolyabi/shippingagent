package ru.relabs.kurjercontroller.domain.mappers

import org.joda.time.DateTime
import ru.relabs.kurjercontroller.data.models.tasks.UserLocationsResponse
import ru.relabs.kurjercontroller.domain.models.UserLocation

object UserLocationMapper {
    fun fromRaw(requestUserPosition: UserLocationsResponse): List<UserLocation> = requestUserPosition.locations.map {
        UserLocation(
            deviceId = it.deviceId,
            lat = it.location.lat,
            long = it.location.long,
            time = DateTime(it.location.time),
            name = requestUserPosition.name
        )
    }

}
