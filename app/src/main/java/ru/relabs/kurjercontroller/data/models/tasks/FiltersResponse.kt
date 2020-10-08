package ru.relabs.kurjercontroller.data.models.tasks

import com.google.gson.annotations.SerializedName

data class FiltersResponse(
    @SerializedName("brigades") val brigades: List<FilterResponse>,
    @SerializedName("districts") val districts: List<FilterResponse>,
    @SerializedName("publishers") val publishers: List<FilterResponse>,
    @SerializedName("regions") val regions: List<FilterResponse>,
    @SerializedName("users") val users: List<FilterResponse>
) {
//    fun toModel(): TaskFiltersModel {
//        return TaskFiltersModel(
//            brigades = brigades.map { it.toModel() }.toMutableList(),
//            districts = districts.map { it.toModel() }.toMutableList(),
//            publishers = publishers.map { it.toModel() }.toMutableList(),
//            regions = regions.map { it.toModel() }.toMutableList(),
//            users = users.map { it.toModel() }.toMutableList()
//        )
//    }
}

data class FilteredTasksCountResponse(
    @SerializedName("closed")    val closedCount: Int,
    @SerializedName("planned")    val plannedCount: Int
)

data class FilterResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("fixed") val fixed: Boolean,
    @SerializedName("type") val type: Int
) {
//    fun toModel(): FilterModel {
//        return FilterModel(
//            id = id,
//            name = name,
//            fixed = fixed,
//            active = fixed,
//            type = type
//        )
//    }
}
