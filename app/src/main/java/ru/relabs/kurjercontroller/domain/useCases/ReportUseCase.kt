package ru.relabs.kurjercontroller.domain.useCases

import android.location.Location
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.data.database.entities.EntranceReportEntity
import ru.relabs.kurjercontroller.data.database.models.ApartmentResult
import ru.relabs.kurjercontroller.domain.controllers.TaskEvent
import ru.relabs.kurjercontroller.domain.controllers.TaskEventController
import ru.relabs.kurjercontroller.domain.models.*
import ru.relabs.kurjercontroller.domain.providers.PathsProvider
import ru.relabs.kurjercontroller.domain.repositories.DatabaseRepository
import ru.relabs.kurjercontroller.domain.repositories.SettingsRepository
import ru.relabs.kurjercontroller.domain.storage.AuthTokenStorage
import ru.relabs.kurjercontroller.presentation.report.ReportApartmentButtonsMode
import ru.relabs.kurjercontroller.presentation.reportPager.TaskItemWithTaskIds
import ru.relabs.kurjercontroller.utils.calculateDistance
import java.util.*

class ReportUseCase(
    private val databaseRepository: DatabaseRepository,
    private val tokenStorage: AuthTokenStorage,
    private val taskEventController: TaskEventController,
    private val pathsProvider: PathsProvider,
    private val settingsRepository: SettingsRepository
) {

    suspend fun createReport(taskItem: TaskItem, entrance: Entrance, location: Location?, withRemove: Boolean) {
        val entranceResult = databaseRepository.getEntranceResult(taskItem, entrance)
        val apartmentResults = databaseRepository.getEntranceApartments(taskItem, entrance)

        val distance = location?.let {
            calculateDistance(
                location.latitude,
                location.longitude,
                taskItem.address.lat,
                taskItem.address.long
            )
        } ?: Int.MAX_VALUE.toDouble()

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
            entranceResult?.hasLookupPost ?: entrance.hasLookout,
            tokenStorage.getToken() ?: "",
            apartmentResults.map {
                ApartmentResult(
                    it.apartmentNumber,
                    it.buttonState,
                    when (it.buttonGroup) {
                        ReportApartmentButtonsMode.Main -> 0
                        ReportApartmentButtonsMode.Additional -> 1
                    },
                    it.description
                )
            },
            DateTime.now(),
            taskItem.publisherId.id,
            entranceResult?.mailboxType ?: entrance.mailboxType,
            location?.latitude ?: 0.0,
            location?.longitude ?: 0.0,
            DateTime(location?.time ?: 0),
            entranceResult?.entranceClosed ?: false,
            withRemove,
            distance.toInt(),
            (settingsRepository.allowedCloseRadius as? AllowedCloseRadius.Required)?.distance ?: 0,
            settingsRepository.allowedCloseRadius is AllowedCloseRadius.Required
        )

        databaseRepository.createEntranceReport(report)
        if (withRemove) {
            databaseRepository.closeEntrance(taskItem.taskId, taskItem.id, entrance.number)
            taskEventController.send(TaskEvent.EntranceClosed(taskItem.taskId, taskItem.id, entrance.number))
        }
    }

    suspend fun savePhoto(
        entrance: EntranceNumber,
        taskItem: TaskItem,
        uuid: UUID,
        location: Location?,
        isEntrancePhoto: Boolean,
        allTaskItemsIds: List<TaskItemWithTaskIds>
    ): EntrancePhoto {
        val photo = databaseRepository.savePhoto(entrance, taskItem, uuid, location, isEntrancePhoto)
        if (isEntrancePhoto) {
            allTaskItemsIds.forEach {
                if (it.taskItemId != taskItem.id) {
                    databaseRepository.savePhoto(
                        entrance,
                        it.taskId,
                        it.taskItemId,
                        taskItem.address.idnd,
                        uuid,
                        location,
                        isEntrancePhoto,
                        pathsProvider.getEntrancePhotoFileByID(taskItem.id, entrance, photo.UUID).path
                    )
                }
            }
        }
        return photo
    }
}