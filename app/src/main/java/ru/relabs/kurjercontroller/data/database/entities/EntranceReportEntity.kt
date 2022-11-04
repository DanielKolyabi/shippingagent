package ru.relabs.kurjercontroller.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.data.database.models.ApartmentResult

/**
 * Created by ProOrange on 19.03.2019.
 */
@Entity(tableName = "entrance_reports")
data class EntranceReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "task_id")
    val taskId: Int,
    @ColumnInfo(name = "task_item_id")
    val taskItemId: Int, //iddot
    val idnd: Int,
    @ColumnInfo(name = "entrance_number")
    val entranceNumber: Int,
    @ColumnInfo(name = "start_appartaments")
    val startAppartaments: Int,
    @ColumnInfo(name = "end_appartaments")
    val endAppartaments: Int,
    val floors: Int,
    val description: String,
    val code: String,
    val key: String,
    @ColumnInfo(name = "euro_key")
    val euroKey: String,
    @ColumnInfo(name = "is_delivery_wrong")
    val isDeliveryWrong: Boolean,
    @ColumnInfo(name = "has_lookup_post")
    val hasLookupPost: Boolean,
    val token: String,
    @ColumnInfo(name = "apartment_results")
    val apartmentResult: List<ApartmentResult>,
    @ColumnInfo(name = "close_time")
    val closeTime: DateTime,
    @ColumnInfo(name = "publisher_id")
    val publisherId: Int,
    @ColumnInfo(name = "mailbox_type")
    val mailboxType: Int,
    @ColumnInfo(name = "gps_lat")
    val gpsLat: Double,
    @ColumnInfo(name = "gps_long")
    val gpsLong: Double,
    @ColumnInfo(name = "gps_time")
    val gpsTime: DateTime,
    @ColumnInfo(name = "entrance_closed")
    val entranceClosed: Boolean,
    @ColumnInfo(name = "remove_after_send") var removeAfterSend: Boolean,
    @ColumnInfo(name = "close_distance") var closeDistance: Int,
    @ColumnInfo(name = "allowed_distance") var allowedDistance: Int,
    @ColumnInfo(name = "radius_required") var radiusRequired: Boolean,
    @ColumnInfo(name = "is_stacked") var isStacked: Boolean,
)