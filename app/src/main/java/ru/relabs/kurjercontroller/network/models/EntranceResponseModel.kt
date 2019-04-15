package ru.relabs.kurjercontroller.network.models

import com.google.gson.annotations.SerializedName
import ru.relabs.kurjercontroller.models.EntranceModel

data class EntranceResponseModel(
    val number: Int,
    @SerializedName("available_euro_keys")
    val availableEuroKeys: List<String>,
    @SerializedName("available_keys")
    val availableKeys: List<String>,
    val code: String?,
    @SerializedName("start_appartaments")
    val startAppartaments: Int,
    @SerializedName("end_appartaments")
    val endAppartaments: Int,
    val floors: Int,
    @SerializedName("mailbox_type")
    val mailboxType: Int,
    @SerializedName("state")
    val state: Int
) {
    fun toModel(): EntranceModel {
        return EntranceModel(
            number = number,
            state = state,
            startAppartaments = startAppartaments,
            mailboxType = mailboxType,
            floors = floors,
            endAppartaments = endAppartaments,
            code = code.orEmpty(),
            availableKeys = availableKeys,
            availableEuroKeys = availableEuroKeys
        )
    }

}
