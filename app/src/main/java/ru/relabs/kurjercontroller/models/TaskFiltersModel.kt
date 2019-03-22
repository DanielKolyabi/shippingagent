package ru.relabs.kurjercontroller.models

import android.os.Parcel
import android.os.Parcelable
import ru.relabs.kurjercontroller.orEmpty

/**
 * Created by ProOrange on 21.03.2019.
 */
data class TaskFiltersModel(
    val publishers: MutableList<FilterModel>,
    val brigades: MutableList<FilterModel>,
    val areas: MutableList<FilterModel>,
    val users: MutableList<FilterModel>,
    val cities: MutableList<FilterModel>,
    val streets: MutableList<FilterModel>,
    val districts: MutableList<FilterModel>,
    val regions: MutableList<FilterModel>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(FilterModel).orEmpty(),
        parcel.createTypedArrayList(FilterModel).orEmpty(),
        parcel.createTypedArrayList(FilterModel).orEmpty(),
        parcel.createTypedArrayList(FilterModel).orEmpty(),
        parcel.createTypedArrayList(FilterModel).orEmpty(),
        parcel.createTypedArrayList(FilterModel).orEmpty(),
        parcel.createTypedArrayList(FilterModel).orEmpty(),
        parcel.createTypedArrayList(FilterModel).orEmpty()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(publishers)
        parcel.writeTypedList(brigades)
        parcel.writeTypedList(areas)
        parcel.writeTypedList(users)
        parcel.writeTypedList(cities)
        parcel.writeTypedList(streets)
        parcel.writeTypedList(districts)
        parcel.writeTypedList(regions)
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
    val fixed: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString().orEmpty(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeByte(if (fixed) 1 else 0)
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