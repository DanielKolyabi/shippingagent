package ru.relabs.kurjercontroller.presentation.filters.editor

import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.relabs.kurjercontroller.domain.models.FilterType
import ru.relabs.kurjercontroller.domain.models.TaskFilter
import ru.relabs.kurjercontroller.domain.models.TaskId
import ru.relabs.kurjercontroller.domain.repositories.ControlRepository
import ru.relabs.kurjercontroller.presentation.base.tea.*

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */

data class FilterEditorSearchData(
    val filters: List<TaskFilter>,
    val query: String,
    val searchJobNumber: Int = 0
)

data class FiltersEditorState(
    val taskId: TaskId? = null,

    val filters: List<TaskFilter> = emptyList(),
    val isPlannedEnabled: Boolean = false,
    val plannedCount: Int = 0,
    val closedCount: Int = 0,

    val activeSearchField: FilterType? = null,
    val searchData: Map<FilterType, FilterEditorSearchData> = mapOf(
        FilterType.Publisher to FilterEditorSearchData(emptyList(), "", 0),
        FilterType.Region to FilterEditorSearchData(emptyList(), "", 0),
        FilterType.District to FilterEditorSearchData(emptyList(), "", 0),
        FilterType.Brigade to FilterEditorSearchData(emptyList(), "", 0),
        FilterType.User to FilterEditorSearchData(emptyList(), "", 0)
    ),

    val withNavBar: Boolean = false
)

class FiltersEditorContext(val errorContext: ErrorContextImpl = ErrorContextImpl()) :
    ErrorContext by errorContext,
    RouterContext by RouterContextMainImpl(),
    KoinComponent {

    val controlRepository: ControlRepository by inject()

    var performStart: (TaskId, List<TaskFilter>, Boolean) -> Unit = { _, _, _ -> Unit }

}

typealias FiltersEditorMessage = ElmMessage<FiltersEditorContext, FiltersEditorState>
typealias FiltersEditorEffect = ElmEffect<FiltersEditorContext, FiltersEditorState>
typealias FiltersEditorRender = ElmRender<FiltersEditorState>