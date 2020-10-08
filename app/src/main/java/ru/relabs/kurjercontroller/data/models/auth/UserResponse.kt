package ru.relabs.kurjercontroller.data.models.auth

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserLogin(val login: String): Parcelable

data class UserResponse(
    @SerializedName("login") val login: String
)
