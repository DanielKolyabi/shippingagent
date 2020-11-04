package ru.relabs.kurjercontroller.data.models.tasks

import com.google.gson.annotations.SerializedName
import ru.relabs.kurjercontroller.domain.models.ApartmentNumber

data class ApartmentResultRequest(
    val number: Int,
    val state: Int,
    @SerializedName("button_group")
    val buttonGroup: Int,
    val description: String
)
