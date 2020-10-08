package ru.relabs.kurjercontroller.providers

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.relabs.kurjercontroller.utils.CancelableScope
import ru.relabs.kurjercontroller.domain.models.FilterModel
import ru.relabs.kurjercontroller.network.DeliveryServerAPI
import ru.relabs.kurjercontroller.data.models.SearchFiltersRequest
import ru.relabs.kurjercontroller.providers.interfaces.FiltersResultOrError
import ru.relabs.kurjercontroller.providers.interfaces.IFilterSearch

/**
 * Created by ProOrange on 16.05.2019.
 */


class RemoteFilterSearch(val scope: CancelableScope, val token: String) :
    IFilterSearch {
    var searchJob: Job? = null
    override fun searchFilters(
        filterType: Int,
        filterValue: String,
        selectedFilters: List<FilterModel>,
        withPlanned: Boolean
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
                            .filter { it.isActive() }
                            .map { it.toFilterResponseModel() },
                        withPlanned
                    )
                ).await().distinctBy { it.id to it.type }
            } catch (e: Exception) {
                deferred.complete(FiltersResultOrError(error = e))
                return@launch
            }

            val filteredResult = result.filter { resultFilter ->
                selectedFilters.firstOrNull {
                    it.type == resultFilter.type && it.id == resultFilter.id
                } == null
            }.sortedBy {
                it.name
            }

            if (filteredResult.isEmpty()) {
                deferred.complete(
                    FiltersResultOrError(
                        error = Exception(
                            "Nothing found after filtering"
                        )
                    )
                )
            }

            deferred.complete(FiltersResultOrError(result = filteredResult.map { it.toModel() }))
        }
        return deferred
    }
}