package ru.relabs.kurjercontroller.domain.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.domain.mappers.MappingException
import java.util.*

@Parcelize
data class TaskId(val id: Int) : Parcelable

@Parcelize
data class Task(
    val id: TaskId,
    val userId: Int,
    val initiator: String,
    val startControlDate: DateTime,
    val endControlDate: DateTime,
    val description: String,
    val storages: List<TaskStorage>,
    val publishers: List<TaskPublisher>,
    val taskItems: List<TaskItem>,
    val taskFilters: TaskFilters,
    val state: State,
    val iteration: Int,
    val firstExaminedDeviceId: String?,
    val filtered: Boolean,
    val isOnline: Boolean,
    val withPlanned: Boolean
) : Parcelable {
    val name: String
        get() {
            return if(filtered){
                description.ifEmpty { "Фильтров: ${taskFilters.all.size}" }
            }else{
                publishers.joinToString("\n", transform = {it.name})
            }
        }
    @Parcelize
    data class State(
        val state: TaskState,
        val byOtherUser: Boolean
    ) : Parcelable
}


enum class TaskState {
    CREATED,
    EXAMINED,
    STARTED,
    COMPLETED,
    CANCELED;

    fun toInt(): Int {
        return when(this){
            CREATED -> 1
            EXAMINED -> 2
            STARTED -> 3
            COMPLETED -> 4
            CANCELED -> 5
        }
    }
}

fun Int.toTaskState() = when(this){
    1 -> TaskState.CREATED
    2 -> TaskState.EXAMINED
    3 -> TaskState.STARTED
    4 -> TaskState.COMPLETED
    5 -> TaskState.CANCELED
    else -> throw MappingException("state", this)
}