package ru.relabs.kurjercontroller.database

/**
 * Created by ProOrange on 30.08.2018.
 */

import android.util.Log
import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import ru.relabs.kurjercontroller.models.AddressModel
import ru.relabs.kurjercontroller.models.GPSCoordinatesModel
import java.util.*


class Converters {
    val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        .create()

    @TypeConverter
    fun fromTimestamp(value: Long?): DateTime? {
        return if (value == null) null else DateTime(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: DateTime?): Long? {
        return date?.millis
    }

    @TypeConverter
    fun jsonToIntList(value: String): List<Int> {
        return gson.fromJson(value, object : TypeToken<List<Int>>() {}.type)
    }

    @TypeConverter
    fun intListToJSON(value: List<Int>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun addressToJSON(value: AddressModel): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun jsonToAddress(json: String): AddressModel {
        return gson.fromJson(json, AddressModel::class.java)
    }

    @TypeConverter
    fun stringToStringList(value: String): List<String> {
        return gson.fromJson(value, object : TypeToken<List<String>>() {}.type)
    }

    @TypeConverter
    fun stringListToJSON(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun gpsToJSON(value: GPSCoordinatesModel): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun jsonToGPS(value: String): GPSCoordinatesModel {
        val obj = gson.fromJson(value, JsonElement::class.java).asJsonObject
        val lat = obj["lat"].asDouble
        val long = obj["long"].asDouble
        val timeStr = obj["time"].asString
        Log.d("Database Conv", timeStr)

        var date = tryParseDateWithFormat("yyyy-MM-dd'T'HH:mm:ss", timeStr)
        if (date != null) {
            return GPSCoordinatesModel(lat, long, date)
        }

        date = tryParseDateWithFormat("MMM d, yyyy HH:mm:ss", timeStr)
        if (date != null) {
            return GPSCoordinatesModel(lat, long, date)
        }

        date = tryParseDateWithFormat("MMM d, yyyy HH:mm:ss", timeStr, Locale("ru", "RU"))
        if (date != null) {
            return GPSCoordinatesModel(lat, long, date)
        }

        return GPSCoordinatesModel(lat, long, DateTime())
    }

    private fun tryParseDateWithFormat(
        formatString: String,
        timeString: String,
        locale: Locale = Locale.ENGLISH
    ): DateTime? {
        try {
            val format = DateTimeFormat.forPattern(formatString).withLocale(locale)
            val time = format.parseDateTime(timeString)
            return time
        } catch (e: Exception) {
            return null
        }
    }
}