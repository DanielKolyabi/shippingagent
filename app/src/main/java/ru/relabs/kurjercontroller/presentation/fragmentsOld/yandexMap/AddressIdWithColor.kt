package ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by ProOrange on 06.06.2019.
 */
@Parcelize
data class AddressIdWithColor(
    val id: Int,
    val color: Int = Color.BLUE,
    val outlineColor: Int = color
) : Parcelable
