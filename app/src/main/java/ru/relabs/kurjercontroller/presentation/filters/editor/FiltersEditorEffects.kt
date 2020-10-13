package ru.relabs.kurjercontroller.presentation.filters.editor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.domain.models.FilterType
import ru.relabs.kurjercontroller.utils.Left
import ru.relabs.kurjercontroller.utils.Right

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */
object FiltersEditorEffects {

    fun effectSearch(filter: String, type: FilterType, searchJobNumber: Int): FiltersEditorEffect = { c, s ->
        when (val r = c.controlRepository.searchFilters(type, filter, s.filters, s.isPlannedEnabled)) {
            is Left -> Unit//TODO
            is Right -> messages.send(FiltersEditorMessages.msgFiltersFound(searchJobNumber, type, r.value))
        }
    }

    fun effectRefreshCounts(): FiltersEditorEffect = { c, s ->
        when (val r = c.controlRepository.countFilteredTasks(s.filters, s.isPlannedEnabled)) {
            is Left -> Unit //TODO
            is Right -> messages.send(FiltersEditorMessages.msgCountUpdated(r.value))
        }
    }

    fun effectStart(): FiltersEditorEffect = { c, s ->
        if(s.taskId != null){
            withContext(Dispatchers.Main){
                c.performStart(s.taskId, s.filters, s.isPlannedEnabled)
            }
        }else{
            //TODO Error
        }
    }
}