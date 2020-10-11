package ru.relabs.kurjercontroller.presentation.fragmentsOld.filters

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.utils.CancelableScope
import ru.relabs.kurjercontroller.data.database.entities.FilterEntity
import ru.relabs.kurjercontroller.domain.models.TaskFilter
import ru.relabs.kurjercontroller.domain.models.TaskFilters
import ru.relabs.kurjercontroller.data.models.FiltersRequest
import ru.relabs.kurjercontroller.utils.application

/**
 * Created by ProOrange on 18.03.2019.
 */

class FiltersPresenter(val fragment: FiltersFragment) {
    val bgScope = CancelableScope(Dispatchers.Default)
    var loadFilterCountJob: Job? = null

    fun loadFilteredTasksCount(filters: List<TaskFilter>, withPlanned: Boolean) {
        val token = application().user.getUserCredentials()?.token
        if (token == null) {
            fragment.setStartButtonCount(-1, 0, false)
            return
        }
        loadFilterCountJob?.cancel()
        loadFilterCountJob = bgScope.launch {
            val count = try {
                DeliveryServerAPI.api.countFilteredTasks(
                    token,
                    FiltersRequest.fromFiltersList(filters, withPlanned)
                ).await()

            } catch (e: Exception) {
                e.logError()
                null
            }
            withContext(Dispatchers.Main) {
                fragment?.setStartButtonCount(count?.closedCount ?: -1, count?.plannedCount ?: 0, withPlanned)
            }
        }
    }

    fun toTaskFiltersModel(filters: List<TaskFilter>): TaskFilters {

        return TaskFilters(
            filters.filter { it.type == FilterEntity.PUBLISHER_FILTER }.toMutableList(),
            filters.filter { it.type == FilterEntity.DISTRICT_FILTER }.toMutableList(),
            filters.filter { it.type == FilterEntity.REGION_FILTER }.toMutableList(),
            filters.filter { it.type == FilterEntity.BRIGADE_FILTER }.toMutableList(),
            filters.filter { it.type == FilterEntity.USER_FILTER }.toMutableList()
        )
    }
}