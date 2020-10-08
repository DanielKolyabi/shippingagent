package ru.relabs.kurjercontroller.domain.mappers.network

import ru.relabs.kurjercontroller.data.models.tasks.FilterResponse
import ru.relabs.kurjercontroller.data.models.tasks.FiltersResponse
import ru.relabs.kurjercontroller.domain.models.TaskFilter
import ru.relabs.kurjercontroller.domain.models.TaskFilters

object FilterMapper {
    fun fromRaw(raw: FiltersResponse) = TaskFilters(
        publishers = raw.publishers.map { fromRaw(it) },
        districts = raw.districts.map { fromRaw(it) },
        regions = raw.regions.map { fromRaw(it) },
        brigades = raw.brigades.map { fromRaw(it) },
        users = raw.users.map { fromRaw(it) }
    )

    fun fromRaw(raw: FilterResponse) = TaskFilter(
        id = raw.id,
        name = raw.name,
        fixed = raw.fixed,
        active = raw.fixed,
        type = raw.type
    )
}