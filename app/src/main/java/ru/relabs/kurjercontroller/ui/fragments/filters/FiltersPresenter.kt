package ru.relabs.kurjercontroller.ui.fragments.filters

import kotlinx.coroutines.Dispatchers
import ru.relabs.kurjercontroller.CancelableScope
import ru.relabs.kurjercontroller.database.entities.FilterEntity
import ru.relabs.kurjercontroller.models.FilterModel
import ru.relabs.kurjercontroller.models.TaskFiltersModel

/**
 * Created by ProOrange on 18.03.2019.
 */

class FiltersPresenter(val fragment: FiltersFragment){
    val bgScope = CancelableScope(Dispatchers.Default)

    fun toTaskFiltersModel(filters: List<FilterModel>): TaskFiltersModel{

        return TaskFiltersModel(
            filters.filter { it.type == FilterEntity.PUBLISHER_FILTER }.toMutableList(),
            filters.filter { it.type == FilterEntity.DISTRICT_FILTER }.toMutableList(),
            filters.filter { it.type == FilterEntity.REGION_FILTER }.toMutableList(),
            filters.filter { it.type == FilterEntity.BRIGADE_FILTER }.toMutableList(),
            filters.filter { it.type == FilterEntity.USER_FILTER }.toMutableList()
        )
    }
}