package ru.relabs.kurjercontroller.domain.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ru.relabs.kurjercontroller.presentation.report.ReportApartmentButtonsMode

@Parcelize
data class ApartmentResultId(val id: Int): Parcelable

@Parcelize
data class ApartmentNumber(val number: Int): Parcelable

@Parcelize
data class ApartmentResult(
    val id: ApartmentResultId,
    val taskId: TaskId,
    val taskItemId: TaskItemId,
    val entranceNumber: EntranceNumber,
    val apartmentNumber: ApartmentNumber,
    val buttonGroup: ReportApartmentButtonsMode,
    val buttonState: Int,
    val description: String
): Parcelable{
    companion object {
        fun empty(task: TaskItem, entrance: Entrance, apartmentNumber: ApartmentNumber) = ApartmentResult(
            ApartmentResultId(0),
            task.taskId,
            task.id,
            entrance.number,
            apartmentNumber,
            ReportApartmentButtonsMode.Main,
            0,
            ""
        )
    }
}
