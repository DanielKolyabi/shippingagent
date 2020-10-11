package ru.relabs.kurjercontroller.domain.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ru.relabs.kurjercontroller.data.database.entities.EntranceEntity
import ru.relabs.kurjercontroller.domain.mappers.MappingException

/**
 * Created by ProOrange on 19.03.2019.
 */

@Parcelize
data class EntranceNumber(val number: Int): Parcelable

enum class EntranceState{
    CREATED, CLOSED
}

fun EntranceState.toInt() = when(this){
    EntranceState.CREATED -> EntranceEntity.STATE_CREATED
    EntranceState.CLOSED -> EntranceEntity.STATE_CLOSED
}

fun Int.toEntranceState() = when(this){
    EntranceEntity.STATE_CREATED -> EntranceState.CREATED
    EntranceEntity.STATE_CLOSED -> EntranceState.CLOSED
    else -> throw MappingException("state", this)
}
@Parcelize
data class Entrance(
    val number: EntranceNumber,
    val euroKey: String,
    val key: String,
    val code: String,
    var startApartments: Int,
    var endApartments: Int,
    val floors: Int,
    val mailboxType: Int,
    var state: EntranceState,
    val hasLookout: Boolean
) : Parcelable {

    fun toEntity(taskId: Int, taskItemId: Int): EntranceEntity =
        EntranceEntity(
            id = 0,
            taskItemId = taskItemId,
            state = state.toInt(),
            euroKey = euroKey,
            key = key,
            code = code,
            endApartments = endApartments,
            floors = floors,
            mailboxType = mailboxType,
            startApartments = startApartments,
            number = number.number,
            taskId = taskId,
            hasLookout = hasLookout
        )

    companion object {
        const val NUMBER_TASK_ITEM = -1
    }
}