package ru.relabs.kurjercontroller.data.database

/**
 * Created by ProOrange on 30.08.2018.
 */

import androidx.room.TypeConverter
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import ru.relabs.kurjercontroller.data.database.models.ApartmentResult
import ru.relabs.kurjercontroller.domain.models.AddressModel
import ru.relabs.kurjercontroller.domain.models.GPSCoordinatesModel
import java.lang.reflect.Type
import java.util.*


class Converters {
    val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        .registerTypeAdapter(DateTime::class.java, object : JsonSerializer<DateTime> {
            override fun serialize(src: DateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
                return JsonPrimitive(src?.millis)
            }
        })
        .registerTypeAdapter(DateTime::class.java, object : JsonDeserializer<DateTime> {
            override fun deserialize(
                json: JsonElement?,
                typeOfT: Type?,
                context: JsonDeserializationContext?
            ): DateTime {
                return DateTime(json?.asLong)
            }
        })
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
    fun apartmentResultsToJSON(value: List<ApartmentResult>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun jsonToApartmentResults(value: String): List<ApartmentResult> {
        return gson.fromJson(value, object: TypeToken<List<ApartmentResult>>() {}.type)
    }

    @TypeConverter
    fun gpsToJSON(value: GPSCoordinatesModel): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun jsonToGPS(value: String): GPSCoordinatesModel {
        return gson.fromJson(value, GPSCoordinatesModel::class.java)
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