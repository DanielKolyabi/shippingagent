package ru.relabs.kurjercontroller.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * Created by ProOrange on 19.03.2019.
 */

data class TaskItemModel(
    val id: Int, //iddot
    @SerializedName("task_id")
    val taskId: Int,
    val address: AddressModel,
    val notes: List<String>,
    val entrances: List<EntranceModel>
) : Parcelable {

    val isClosed: Boolean
        get() = entrances.find { it.state == EntranceModel.CREATED } == null

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readParcelable(AddressModel::class.java.classLoader),
        parcel.createStringArrayList().orEmpty(),
        parcel.createTypedArrayList(EntranceModel).orEmpty()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(taskId)
        parcel.writeParcelable(address, flags)
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
    }
}