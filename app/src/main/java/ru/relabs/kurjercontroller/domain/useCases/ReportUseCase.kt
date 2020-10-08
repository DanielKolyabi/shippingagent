package ru.relabs.kurjercontroller.domain.useCases

import android.location.Location
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.domain.controllers.TaskEvent
import ru.relabs.kurjercontroller.domain.controllers.TaskEventController
import ru.relabs.kurjercontroller.domain.mappers.ReportEntranceSelectionMapper
import ru.relabs.kurjercontroller.domain.models.AllowedCloseRadius
import ru.relabs.kurjercontroller.domain.models.GPSCoordinatesModel
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.domain.repositories.DatabaseRepository
import ru.relabs.kurjercontroller.domain.storage.AuthTokenStorage
import ru.relabs.kurjercontroller.utils.calculateDistance
import java.util.*
import kotlin.math.roundToInt

class ReportUseCase(
    private val databaseRepository: DatabaseRepository,
    private val tokenStorage: AuthTokenStorage,
    private val taskEventController: TaskEventController
) {

    suspend fun createReport(task: Task, taskItem: TaskItem, location: Location?, batteryLevel: Float, isCloseTaskRequired: Boolean) {
        val result = databaseRepository.getTaskItemResult(taskItem)
        val distance = location?.let {
            calculateDistance(
                location.latitude,
                location.longitude,
                taskItem.address.lat.toDouble(),
                taskItem.address.long.toDouble()
            )
        } ?: Int.MAX_VALUE.toDouble()

        val reportItem = ReportQueryItemEntity(
            0,
            taskItem.id.id,
            task.id.id,
            taskItem.address.id.id,
            getReportLocation(location),
            Date(),
            result?.description ?: "",
            result?.entrances?.map {
                it.entranceNumber.number to ReportEntranceSelectionMapper.toBits(it.selection)
            } ?: emptyList(),
            tokenStorage.getToken() ?: "",
            (batteryLevel * 100).roundToInt(),
            isCloseTaskRequired,
            distance.toInt()
        )

        databaseRepository.createTaskItemReport(reportItem)

        if (isCloseTaskRequired) {
            databaseRepository.closeTaskItem(taskItem)
            taskEventController.send(TaskEvent.TaskItemClosed(taskItem.id))
            if (databaseRepository.isTaskCloseRequired(taskItem.taskId)) {
                databaseRepository.closeTaskById(taskItem.taskId, true)
                taskEventController.send(TaskEvent.TaskClosed(taskItem.taskId))
            }
        }
    }

    private fun getReportLocation(location: Location?) = when (location) {
        null -> GPSCoordinatesModel(0.0, 0.0, DateTime(0))
        else -> GPSCoordinatesModel(location.latitude, location.longitude, DateTime(location.time))
    }
}