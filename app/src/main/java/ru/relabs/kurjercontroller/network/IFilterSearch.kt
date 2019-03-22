package ru.relabs.kurjercontroller.network

import kotlinx.coroutines.Deferred
import ru.relabs.kurjercontroller.models.FilterModel

/**
 * Created by ProOrange on 22.03.2019.
 */
interface IFilterSearch {
    fun searchFilter(filterName: String, filterValue: String, selectedFilters: List<Int>): Deferred<List<FilterModel>>
}