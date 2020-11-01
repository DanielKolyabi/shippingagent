package ru.relabs.kurjercontroller.domain.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ru.relabs.kurjercontroller.data.database.entities.FilterEntity
import ru.relabs.kurjercontroller.data.models.tasks.FilterResponse
import ru.relabs.kurjercontroller.domain.mappers.FilterTypeMapper
import ru.relabs.kurjercontroller.domain.mappers.MappingException

/**
 * Created by ProOrange on 21.03.2019.
 */
enum class FilterType {
    Publisher, District, Region, Brigade, User
}

fun FilterType.toInt() = when (this) {
    FilterType.Publisher -> FilterEntity.PUBLISHER_FILTER
    FilterType.District -> FilterEntity.DISTRICT_FILTER
    FilterType.Region -> FilterEntity.REGION_FILTER
    FilterType.Brigade -> FilterEntity.BRIGADE_FILTER
    FilterType.User -> FilterEntity.USER_FILTER
}

fun Int.toFilterType() = when (this) {
    FilterEntity.PUBLISHER_FILTER -> FilterType.Publisher
    FilterEntity.DISTRICT_FILTER -> FilterType.District
    FilterEntity.REGION_FILTER -> FilterType.Region
    FilterEntity.BRIGADE_FILTER -> FilterType.Brigade
    FilterEntity.USER_FILTER -> FilterType.User
    else -> throw MappingException("filterType", this)
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