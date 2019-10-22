package ru.relabs.kurjercontroller.providers.interfaces

import kotlinx.coroutines.Deferred
import ru.relabs.kurjercontroller.models.FilterModel

/**
 * Created by ProOrange on 22.03.2019.
 */
interface IFilterSearch {
    fun searchFilters(filterType: Int, filterValue: String, selectedFilters: List<FilterModel>, withPlanned: Boolean): Deferred<FiltersResultOrError>
}

data class FiltersResultOrError(
    val result: List<FilterModel> = listOf(),
    val error: Exception? = null
)