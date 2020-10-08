package ru.relabs.kurjercontroller.domain.mappers.database

import ru.relabs.kurjercontroller.data.database.entities.FilterEntity
import ru.relabs.kurjercontroller.domain.models.TaskFilter
import ru.relabs.kurjercontroller.domain.models.TaskFilters
import ru.relabs.kurjercontroller.domain.models.TaskId

object DatabaseFilterMapper {
    fun toEntity(model: TaskFilter, taskId: TaskId, type: Int) = FilterEntity(
        id = 0,
        taskId = taskId.id,
        type = type,
        filterId = model.id,
        name = model.name,
        fixed = model.fixed,
        active = model.active
    )

    fun fromEntity(entity: FilterEntity) = TaskFilter(
        id = entity.id,
        name = entity.name,
        fixed = entity.fixed,
        active = entity.active,
        type = entity.type
    )

    fun fromEntities(entities: List<FilterEntity>): TaskFilters {
        val filters = entities.groupBy { it.type }
            .mapValues { entry ->
                entry.value.map { TaskFilter(it.filterId, it.name, it.fixed, it.active, it.type) }
            }
        return TaskFilters(
            publishers = filters.getOrElse(FilterEntity.PUBLISHER_FILTER, { listOf() }),
            districts = filters.getOrElse(FilterEntity.DISTRICT_FILTER, { listOf() }),
            regions = filters.getOrElse(FilterEntity.REGION_FILTER, { listOf() }),
            brigades = filters.getOrElse(FilterEntity.BRIGADE_FILTER, { listOf() }),
            users = filters.getOrElse(FilterEntity.USER_FILTER, { listOf() })
        )
    }
}