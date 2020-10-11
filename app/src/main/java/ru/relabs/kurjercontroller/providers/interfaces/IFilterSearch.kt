package ru.relabs.kurjercontroller.providers.interfaces

import kotlinx.coroutines.Deferred
import ru.relabs.kurjercontroller.domain.models.TaskFilter
import ru.relabs.kurjercontroller.utils.Either

/**
 * Created by ProOrange on 22.03.2019.
 */
interface IFilterSearch {
    fun searchFilters(
        filterType: Int,
        filterValue: String,
        selectedFilters: List<TaskFilter>,
        withPlanned: Boolean
    ): Deferred<Either<Exception, List<TaskFilter>>>
}