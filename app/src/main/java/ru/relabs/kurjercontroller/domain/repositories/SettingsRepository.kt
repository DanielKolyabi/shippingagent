package ru.relabs.kurjercontroller.domain.repositories

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.relabs.kurjercontroller.domain.models.AllowedCloseRadius
import ru.relabs.kurjercontroller.domain.models.EntrancesMonitoring
import ru.relabs.kurjercontroller.domain.models.GpsRefreshTimes
import ru.relabs.kurjercontroller.utils.Right

/**
 * Created by Daniil Kurchanov on 13.01.2020.
 */
class SettingsRepository(
    private val api: ControlRepository,
    private val sharedPreferences: SharedPreferences
) {
    val scope = CoroutineScope(Dispatchers.Main)
    var isCloseRadiusRequired: Boolean = sharedPreferences.getBoolean(RADIUS_REQUIRED_KEY, true)
    var isPhotoRadiusRequired: Boolean = sharedPreferences.getBoolean(PHOTO_REQUIRED_KEY, true)
    var closeGpsUpdateTime: GpsRefreshTimes = loadSavedGPSRefreshTimes()
    private var entrancesMonitoring: EntrancesMonitoring = loadEntrancesMonitoring()
        set(value) {
            _entranceMonitoringFlow.tryEmit(value)
            field = value
        }
    private val _entranceMonitoringFlow = MutableStateFlow(entrancesMonitoring)
    val entranceMonitoringFlow: StateFlow<EntrancesMonitoring> = _entranceMonitoringFlow

    private var updateJob: Job? = null

    fun resetData() {
        closeGpsUpdateTime = GpsRefreshTimes(40, 40)
        isCloseRadiusRequired = false
        isPhotoRadiusRequired = false
        entrancesMonitoring = EntrancesMonitoring(false)
        sharedPreferences.edit {
            remove(RADIUS_REQUIRED_KEY)
            remove(RADIUS_KEY)
            remove(PHOTO_REQUIRED_KEY)
            remove(PHOTO_GPS_KEY)
            remove(CLOSE_GPS_KEY)
            remove(MONITORING_COUNTER_KEY)
        }
    }

    suspend fun startRemoteUpdating() {
        updateJob?.cancel()
        updateJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                loadSettingsRemote()
                delay(60 * 1000)
            }
        }
    }

    private suspend fun loadSettingsRemote() = withContext(Dispatchers.Default) {
        when (val r = api.getAppSettings()) {
            is Right -> {
                isCloseRadiusRequired = r.value.isCloseRadiusRequired
                isPhotoRadiusRequired = r.value.isPhotoRadiusRequired
                closeGpsUpdateTime = r.value.gpsRefreshTimes
                entrancesMonitoring = r.value.entrancesMonitoring
                saveRadius(isCloseRadiusRequired, isPhotoRadiusRequired)
                saveGPSRefreshTime(closeGpsUpdateTime)
                saveEntrancesMonitoring(r.value.entrancesMonitoring)
            }
        }
    }

    private fun saveEntrancesMonitoring(entrancesMonitoring: EntrancesMonitoring) {
        sharedPreferences.edit {
            putBoolean(MONITORING_COUNTER_KEY, entrancesMonitoring.isCounterEnabled)
        }
    }

    private fun saveGPSRefreshTime(gpsRefreshTimes: GpsRefreshTimes) {
        sharedPreferences.edit {
            putInt(PHOTO_GPS_KEY, gpsRefreshTimes.photo)
            putInt(CLOSE_GPS_KEY, gpsRefreshTimes.close)
        }
    }

    private fun loadSavedGPSRefreshTimes(): GpsRefreshTimes {
        val photo = sharedPreferences.getInt(PHOTO_GPS_KEY, 40)
        val close = sharedPreferences.getInt(CLOSE_GPS_KEY, 40)
        return GpsRefreshTimes(close = close, photo = photo)
    }

    private fun loadEntrancesMonitoring(): EntrancesMonitoring {
        val counterEnabled = sharedPreferences.getBoolean(MONITORING_COUNTER_KEY, false)
        return EntrancesMonitoring(counterEnabled)
    }

    private fun saveRadius(isCloseRadiusRequired: Boolean, isPhotoRadiusRequired: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(RADIUS_REQUIRED_KEY, isCloseRadiusRequired)
        editor.putBoolean(PHOTO_REQUIRED_KEY, isPhotoRadiusRequired)
        editor.apply()
    }

    companion object {
        const val RADIUS_REQUIRED_KEY = "radius_required"
        const val PHOTO_REQUIRED_KEY = "photo_required"
        const val CLOSE_GPS_KEY = "close_gps"
        const val PHOTO_GPS_KEY = "photo_gps"
        const val MONITORING_COUNTER_KEY = "monitoring_counter"

        @Deprecated("Kept for migration purpose")
        const val RADIUS_KEY = "radius"

        @Deprecated("Kept for migration purpose")
        const val DEFAULT_REQUIRED_RADIUS = 50
    }
}