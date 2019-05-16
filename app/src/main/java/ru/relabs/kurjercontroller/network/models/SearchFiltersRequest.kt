package ru.relabs.kurjercontroller.network.models

import com.google.gson.annotations.SerializedName

/**
 * Created by ProOrange on 16.05.2019.
 */
data class SearchFiltersRequest (
    @SerializedName("filter_type")
    val filterType: Int,
    @SerializedName("filter_value")
    val filterValue: String,
    @SerializedName("selected_filters")
    val filters: List<FilterResponseModel>
){
}