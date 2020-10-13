package ru.relabs.kurjercontroller.domain.mappers.network

import ru.relabs.kurjercontroller.data.models.tasks.FilteredTasksCountResponse
import ru.relabs.kurjercontroller.domain.models.FilteredTasksCount

object FilteredTasksCountMapper {
    fun fromRaw(raw: FilteredTasksCountResponse) = FilteredTasksCount(
        planned = raw.plannedCount,
        closed = raw.closedCount
    )
}