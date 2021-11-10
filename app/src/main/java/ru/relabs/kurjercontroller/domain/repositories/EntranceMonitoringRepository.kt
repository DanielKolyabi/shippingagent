package ru.relabs.kurjercontroller.domain.repositories

import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.data.database.AppDatabase
import ru.relabs.kurjercontroller.data.database.entities.ClosedAddressEntity
import ru.relabs.kurjercontroller.domain.models.Entrance
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.domain.storage.CurrentUserStorage

class EntranceMonitoringRepository(
    private val database: AppDatabase,
    private val databaseRepository: DatabaseRepository,
    private val sharedPreferences: SharedPreferences,
    private val currentUserStorage: CurrentUserStorage
) {
    private val _closedCountFlow = MutableStateFlow(0)
    val closedCountFlow: StateFlow<Int> = _closedCountFlow
    private val _requiredCountFlow = MutableStateFlow(0)
    val requiredCountFlow: StateFlow<Int> = _requiredCountFlow

    suspend fun trackEntrance(taskItem: TaskItem, entrance: Entrance) = withContext(Dispatchers.IO) {
        val currentUserLogin = currentUserStorage.getCurrentUserLogin()?.login ?: return@withContext
        Log.d("EntranceMonitoring", "PreClean")
        cleanDailyCloses()
        Log.d("EntranceMonitoring", "PostClean")
        val sameClosedEntrances =
            database.closedAddressesDao().findEntrance(taskItem.address.idnd, entrance.number.number, currentUserLogin)
        Log.d("EntranceMonitoring", "Track, sameEntrances: $sameClosedEntrances")
        if (sameClosedEntrances.isEmpty()) {
            database.closedAddressesDao()
                .insert(ClosedAddressEntity(0, taskItem.address.idnd, entrance.number.number, currentUserLogin))
            _closedCountFlow.tryEmit(getClosedEntrancesCount())
        }
    }

    suspend fun getClosedEntrancesCount(withCleanup: Boolean = true): Int = withContext(Dispatchers.IO) {
        val currentUserLogin = currentUserStorage.getCurrentUserLogin()?.login ?: return@withContext 0
        if (withCleanup) {
            cleanDailyCloses()
        }
        database.closedAddressesDao()
            .getClosedCount(currentUserLogin)
            .also { _closedCountFlow.tryEmit(it) }
    }

    suspend fun getRequiredEntrancesCount() = withContext(Dispatchers.IO) {
        val now = DateTime.now()
        val tasks = databaseRepository.getTasks()
        tasks
            .filter { now > it.startControlDate && now.withTimeAtStartOfDay() < it.endControlDate } //Tasks for current date
            .flatMap { it.taskItems } //Item with task
            .distinctBy { it.address.idnd } //Unique addresses
            .sumOf { it.entrances.count() }
            .also { _requiredCountFlow.tryEmit(it) }
    }

    private suspend fun cleanDailyCloses() = withContext(Dispatchers.IO) {
        val currentUserLogin = currentUserStorage.getCurrentUserLogin()?.login ?: return@withContext
        val today = DateTime.now()
        val savedLatestTrackingDay = sharedPreferences.getString(LATEST_TRACKING_DAY_KEY, null)
        val latestTrackingDay = if (savedLatestTrackingDay == null) {
            sharedPreferences.edit().putString(LATEST_TRACKING_DAY_KEY, today.toString()).apply()
            today
        } else {
            DateTime.parse(savedLatestTrackingDay)
        }
        if (latestTrackingDay.dayOfYear < today.dayOfYear || latestTrackingDay.year < today.year) {
            database.closedAddressesDao().cleanForUser(currentUserLogin)
            _closedCountFlow.tryEmit(getClosedEntrancesCount(false))
            sharedPreferences.edit().putString(LATEST_TRACKING_DAY_KEY, today.toString()).apply()
        }
    }

    companion object {
        const val LATEST_TRACKING_DAY_KEY = "latest_day"
    }
}