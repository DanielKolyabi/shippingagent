package ru.relabs.kurjercontroller.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime

/**
 * Created by ProOrange on 19.03.2019.
 */
data class TaskModel(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    val initiator: String,
    val publisher: String,
    val edition: Int,
    @SerializedName("start_control_date")
    val startControlDate: DateTime,
    @SerializedName("end_control_date")
    val endControlDate: DateTime,
    @SerializedName("start_distribution_date")
    val startDistributionDate: DateTime,
    @SerializedName("end_distribution_date")
    val endDistributionDate: DateTime,
    val storages: List<String>,
    val description: String,
    @Expose
    val taskItems: List<TaskItemModel>,
    val taskFilters: TaskFiltersModel?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readInt(),
        DateTime.parse(parcel.readString()),
        DateTime.parse(parcel.readString()),
        DateTime.parse(parcel.readString()),
        DateTime.parse(parcel.readString()),
        parcel.createStringArrayList().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.createTypedArrayList(TaskItemModel)?.toList().orEmpty(),
        parcel.readParcelable(TaskFiltersModel::class.java.classLoader)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(userId)
        parcel.writeString(initiator)
        parcel.writeString(publisher)
        parcel.writeInt(edition)
        parcel.writeString(startControlDate.toString())
        parcel.writeString(endControlDate.toString())
        parcel.writeString(startDistributionDate.toString())
        parcel.writeString(endDistributionDate.toString())
        parcel.writeStringList(storages)
        parcel.writeString(description)
        parcel.writeTypedList(taskItems)
        parcel.writeParcelable(taskFilters, flags)
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
    }
}