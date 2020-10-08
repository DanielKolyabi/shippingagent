package ru.relabs.kurjercontroller.domain.mappers.network

import ru.relabs.kurjercontroller.data.models.pause.PauseTimeResponse
import ru.relabs.kurjercontroller.data.models.pause.PauseTimesResponse
import ru.relabs.kurjercontroller.domain.models.PauseDurations
import ru.relabs.kurjercontroller.domain.models.PauseTimeInterval
import ru.relabs.kurjercontroller.domain.models.PauseTimes

object PauseMapper {
    fun fromRaw(raw: PauseTimesResponse): PauseDurations = PauseDurations(
        loading = raw.loading,
        lunch = raw.lunch
    )

    fun fromRaw(raw: PauseTimeResponse): PauseTimes = PauseTimes(
        loading = PauseTimeInterval(
            start = raw.start.loading,
            end = raw.end.loading
        ),
        lunch = PauseTimeInterval(
            start = raw.start.lunch,
            end = raw.end.lunch
        )
    )
}