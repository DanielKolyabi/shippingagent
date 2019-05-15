package ru.relabs.kurjercontroller.network

import kotlinx.coroutines.*
import ru.relabs.kurjercontroller.database.entities.FilterEntity
import ru.relabs.kurjercontroller.models.FilterModel
import ru.relabs.kurjercontroller.models.TaskFiltersModel
import java.lang.RuntimeException

/**
 * Created by ProOrange on 22.03.2019.
 */
object MockFilterSearch: IFilterSearch {
    fun getFilterTypeByName(name: String): Int{
        return when(name){
            "publisher" -> FilterEntity.PUBLISHER_FILTER
            "region" -> FilterEntity.REGION_FILTER
            "district" -> FilterEntity.DISTRICT_FILTER
            "brigade" -> FilterEntity.BRIGADE_FILTER
            "user" -> FilterEntity.USER_FILTER
            else ->
                throw RuntimeException("Unknown filter name")
        }
    }

    override fun searchFilter(filterName: String, filterValue: String, selectedFilters: List<FilterModel>): Deferred<List<FilterModel>> {
        val deferred = CompletableDeferred<List<FilterModel>>()
        GlobalScope.launch(Dispatchers.IO) {
            delay(1000)
            deferred.complete(listOf(FilterModel(1, filterValue+"_"+filterName, false, false, getFilterTypeByName(filterName))))
        }
        return deferred
    }
}