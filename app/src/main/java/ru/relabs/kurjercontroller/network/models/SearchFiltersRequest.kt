package ru.relabs.kurjercontroller.network.models

import com.google.gson.annotations.SerializedName
import ru.relabs.kurjercontroller.models.FilterModel

/**
 * Created by ProOrange on 16.05.2019.
 */
data class SearchFiltersRequest(
    @SerializedName("filter_type")
    val filterType: Int,
    @SerializedName("filter_value")
    val filterValue: String,
    @SerializedName("selected_filters")
    val filters: List<FilterResponseModel>
) {
}

data class FiltersRequest(
    @SerializedName("selected_filters")
    val filters: List<FilterResponseModel>
) {
    companion object {
        fun fromFiltersList(filters: List<FilterModel>): FiltersRequest =
            FiltersRequest(filters.filter { it.isActive() }.map {
                FilterResponseModel(
                    it.id,
                    it.name,
                    it.fixed,
                    it.type
                )
            })
    }
}