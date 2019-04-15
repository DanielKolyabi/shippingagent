package ru.relabs.kurjercontroller.models

import android.os.Parcel
import android.os.Parcelable
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.database.entities.TaskPublisherEntity

data class PublisherModel(
    val id: Int,
    val name: String,
    val taskId: Int,
    val startDistributionDate: DateTime,
    val endDistributionDate: DateTime
) : Parcelable {
    fun toEntity(): TaskPublisherEntity =
        TaskPublisherEntity(
            id = 0,
            name = name,
            endDistributionDate = endDistributionDate,
            startDistributionDate = startDistributionDate,
            taskId = taskId,
            publisherId = id
        )

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString().orEmpty(),
        parcel.readInt(),
        DateTime.parse(parcel.readString()),
        DateTime.parse(parcel.readString())
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeInt(taskId)
        parcel.writeString(startDistributionDate.toString())
        parcel.writeString(endDistributionDate.toString())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PublisherModel> {
        override fun createFromParcel(parcel: Parcel): PublisherModel {
            return PublisherModel(parcel)
        }

        override fun newArray(size: Int): Array<PublisherModel?> {
            return arrayOfNulls(size)
        }
    }
}
