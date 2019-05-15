package ru.relabs.kurjercontroller.models

import android.os.Parcel
import android.os.Parcelable
import ru.relabs.kurjercontroller.orEmpty

/**
 * Created by ProOrange on 21.03.2019.
 */
data class TaskFiltersModel(
    val publishers: MutableList<FilterModel>,
    val districts: MutableList<FilterModel>,
    val regions: MutableList<FilterModel>,
    val brigades: MutableList<FilterModel>,
    val users: MutableList<FilterModel>
) : Parcelable {
    val all: List<FilterModel>
        get() = publishers.asSequence()
            .plus(districts)
            .plus(regions)
            .plus(brigades)
            .plus(users)
            .toList()


    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(FilterModel).orEmpty(),
        parcel.createTypedArrayList(FilterModel).orEmpty(),
        parcel.createTypedArrayList(FilterModel).orEmpty(),
        parcel.createTypedArrayList(FilterModel).orEmpty(),
        parcel.createTypedArrayList(FilterModel).orEmpty()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(publishers)
        parcel.writeTypedList(districts)
        parcel.writeTypedList(regions)
        parcel.writeTypedList(brigades)
        parcel.writeTypedList(users)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TaskFiltersModel> {
        override fun createFromParcel(parcel: Parcel): TaskFiltersModel {
            return TaskFiltersModel(parcel)
        }

        override fun newArray(size: Int): Array<TaskFiltersModel?> {
            return arrayOfNulls(size)
        }

        fun blank(): TaskFiltersModel {
            return TaskFiltersModel(
                mutableListOf(),
                mutableListOf(),
                mutableListOf(),
                mutableListOf(),
                mutableListOf()
            )
        }
    }
}

data class FilterModel(
    val id: Int,
    val name: String,
    val fixed: Boolean,
    var active: Boolean,
    val type: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString().orEmpty(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeByte(if (fixed) 1 else 0)
        parcel.writeByte(if (active) 1 else 0)
        parcel.writeInt(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FilterModel> {
        override fun createFromParcel(parcel: Parcel): FilterModel {
            return FilterModel(parcel)
        }

        override fun newArray(size: Int): Array<FilterModel?> {
            return arrayOfNulls(size)
        }
    }
}