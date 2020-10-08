package ru.relabs.kurjercontroller.data.models.tasks

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Created by ProOrange on 05.09.2018.
 */

data class TaskResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("initiator") val initiator: String,
    @SerializedName("state") val state: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("start_сontrol_date") val startControlDate: Date,
    @SerializedName("end_сontrol_date") val endControlDate: Date,
    @SerializedName("description") val description: String,
    @SerializedName("iteration") val iteration: Int,
    @SerializedName("first_examined_device_id") val firstExaminedDeviceId: String?,
    @SerializedName("items") val items: List<TaskItemResponse>,
    @SerializedName("publishers") val publishers: List<TaskPublisherResponse>,
    @SerializedName("storages") val storages: List<StorageResponse>,
    @SerializedName("filters") val filters: FiltersResponse,
    @SerializedName("filtered") val filtered: Boolean
)
//{
//    fun toTaskModel(deviceId: String): TaskModel {
//        return TaskModel(
//                id, name, edition, copies, packs, remain, area, fromSiriusState(deviceId), startTime, endTime, brigade, brigadier, rastMapUrl, userId,
//                items.map{it.toTaskItemModel()}, city, storageAddress ?: "", iteration, false, coupleType
//
//        )
//    }
//
//    private fun fromSiriusState(deviceId: String): Int {
//        val newState = when (state) {
//            0, 10, 11, 20 -> TaskModel.CREATED
//            30 -> TaskModel.EXAMINED
//            40, 41, 42 -> TaskModel.STARTED
//            50, 51, 60, 61 -> TaskModel.COMPLETED
//            12 -> TaskModel.CANCELED
//            else -> TaskModel.COMPLETED
//        }
//
//        if(newState == TaskModel.CANCELED){
//            return newState
//        }
//
//        if(newState > TaskModel.CREATED && deviceId != firstExaminedDeviceId){
//            return newState xor TaskModel.BY_OTHER_USER
//        }
//
//        return newState
//    }
//}