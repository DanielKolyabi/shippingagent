package ru.relabs.kurjercontroller.domain.mappers

import com.google.firebase.crashlytics.FirebaseCrashlytics
import ru.relabs.kurjercontroller.data.database.entities.FilterEntity
import ru.relabs.kurjercontroller.domain.models.FilterType

object FilterTypeMapper {
    fun fromInt(int: Int): FilterType = when (int) {
        FilterEntity.USER_FILTER -> FilterType.User
        FilterEntity.BRIGADE_FILTER -> FilterType.Brigade
        FilterEntity.DISTRICT_FILTER -> FilterType.District
        FilterEntity.PUBLISHER_FILTER -> FilterType.Publisher
        FilterEntity.REGION_FILTER -> FilterType.Region
        else -> {
            FirebaseCrashlytics.getInstance().log("Unknown filter int type ${int}")
            FilterType.User
        }
    }

    fun toInt(filter: FilterType): Int = when (filter) {
        FilterType.Publisher -> FilterEntity.PUBLISHER_FILTER
        FilterType.District -> FilterEntity.DISTRICT_FILTER
        FilterType.Region -> FilterEntity.REGION_FILTER
        FilterType.Brigade -> FilterEntity.BRIGADE_FILTER
        FilterType.User -> FilterEntity.USER_FILTER
    }
}