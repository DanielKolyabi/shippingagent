package ru.relabs.kurjercontroller.domain.models

import android.graphics.Color
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/**
 * Created by ProOrange on 19.03.2019.
 */

@Parcelize
data class TaskItemId(val id: Int) : Parcelable

@Parcelize
data class TaskItem(
    val id: TaskItemId, //iddot
    val taskId: TaskId,
    val publisherName: String,
    val defaultReportType: Int,
    val required: Boolean,
    val address: Address,
    val entrances: List<Entrance>,
    val notes: List<String>,
    var closeTime: Date? = null,
    val deliverymanId: Int,
    var isNew: Boolean,
    val wrongMethod: Boolean,
    val buttonName: String,
    val requiredApartments: String,
    val publisherId: PublisherId
) : Parcelable {
    fun getRequiredApartments(): List<RequiredApartment> {
        return requiredApartments.split(",").mapNotNull {
            if (it.contains("*")) {
                it.replace("*", "").toIntOrNull()?.let { RequiredApartment(it, true) }
            } else {
                it.toIntOrNull()?.let { RequiredApartment(it, false) }
            }
        }.sortedBy {
            it.number
        }
    }

    val isClosed: Boolean
        get() = entrances.none { it.state == EntranceState.CREATED }

    val placemarkColor: Int
        get() = if (isClosed) {
            Color.GRAY
        } else if (closeTime == null) {
            Color.BLUE
        } else {
            val currentTime = Date().time
            val diff = abs(TimeUnit.MILLISECONDS.toSeconds(currentTime - (closeTime?.time ?: currentTime)))
            when {
                diff < 1.5 * 60 * 60 -> Color.GREEN
                diff < 3 * 60 * 60 -> Color.YELLOW
                else -> Color.MAGENTA
            }
        }
}

data class RequiredApartment(
    val number: Int,
    val colored: Boolean
)
