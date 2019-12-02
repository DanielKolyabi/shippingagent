package ru.relabs.kurjercontroller.models

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import org.joda.time.DateTime
import org.joda.time.Seconds
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
    val notes: List<String>,
    var closeTime: DateTime? = null,
    val deliverymanId: Int,
    var isNew: Boolean,
    val wrongMethod: Boolean,
    val buttonName: String,
    val requiredApartments: String
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
        get() = entrances.none { it.state == EntranceModel.CREATED }

    val placemarkColor: Int
        get() = if (isClosed) {
            Color.GRAY
        } else if (closeTime == null) {
            Color.BLUE
        } else {
            val diff = Seconds.secondsBetween(closeTime, DateTime()).seconds
            when {
                diff < 1.5 * 60 * 60 -> Color.GREEN
                diff < 3 * 60 * 60 -> Color.YELLOW
                else -> Color.MAGENTA
            }
        }


    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString().orEmpty(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readParcelable(AddressModel::class.java.classLoader) ?: AddressModel.blank(),
        parcel.createTypedArrayList(EntranceModel).orEmpty(),
        parcel.createStringArrayList().orEmpty(),
        parcel.readLong().let {
            if (it > 0) {
                DateTime(it)
            } else {
                null
            }
        },
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(taskId)
        parcel.writeString(publisherName)
        parcel.writeInt(defaultReportType)
        parcel.writeByte(if (required) 1 else 0)
        parcel.writeParcelable(address, flags)
        parcel.writeTypedList(entrances)
        parcel.writeStringList(notes)
        parcel.writeLong(closeTime?.millis ?: -1)
        parcel.writeInt(deliverymanId)
        parcel.writeByte(if (isNew) 1 else 0)
        parcel.writeByte(if (wrongMethod) 1 else 0)
        parcel.writeString(buttonName)
        parcel.writeString(buttonName)
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
            addressId = address.id,
            closeTime = closeTime,
            deliverymanId = deliverymanId,
            isNew = isNew,
            wrongMethod = wrongMethod,
            buttonName = buttonName,
            requiredApartments = requiredApartments
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

data class RequiredApartment(
    val number: Int,
    val colored: Boolean
)
