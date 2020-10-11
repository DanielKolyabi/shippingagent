package ru.relabs.kurjercontroller.domain.useCases

import android.location.Location
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.data.database.entities.EntranceReportEntity
import ru.relabs.kurjercontroller.data.database.models.ApartmentResult
import ru.relabs.kurjercontroller.domain.controllers.TaskEvent
import ru.relabs.kurjercontroller.domain.controllers.TaskEventController
import ru.relabs.kurjercontroller.domain.models.Entrance
import ru.relabs.kurjercontroller.domain.models.GPSCoordinatesModel
import ru.relabs.kurjercontroller.domain.models.TaskPublisher
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.domain.repositories.DatabaseRepository
import ru.relabs.kurjercontroller.domain.storage.AuthTokenStorage

class ReportUseCase(
    private val databaseRepository: DatabaseRepository,
    private val tokenStorage: AuthTokenStorage,
    private val taskEventController: TaskEventController
) {

    suspend fun createReport(taskItem: TaskItem, entrance: Entrance, publisher: TaskPublisher, location: GPSCoordinatesModel) {
        val entranceResult = databaseRepository.getEntranceResult(taskItem, entrance)
        val apartmentResults = databaseRepository.getEntranceApartments(taskItem, entrance)

        val report = EntranceReportEntity(
            0,
            taskItem.taskId.id,
            taskItem.id.id,
            taskItem.address.idnd,
            entrance.number.number,
            entranceResult?.apartmentFrom ?: entrance.startApartments,
            entranceResult?.apartmentTo ?: entrance.endApartments,
            entranceResult?.floors ?: entrance.floors,
            entranceResult?.description ?: "",
            entranceResult?.code ?: entrance.code,
            entranceResult?.key ?: entrance.key,
            entranceResult?.euroKey ?: entrance.euroKey,
            entranceResult?.isDeliveryWrong ?: false,
            entranceResult?.hasLookupPost ?: entrance.hasLookout ?: false,
            tokenStorage.getToken() ?: "",
            apartmentResults.map {
                ApartmentResult(
                    it.apartmentNumber,
                    it.buttonState,
                    it.buttonGroup,
                    it.description
                )
            },
            DateTime.now(),
            publisher.id.id,
            entranceResult?.mailboxType ?: entrance.mailboxType,
            location.lat,
            location.long,
            location.time,
            entranceResult?.entranceClosed ?: false
        )

        databaseRepository.createEntranceReport(report)

        taskEventController.send(TaskEvent.EntranceClosed(taskItem.taskId, taskItem.id, entrance.number))
    }

    private fun getReportLocation(location: Location?) = when (location) {
        null -> GPSCoordinatesModel(0.0, 0.0, DateTime(0))
        else -> GPSCoordinatesModel(location.latitude, location.longitude, DateTime(location.time))
    }
}