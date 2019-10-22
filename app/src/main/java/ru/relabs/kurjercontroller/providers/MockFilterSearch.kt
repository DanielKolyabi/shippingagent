package ru.relabs.kurjercontroller.providers

import kotlinx.coroutines.*
import ru.relabs.kurjercontroller.database.entities.FilterEntity
import ru.relabs.kurjercontroller.models.FilterModel
import ru.relabs.kurjercontroller.providers.interfaces.FiltersResultOrError
import ru.relabs.kurjercontroller.providers.interfaces.IFilterSearch

/**
 * Created by ProOrange on 22.03.2019.
 */
object MockFilterSearch : IFilterSearch {
    fun getFilterTypeByName(name: String): Int {
        return when (name) {
            "publisher" -> FilterEntity.PUBLISHER_FILTER
            "region" -> FilterEntity.REGION_FILTER
            "district" -> FilterEntity.DISTRICT_FILTER
            "brigade" -> FilterEntity.BRIGADE_FILTER
            "user" -> FilterEntity.USER_FILTER
            else ->
                throw RuntimeException("Unknown filter name")
        }
    }

    override fun searchFilters(
        filterType: Int,
        filterValue: String,
        selectedFilters: List<FilterModel>,
        withPlanned: Boolean
    ): Deferred<FiltersResultOrError> {
        val deferred = CompletableDeferred<FiltersResultOrError>()
        GlobalScope.launch(Dispatchers.IO) {
            delay(1000)
            deferred.complete(
                FiltersResultOrError(
                    result = listOf(
                        FilterModel(
                            1,
                            filterValue + "_" + filterType,
                            false,
                            false,
                            filterType
                        )
                    )
                )
            )
        }
        return deferred
    }
}