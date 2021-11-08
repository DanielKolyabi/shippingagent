package ru.relabs.kurjercontroller.domain.mappers.network

import ru.relabs.kurjercontroller.data.models.tasks.FilteredTaskDataResponse
import ru.relabs.kurjercontroller.domain.models.EntrancesMonitoringMode
import ru.relabs.kurjercontroller.domain.models.FilteredTasksData

object FilteredTasksDataMapper {
    fun fromRaw(raw: FilteredTaskDataResponse) = FilteredTasksData(
        items = raw.items.map { TaskItemMapper.fromRaw(it, EntrancesMonitoringMode.DeliveryControl) },
        storages = raw.storages.map { StorageMapper.fromRaw(it) },
        publishers = raw.publishers.map { PublisherMapper.fromRaw(it) }
    )
}