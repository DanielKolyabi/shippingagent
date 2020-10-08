package ru.relabs.kurjercontroller.data.database.models

import com.google.gson.annotations.SerializedName

/**
 * Created by ProOrange on 17.04.2019.
 */
data class ApartmentResult(
    val number: Int,
    val state: Int,
    @SerializedName("button_group")
    val buttonGroup: Int,
    val description: String
)