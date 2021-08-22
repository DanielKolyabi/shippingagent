package ru.relabs.kurjercontroller.presentation.report

import android.net.Uri
import ru.relabs.kurjercontroller.domain.mappers.MappingException
import ru.relabs.kurjercontroller.domain.models.ApartmentNumber
import ru.relabs.kurjercontroller.domain.models.EntrancePhoto

sealed class ReportPhotoItem {
    object Single : ReportPhotoItem()
    object Multiple : ReportPhotoItem()
    data class Photo(val photo: EntrancePhoto, val photoUri: Uri) : ReportPhotoItem()
}

enum class ReportApartmentButtonsMode {
    Main, Additional
}

fun ReportApartmentButtonsMode.toInt() = when(this){
    ReportApartmentButtonsMode.Main -> 1
    ReportApartmentButtonsMode.Additional -> 2
}

fun Int.toApartmentButtonsMode(): ReportApartmentButtonsMode = when(this){
    1 -> ReportApartmentButtonsMode.Main
    2 -> ReportApartmentButtonsMode.Additional
    else -> throw MappingException("buttonGroup", this)
}

const val ENTRANCE_NUMBER_TASK_ITEM = -1

sealed class ReportApartmentItem {
    data class Apartment(
        val number: ApartmentNumber,
        val buttonGroup: ReportApartmentButtonsMode,
        val state: Int,
        val colored: Boolean,
        val required: Boolean,
        val hasDescription: Boolean,
        val isAnyApartmentUndefined: Boolean
    ) : ReportApartmentItem()
    data class Lookout(var state: Int) : ReportApartmentItem()
    data class Entrance(var state: Int) : ReportApartmentItem()
    object Divider : ReportApartmentItem()
}