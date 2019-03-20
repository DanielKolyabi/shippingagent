package ru.relabs.kurjercontroller.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * Created by ProOrange on 19.03.2019.
 */

data class TaskItemModel(
    val id: Int,
    @SerializedName("task_id")
    val taskId: Int,
    val address: AddressModel,
    val state: Int,
    val notes: List<String>,
    val entrances: List<EntranceModel>
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readParcelable(AddressModel::class.java.classLoader),
        parcel.readInt(),
        parcel.createStringArrayList(),
        parcel.createTypedArrayList(EntranceModel)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(taskId)
        parcel.writeParcelable(address, flags)
        parcel.writeInt(state)
        parcel.writeStringList(notes)
        parcel.writeTypedList(entrances)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TaskItemModel> {
        override fun createFromParcel(parcel: Parcel): TaskItemModel {
            return TaskItemModel(parcel)
        }

        override fun newArray(size: Int): Array<TaskItemModel?> {
            return arrayOfNulls(size)
        }

        val CREATED = 0
        val CLOSED = 1
    }
}