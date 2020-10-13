package ru.relabs.kurjercontroller.data.models

import com.google.gson.annotations.SerializedName
import ru.relabs.kurjercontroller.data.models.tasks.FilterResponse
import ru.relabs.kurjercontroller.domain.models.TaskFilter

/**
 * Created by ProOrange on 16.05.2019.
 */
data class SearchFiltersRequest(
    @SerializedName("filter_type") val filterType: Int,
    @SerializedName("filter_value") val filterValue: String,
    @SerializedName("selected_filters") val filters: List<FilterResponse>,
    @SerializedName("with_planned") val withPlanned: Boolean
)

data class FiltersRequest(
    @SerializedName("selected_filters") val filters: List<FilterResponse>,
    @SerializedName("with_planned") val withPlanned: Boolean
)