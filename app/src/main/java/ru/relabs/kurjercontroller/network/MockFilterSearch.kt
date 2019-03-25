package ru.relabs.kurjercontroller.network

import kotlinx.coroutines.*
import ru.relabs.kurjercontroller.models.FilterModel
import ru.relabs.kurjercontroller.models.TaskFiltersModel

/**
 * Created by ProOrange on 22.03.2019.
 */
object MockFilterSearch: IFilterSearch {
    override fun searchFilter(filterName: String, filterValue: String, selectedFilters: List<FilterModel>): Deferred<List<FilterModel>> {
        val deferred = CompletableDeferred<List<FilterModel>>()
        GlobalScope.launch(Dispatchers.IO) {
            delay(1000)
            deferred.complete(listOf(FilterModel(1, filterValue+"_"+filterName, false)))
        }
        return deferred
    }
}