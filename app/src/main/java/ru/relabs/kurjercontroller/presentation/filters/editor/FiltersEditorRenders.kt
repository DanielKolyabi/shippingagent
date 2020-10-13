package ru.relabs.kurjercontroller.presentation.filters.editor

import android.graphics.Color
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import ru.relabs.kurjercontroller.BuildConfig
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.models.FilterType
import ru.relabs.kurjercontroller.domain.models.TaskFilter
import ru.relabs.kurjercontroller.presentation.base.tea.renderT
import ru.relabs.kurjercontroller.presentation.сustomView.FilterTagLayout
import ru.relabs.kurjercontroller.utils.extensions.renderText

/**
 * Created by Daniil Kurchanov on 06.04.2020.
 */
object FiltersEditorRenders {
    fun renderFilterResults(adapter: ArrayAdapter<TaskFilter>, type: FilterType): FiltersEditorRender = renderT(
        { (type == it.activeSearchField) to (it.searchData[type]?.filters?.filter { it.type == type } ?: emptyList()) },
        { (searchActive, searchResults) ->
            if (searchActive) {
                adapter.clear()
                adapter.addAll(searchResults)
                adapter.notifyDataSetChanged()
            }
        }
    )

    fun renderFilters(tags: FilterTagLayout, type: FilterType): FiltersEditorRender = renderT(
        { it.filters.filter { it.type == type } },
        { filters ->
            tags.clear()
            filters
                .sortedByDescending { it.fixed }
                .forEach { tags.add(it) }
        }
    )

    fun renderFilterSearch(text: EditText, type: FilterType, watcher: TextWatcher): FiltersEditorRender = renderT(
        { it.searchData[type]?.query ?: "" },
        { text.renderText(it, watcher) }
    )

    fun renderPlannedCheck(plannedTasks: CheckBox): FiltersEditorRender = renderT(
        { it.isPlannedEnabled },
        { plannedTasks.isChecked = it }
    )

    fun renderStartButton(btn: TextView): FiltersEditorRender = renderT(
        { Triple(it.plannedCount, it.closedCount, it.isPlannedEnabled) },
        { (plannedCount, closedCount, plannedEnabled) ->

            if ((closedCount + plannedCount) > BuildConfig.MAX_ADDRESSES_IN_FILTERS || closedCount < 0) {
                btn.setTextColor(Color.RED)
                btn.isEnabled = false
            } else {
                btn.setTextColor(Color.BLACK)
                btn.isEnabled = true
            }
            if (closedCount < 0) {
                btn.text = "Ошибка"
            } else if (!plannedEnabled) {
                btn.text = btn.resources.getString(R.string.filter_apply_button, closedCount.toString())
            } else {
                btn.text = btn.resources.getString(R.string.filter_apply_button, "$closedCount - $plannedCount")
            }

        }
    )
}