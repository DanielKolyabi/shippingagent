package ru.relabs.kurjercontroller.data.database.models

import com.google.gson.annotations.SerializedName
import ru.relabs.kurjercontroller.domain.models.ApartmentNumber

/**
 * Created by ProOrange on 17.04.2019.
 */
data class ApartmentResult(
    val number: ApartmentNumber,
    val state: Int,
    @SerializedName("button_group")
    val buttonGroup: Int,
    val description: String
)