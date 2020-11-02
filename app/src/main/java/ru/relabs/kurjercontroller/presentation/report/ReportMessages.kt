package ru.relabs.kurjercontroller.presentation.report

import android.graphics.Bitmap
import ru.relabs.kurjercontroller.data.database.entities.EntranceResultEntity
import ru.relabs.kurjercontroller.domain.models.*
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffect
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffects
import ru.relabs.kurjercontroller.presentation.base.tea.msgEmpty
import ru.relabs.kurjercontroller.presentation.base.tea.msgState
import ru.relabs.kurjercontroller.presentation.reportPager.ReportTaskWithItem
import java.io.File
import java.util.*

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */

object ReportMessages {
    fun msgInit(
        task: Task,
        taskItem: TaskItem,
        entrance: Entrance
    ): ReportMessage = msgEffects(
        {
            it.copy(
                task = ReportTaskWithItem(task, taskItem),
                entrance = entrance,
                defaultReportType = taskItem.defaultReportType
            )
        },
        { listOf(ReportEffects.effectLoadSavedData(task, taskItem, entrance)) }
    )

    fun msgAddLoaders(i: Int): ReportMessage =
        msgState { it.copy(loaders = it.loaders + i) }

    fun msgDataLoaded(
        savedEntrance: EntranceResultEntity,
        savedApartments: List<ApartmentResult>,
        entranceEuroKeys: List<String>,
        entranceKeys: List<String>,
        photos: List<PhotoWithUri>
    ): ReportMessage =
        msgState {
            it.copy(
                saved = savedEntrance,
                savedApartments = savedApartments,
                entranceKeys = entranceKeys,
                selectedKey = savedEntrance?.key ?: entranceKeys.firstOrNull() ?: "",
                entranceEuroKeys = entranceEuroKeys,
                selectedEuroKey = savedEntrance?.euroKey ?: entranceEuroKeys.firstOrNull() ?: "",
                selectedEntrancePhotos = photos
            )
        }

    fun msgPhotoClicked(): ReportMessage =
        msgEffect(ReportEffects.effectCreatePhoto(false))

    fun msgRemovePhotoClicked(removedPhoto: EntrancePhoto): ReportMessage = msgEffects(
        { state -> state.copy(selectedEntrancePhotos = state.selectedEntrancePhotos.filter { photo -> photo.photo != removedPhoto }) },
        { listOf(ReportEffects.effectRemovePhoto(removedPhoto)) }
    )

    fun msgNewPhoto(newPhoto: PhotoWithUri): ReportMessage =
        msgState { it.copy(selectedEntrancePhotos = it.selectedEntrancePhotos + listOf(newPhoto)) }

    fun msgPhotoError(errorCode: Int): ReportMessage =
        msgEffect(ReportEffects.effectShowPhotoError(errorCode))

    fun msgPhotoCaptured(entrance: EntranceNumber, multiplePhoto: Boolean, targetFile: File, uuid: UUID): ReportMessage =
        msgEffects(
            { it },
            {
                listOfNotNull(
                    ReportEffects.effectSavePhotoFromFile(entrance, targetFile, uuid),
                    ReportEffects.effectCreatePhoto(multiplePhoto).takeIf { multiplePhoto }
                )
            }
        )

    fun msgPhotoCaptured(
        entrance: EntranceNumber,
        multiplePhoto: Boolean,
        bitmap: Bitmap,
        targetFile: File,
        uuid: UUID
    ): ReportMessage = msgEffects(
        { it },
        {
            listOfNotNull(
                ReportEffects.effectSavePhotoFromBitmap(entrance, bitmap, targetFile, uuid),
                ReportEffects.effectCreatePhoto(multiplePhoto).takeIf { multiplePhoto }
            )
        }
    )

    fun msgApartmentStateChanged(apartmentNumber: Int, newState: Int): ReportMessage =
        msgEffect(ReportEffects.effectChangeApartmentState(apartmentNumber, newState))

    fun msgAllApartmentStateChanged(apartmentNumber: Int, newState: Int): ReportMessage =
        msgEffect(ReportEffects.effectChangeAllApartmentState(apartmentNumber, newState))

    fun msgApartmentDescriptionClicked(entranceNumber: Int): ReportMessage =
        msgEmpty()

    private fun msgUpdateSavedAndSave(mapper: (ReportState) -> ReportState): ReportMessage = msgEffects(
        { mapper(it) },
        { listOfNotNull(mapper(it).saved?.let { ReportEffects.effectSaveEntranceChanges(it) }) }
    )

    fun msgApartmentsFromChanged(startApartment: Int?): ReportMessage = msgUpdateSavedAndSave {
        it.copy(saved = it.saved?.copy(apartmentFrom = startApartment ?: it.saved.apartmentFrom))
    }

    fun msgApartmentsToChanged(endApartment: Int?): ReportMessage = msgUpdateSavedAndSave {
        it.copy(saved = it.saved?.copy(apartmentTo = endApartment ?: it.saved.apartmentTo))
    }

    fun msgCodeChanged(code: String): ReportMessage = msgUpdateSavedAndSave {
        it.copy(saved = it.saved?.copy(code = code))
    }

    fun msgFloorsChanged(floors: Int): ReportMessage = msgUpdateSavedAndSave {
        it.copy(saved = it.saved?.copy(floors = floors))
    }

    fun msgKeySelected(key: String): ReportMessage = msgUpdateSavedAndSave {
        it.copy(saved = it.saved?.copy(key = key))
    }

    fun msgEuroKeySelected(euroKey: String): ReportMessage = msgUpdateSavedAndSave {
        it.copy(saved = it.saved?.copy(euroKey = euroKey))
    }

    fun msgLayoutErrorChanged(): ReportMessage = msgUpdateSavedAndSave {
        it.copy(saved = it.saved?.copy(isDeliveryWrong = it.saved.isDeliveryWrong?.not()))
    }

    fun msgLookoutChanged(): ReportMessage = msgUpdateSavedAndSave {
        it.copy(saved = it.saved?.copy(hasLookupPost = it.saved.hasLookupPost?.not()))
    }

    fun msgMailboxTypeChanged(type: Int): ReportMessage = msgUpdateSavedAndSave {
        it.copy(saved = it.saved?.copy(mailboxType = type))
    }

    fun msgEntranceClosedClicked(): ReportMessage = msgUpdateSavedAndSave {
        it.copy(saved = it.saved?.copy(entranceClosed = it.saved.entranceClosed?.not()))
    }

    fun msgChangeApartmentButtonGroup(apartment: Int, newButtonGroup: ReportApartmentButtonsMode): ReportMessage = msgState { s ->
        if (s.task != null && s.entrance != null) {
            s.copy(
                savedApartments = if (s.savedApartments.firstOrNull { it.apartmentNumber.number == apartment } != null) {
                    s.savedApartments.map {
                        if (it.apartmentNumber.number == apartment) {
                            it.copy(buttonGroup = newButtonGroup)
                        } else {
                            it
                        }
                    }
                } else {
                    s.savedApartments + listOf(
                        ApartmentResult.empty(
                            s.task,
                            s.entrance,
                            apartment
                        ).copy(buttonGroup = newButtonGroup)
                    )
                }
            )
        } else {
            s
        }
    }

    fun msgListTypeChanged(): ReportMessage =
        msgEffect(ReportEffects.effectChangeButtonGroup())

    fun msgNavigateBack(): ReportMessage =
        msgEffect(ReportEffects.effectNavigateBack())

    fun msgUpdateApartment(newApartmentResult: ApartmentResult): ReportMessage =
        msgState { s ->
            s.copy(
                savedApartments = if (s.savedApartments.firstOrNull { it.apartmentNumber == newApartmentResult.apartmentNumber } != null) {
                    s.savedApartments.map {
                        if (it.apartmentNumber == newApartmentResult.apartmentNumber) {
                            newApartmentResult
                        } else {
                            it
                        }
                    }
                } else {
                    s.savedApartments + listOf(newApartmentResult)
                }
            )
        }
}