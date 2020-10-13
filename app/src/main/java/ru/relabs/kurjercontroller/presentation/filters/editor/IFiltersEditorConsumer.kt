package ru.relabs.kurjercontroller.presentation.filters.editor

import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskFilters
import ru.relabs.kurjercontroller.domain.models.TaskId

interface IFiltersEditorConsumer {
    fun onStartClicked(task: TaskId, filters: TaskFilters, withPlanned: Boolean)
}