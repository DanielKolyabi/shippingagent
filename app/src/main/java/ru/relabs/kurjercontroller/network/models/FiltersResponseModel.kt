package ru.relabs.kurjercontroller.network.models

import ru.relabs.kurjercontroller.database.entities.FilterEntity
import ru.relabs.kurjercontroller.models.FilterModel
import ru.relabs.kurjercontroller.models.TaskFiltersModel

data class FiltersResponseModel(
    val brigades: List<FilterResponseModel>,
    val districts: List<FilterResponseModel>,
    val publishers: List<FilterResponseModel>,
    val regions: List<FilterResponseModel>,
    val users: List<FilterResponseModel>
) {
    fun toModel(): TaskFiltersModel {
        return TaskFiltersModel(
            brigades = brigades.map { it.toModel() }.toMutableList(),
            districts = districts.map { it.toModel() }.toMutableList(),
            publishers = publishers.map { it.toModel() }.toMutableList(),
            regions = regions.map { it.toModel() }.toMutableList(),
            users = users.map { it.toModel() }.toMutableList()
        )
    }

}

data class FilterResponseModel(
    val id: Int,
    val name: String,
    val fixed: Boolean,
    val type: Int
) {
    fun toModel(): FilterModel {
        return FilterModel(
            id = id,
            name = name,
            fixed = fixed,
            active = fixed,
            type = type
        )
    }
}
