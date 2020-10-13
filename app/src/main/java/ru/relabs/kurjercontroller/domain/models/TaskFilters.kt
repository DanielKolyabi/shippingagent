package ru.relabs.kurjercontroller.domain.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ru.relabs.kurjercontroller.data.models.tasks.FilterResponse
import ru.relabs.kurjercontroller.domain.mappers.FilterTypeMapper

/**
 * Created by ProOrange on 21.03.2019.
 */
enum class FilterType{
    Publisher, District, Region, Brigade, User
}

@Parcelize
data class TaskFilters(
    val publishers: List<TaskFilter>,
    val districts: List<TaskFilter>,
    val regions: List<TaskFilter>,
    val brigades: List<TaskFilter>,
    val users: List<TaskFilter>
) : Parcelable {
    val all: List<TaskFilter>
        get() = publishers.asSequence()
            .plus(districts)
            .plus(regions)
            .plus(brigades)
            .plus(users)
            .toList()


    companion object {
        fun blank(): TaskFilters {
            return TaskFilters(
                listOf(),
                listOf(),
                listOf(),
                listOf(),
                listOf()
            )
        }
    }
}

@Parcelize
data class TaskFilter(
    val id: Int,
    val name: String,
    val fixed: Boolean,
    var active: Boolean,
    val type: FilterType
) : Parcelable {

    fun isActive() = if (fixed) active else true

    fun toFilterResponseModel(): FilterResponse =
        FilterResponse(id, name, fixed, FilterTypeMapper.toInt(type))
}