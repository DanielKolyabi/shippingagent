package ru.relabs.kurjercontroller.network.models

import com.google.gson.annotations.SerializedName
import ru.relabs.kurjercontroller.models.EntranceModel

data class EntranceResponseModel(
    val number: Int,
    @SerializedName("euro_key")
    val euroKey: String,
    @SerializedName("key")
    val key: String,
    val code: String?,
    @SerializedName("start_appartaments")
    val startAppartaments: Int,
    @SerializedName("end_appartaments")
    val endAppartaments: Int,
    val floors: Int,
    @SerializedName("mailbox_type")
    val mailboxType: Int,
    @SerializedName("state")
    val state: Int,
    @SerializedName("lookout")
    val hasLookout: Boolean
) {
    fun toModel(): EntranceModel {
        return EntranceModel(
            number = number,
            state = state,
            startApartments = startAppartaments,
            mailboxType = mailboxType,
            floors = floors,
            endApartments = endAppartaments,
            code = code.orEmpty(),
            key = key,
            euroKey = euroKey,
            hasLookout = hasLookout
        )
    }

}
