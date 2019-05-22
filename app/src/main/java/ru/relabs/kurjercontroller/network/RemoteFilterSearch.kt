package ru.relabs.kurjercontroller.network

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.relabs.kurjercontroller.CancelableScope
import ru.relabs.kurjercontroller.models.FilterModel
import ru.relabs.kurjercontroller.network.models.FilterResponseModel
import ru.relabs.kurjercontroller.network.models.SearchFiltersRequest

/**
 * Created by ProOrange on 16.05.2019.
 */


class RemoteFilterSearch(val scope: CancelableScope, val token: String) : IFilterSearch {
    var searchJob: Job? = null
    override fun searchFilters(
        filterType: Int,
        filterValue: String,
        selectedFilters: List<FilterModel>
    ): Deferred<FiltersResultOrError> {
        val deferred = CompletableDeferred<FiltersResultOrError>()
        searchJob?.cancel()
        searchJob = scope.launch {
            val result = try {
                DeliveryServerAPI.api.searchFilters(
                    token, SearchFiltersRequest(
                        filterType,
                        filterValue,
                        selectedFilters
                            .filter {
                                if(it.fixed) it.active
                                else true
                            }
                            .map { FilterResponseModel(it.id, it.name, it.fixed, it.type) }
                    )
                ).await()
            } catch (e: Exception) {
                deferred.complete(FiltersResultOrError(error = e))
                return@launch
            }

            deferred.complete(FiltersResultOrError(result = result.map { it.toModel() }))
        }
        return deferred
    }
}