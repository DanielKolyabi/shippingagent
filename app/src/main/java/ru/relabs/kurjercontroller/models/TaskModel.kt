package ru.relabs.kurjercontroller.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.database.entities.TaskEntity

/**
 * Created by ProOrange on 19.03.2019.
 */
fun Int.toAndroidState(): Int {
    return when (this) {
        0, 10, 11, 20 -> TaskModel.CREATED
        30 -> TaskModel.EXAMINED
        40 -> TaskModel.STARTED
        50, 60 -> TaskModel.COMPLETED
        12 -> TaskModel.CANCELED
        else -> TaskModel.COMPLETED
    }
}

fun Int.toSiriusState(): Int {
    return when (this) {
        TaskModel.CREATED -> 20
        TaskModel.EXAMINED -> 30
        TaskModel.STARTED -> 40
        TaskModel.COMPLETED -> 50
        TaskModel.CANCELED -> 12
        else -> 60
    }
}

data class TaskModel(
    val id: Int,
    val userId: Int,
    val initiator: String,
    val startControlDate: DateTime,
    val endControlDate: DateTime,
    val description: String,
    val storages: List<String>,
    val publishers: List<PublisherModel>,
    val taskItems: List<TaskItemModel>,
    val taskFilters: TaskFiltersModel?,
    val state: Int,
    val iteration: Int
) : Parcelable {

    val androidState: Int
        get() = state.toAndroidState()

    suspend fun getTaskItems(): List<TaskItemModel> = withContext(Dispatchers.IO) {
        //TODO: Filters loading
        return@withContext taskItems
    }

    fun toEntity(): TaskEntity =
        TaskEntity(
            id = id,
            state = state,
            storages = storages,
            userId = userId,
            initiator = initiator,
            description = description,
            endControlDate = endControlDate,
            startControlDate = startControlDate,
            iteration = iteration
        )

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString().orEmpty(),
        DateTime.parse(parcel.readString()),
        DateTime.parse(parcel.readString()),
        parcel.readString().orEmpty(),
        parcel.createStringArrayList().orEmpty(),
        parcel.createTypedArrayList(PublisherModel).orEmpty(),
        parcel.createTypedArrayList(TaskItemModel).orEmpty(),
        parcel.readParcelable(TaskFiltersModel::class.java.classLoader),
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(userId)
        parcel.writeString(initiator)
        parcel.writeString(startControlDate.toString())
        parcel.writeString(endControlDate.toString())
        parcel.writeString(description)
        parcel.writeStringList(storages)
        parcel.writeTypedList(publishers)
        parcel.writeTypedList(taskItems)
        parcel.writeParcelable(taskFilters, flags)
        parcel.writeInt(state)
        parcel.writeInt(iteration)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TaskModel> {
        override fun createFromParcel(parcel: Parcel): TaskModel {
            return TaskModel(parcel)
        }

        override fun newArray(size: Int): Array<TaskModel?> {
            return arrayOfNulls(size)
        }


        val CREATED = 0
        val EXAMINED = 1
        val STARTED = 2
        val COMPLETED = 4
        val CANCELED = 16
    }
}
