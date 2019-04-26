package ru.relabs.kurjercontroller.models

import android.os.Parcel
import android.os.Parcelable
import ru.relabs.kurjercontroller.database.entities.TaskItemEntity
import ru.relabs.kurjercontroller.orEmpty

/**
 * Created by ProOrange on 19.03.2019.
 */

data class TaskItemModel(
    val id: Int, //iddot
    val taskId: Int,
    val publisherName: String,
    val defaultReportType: Int,
    val required: Boolean,
    val address: AddressModel,
    val entrances: MutableList<EntranceModel>,
    val notes: List<String>
) : Parcelable {
    val isClosed: Boolean
        get() = entrances.none { it.state == EntranceModel.CREATED }

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString().orEmpty(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readParcelable(AddressModel::class.java.classLoader) ?: AddressModel.blank(),
        parcel.createTypedArrayList(EntranceModel).orEmpty(),
        parcel.createStringArrayList().orEmpty()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(taskId)
        parcel.writeString(publisherName)
        parcel.writeInt(defaultReportType)
        parcel.writeByte(if (required) 1 else 0)
        parcel.writeParcelable(address, flags)
        parcel.writeTypedList(entrances)
        parcel.writeStringList(notes)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun toEntity(): TaskItemEntity =
        TaskItemEntity(
            id = 0,
            taskItemId = id,
            taskId = taskId,
            notes = notes,
            defaultReportType = defaultReportType,
            required = required,
            publisherName = publisherName,
            addressId = address.id
        )

    companion object CREATOR : Parcelable.Creator<TaskItemModel> {
        override fun createFromParcel(parcel: Parcel): TaskItemModel {
            return TaskItemModel(parcel)
        }

        override fun newArray(size: Int): Array<TaskItemModel?> {
            return arrayOfNulls(size)
        }
    }
}