package ru.relabs.kurjercontroller.providers

import kotlinx.coroutines.*
import ru.relabs.kurjercontroller.domain.models.TaskFilter
import ru.relabs.kurjercontroller.domain.repositories.ControlRepository
import ru.relabs.kurjercontroller.providers.interfaces.IFilterSearch
import ru.relabs.kurjercontroller.utils.CancelableScope
import ru.relabs.kurjercontroller.utils.Either
import ru.relabs.kurjercontroller.utils.Left
import ru.relabs.kurjercontroller.utils.Right

/**
 * Created by ProOrange on 16.05.2019.
 */


class RemoteFilterSearch(
    private val scope: CoroutineScope,
    private val repository: ControlRepository
) : IFilterSearch {
    var searchJob: Job? = null
    override fun searchFilters(
        filterType: Int,
        filterValue: String,
        selectedFilters: List<TaskFilter>,
        withPlanned: Boolean
    ): Deferred<Either<Exception, List<TaskFilter>>> {
        val deferred = CompletableDeferred<Either<Exception, List<TaskFilter>>>()
        searchJob?.cancel()
        searchJob = scope.launch(Dispatchers.IO) {
            val result = repository.searchFilters(filterType, filterValue, selectedFilters.filter { it.isActive() }, withPlanned)
            when (result) {
                is Left -> deferred.complete(result)
                is Right -> {
                    val data = result.value
                        .distinctBy { it.id to it.type }
                        .filter { resultFilter ->
                            selectedFilters.none { it.type == resultFilter.type && it.id == resultFilter.id }
                        }
                        .sortedBy { it.name }
                    if (data.isEmpty()) {
                        deferred.complete(Left(RuntimeException("Nothing found after filtering")))
                    } else {
                        deferred.complete(Right(data))
                    }
                }
            }
        }
        return deferred
    }
}