package ru.relabs.kurjercontroller.data.models.tasks

import com.google.gson.annotations.SerializedName

data class TaskItemEntranceResponse(
    @SerializedName("number") val number: Int,
    @SerializedName("euro_key") val euroKey: String,
    @SerializedName("key") val key: String,
    @SerializedName("code") val code: String?,
    @SerializedName("start_appartaments") val startApartments: Int,
    @SerializedName("end_appartaments") val endApartments: Int,
    @SerializedName("floors") val floors: Int,
    @SerializedName("mailbox_type") val mailboxType: Int,
    @SerializedName("state") val state: Int,
    @SerializedName("lookout") val hasLookout: Boolean,
    @SerializedName("is_stacked") val isStacked: Boolean,
)