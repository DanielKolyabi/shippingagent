package ru.relabs.kurjercontroller.presentation.filters.editor

import ru.relabs.kurjercontroller.domain.models.*
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffect
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffects
import ru.relabs.kurjercontroller.presentation.base.tea.msgState

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */

object FiltersEditorMessages {
    fun msgInit(taskId: TaskId, filters: TaskFilters, withPlanned: Boolean, withNavBar: Boolean): FiltersEditorMessage =
        msgEffects(
            { it.copy(taskId = taskId, filters = filters.all, isPlannedEnabled = withPlanned, withNavBar = withNavBar) },
            { listOf(FiltersEditorEffects.effectRefreshCounts()) }
        )

    fun msgFilterRemoveClicked(filter: TaskFilter): FiltersEditorMessage = msgEffects(
        { s ->
            if (filter.fixed) {
                if (filter.active || s.filters.any { it.id != filter.id && it.type == filter.type && it.isActive() }) {
                    s.copy(filters = s.filters.map {
                        if (it.id == filter.id && it.type == filter.type) {
                            it.copy(active = filter.active)
                        } else {
                            it
                        }
                    })
                } else {
                    s
                }
            } else {
                s.copy(filters = s.filters.filter { it.id != filter.id || it.type != filter.type })
            }
        },
        { s -> listOf(FiltersEditorEffects.effectRefreshCounts()) }
    )

    fun msgSearchFilter(filter: String, type: FilterType): FiltersEditorMessage = msgEffects(
        {
            it.copy(searchData = it.searchData.mapValues { entry ->
                if (entry.key == type) {
                    entry.value.copy(query = filter, searchJobNumber = entry.value.searchJobNumber + 1)
                } else {
                    entry.value
                }
            })
        },
        {
            listOf(
                FiltersEditorEffects.effectSearch(filter, type, it.searchData[type]?.searchJobNumber ?: 0)
            )
        }
    )

    fun msgFiltersFound(searchJobNumber: Int, type: FilterType, value: List<TaskFilter>): FiltersEditorMessage = msgState {
        if (searchJobNumber == it.searchData[type]?.searchJobNumber) {
            it.copy(searchData = it.searchData.mapValues { entry ->
                if (entry.key == type) {
                    entry.value.copy(filters = value)
                } else {
                    entry.value
                }
            })
        } else {
            it
        }
    }

    fun msgPlannedChanged(checked: Boolean): FiltersEditorMessage = msgEffects(
        { it.copy(isPlannedEnabled = checked) },
        { listOf(FiltersEditorEffects.effectRefreshCounts()) }
    )

    fun msgRefreshCounts(): FiltersEditorMessage =
        msgEffect(FiltersEditorEffects.effectRefreshCounts())

    fun msgCountUpdated(count: FilteredTasksCount): FiltersEditorMessage =
        msgState { it.copy(plannedCount = count.planned, closedCount = count.closed) }

    fun msgStartClicked(): FiltersEditorMessage =
        msgEffect(FiltersEditorEffects.effectStart())

    fun msgSearchFilterSelected(item: TaskFilter, type: FilterType): FiltersEditorMessage = msgEffects(
        { s ->
            val updatedSearchState = s.copy(searchData = s.searchData.mapValues {
                if (it.key == type) {
                    it.value.copy(query = "", filters = emptyList(), searchJobNumber = it.value.searchJobNumber + 1)
                } else {
                    it.value
                }
            })

            if (updatedSearchState.filters.any { it.id == item.id && it.type == item.type }) {
                updatedSearchState
            } else {
                updatedSearchState.copy(filters = updatedSearchState.filters + listOf(item))
            }
        },
        {
            listOf(
                FiltersEditorEffects.effectRefreshCounts(),
                FiltersEditorEffects.effectSearch("", type, it.searchData[type]?.searchJobNumber ?: 0)
            )
        }
    )

    fun msgNavigateBack(): FiltersEditorMessage =
        msgEffect(FiltersEditorEffects.effectNavigateBack())

    fun msgSearchFieldClicked(type: FilterType): FiltersEditorMessage =
        msgState { it.copy(activeSearchField = type) }
}