package ru.relabs.kurjercontroller.presentation.report

import android.graphics.Bitmap
import android.net.Uri
import ru.relabs.kurjercontroller.data.database.entities.EntranceResultEntity
import ru.relabs.kurjercontroller.domain.models.*
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffect
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffects
import ru.relabs.kurjercontroller.presentation.base.tea.msgState
import ru.relabs.kurjercontroller.presentation.reportPager.TaskItemWithTaskIds
import java.io.File
import java.util.*

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */

object ReportMessages {
    fun msgInit(
        taskItem: TaskItem,
        entrance: Entrance,
        allTaskItems: List<TaskItemWithTaskIds>
    ): ReportMessage = msgEffects(
        {
            it.copy(
                taskItem = taskItem,
                entrance = entrance,
                allTaskItemsIds = allTaskItems
            )
        },
        { listOf(ReportEffects.effectLoadSavedData(taskItem, entrance)) }
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
                selectedKey = savedEntrance.key?.takeIf { it.isNotBlank() }
                    ?: entranceKeys.firstOrNull { it == "Нет" }
                    ?: entranceKeys.firstOrNull()
                    ?: "",
                entranceEuroKeys = entranceEuroKeys,
                selectedEuroKey = savedEntrance.euroKey?.takeIf { it.isNotBlank() }
                    ?: entranceEuroKeys.firstOrNull { it == "Нет" }
                    ?: entranceEuroKeys.firstOrNull()
                    ?: "",
                selectedEntrancePhotos = photos
            )
        }

    fun msgPhotoClicked(multiple: Boolean, isEntrancePhoto: Boolean): ReportMessage =
        msgEffect(ReportEffects.effectCreatePhoto(multiple, isEntrancePhoto))

    fun msgRemovePhotoClicked(removedPhoto: EntrancePhoto): ReportMessage = msgEffects(
        { s ->
            s.copy(
                selectedEntrancePhotos = if (s.entrance?.state == EntranceState.CREATED && s.saved?.entranceClosed != true) {
                    s.selectedEntrancePhotos.filter { photo -> photo.photo != removedPhoto }
                } else {
                    s.selectedEntrancePhotos
                }
            )
        },
        { listOf(ReportEffects.effectRemovePhoto(removedPhoto)) }
    )

    fun msgNewPhoto(newPhoto: PhotoWithUri): ReportMessage =
        msgState { it.copy(selectedEntrancePhotos = it.selectedEntrancePhotos + listOf(newPhoto)) }

    fun msgPhotoError(errorCode: Int): ReportMessage =
        msgEffect(ReportEffects.effectShowPhotoError(errorCode))

    fun msgPhotoCaptured(
        entrance: EntranceNumber,
        multiplePhoto: Boolean,
        photoUri: Uri,
        targetFile: File,
        uuid: UUID,
        isEntrancePhoto: Boolean
    ): ReportMessage =
        msgEffects(
            { it },
            {
                listOfNotNull(
                    ReportEffects.effectSavePhotoFromFile(entrance, photoUri, targetFile, uuid, isEntrancePhoto),
                    ReportEffects.effectCreatePhoto(multiplePhoto, isEntrancePhoto).takeIf { multiplePhoto }
                )
            }
        )

    fun msgApartmentStateChanged(apartmentNumber: ApartmentNumber, newState: Int, buttonsMode: ReportApartmentButtonsMode): ReportMessage =
        msgEffect(ReportEffects.effectChangeApartmentState(apartmentNumber, newState, buttonsMode))

    fun msgAllApartmentStateChanged(apartmentNumber: ApartmentNumber, newState: Int, buttonsMode: ReportApartmentButtonsMode): ReportMessage =
        msgEffect(ReportEffects.effectChangeAllApartmentState(apartmentNumber, newState, buttonsMode))

    fun msgApartmentDescriptionClicked(apartmentNumber: ApartmentNumber): ReportMessage =
        msgEffect(ReportEffects.effectShowDescriptionInput(apartmentNumber))

    fun msgApartmentDescriptionChanged(apartmentNumber: ApartmentNumber, description: String): ReportMessage =
        msgEffect(ReportEffects.effectChangeApartmentDescription(apartmentNumber, description))

    private fun msgUpdateSavedAndSave(
        saveForSameEntrances: Boolean = false,
        mapper: (ReportState) -> ReportState
    ): ReportMessage =
        msgEffects(
            { mapper(it) },
            {
                listOfNotNull(
                    if (!saveForSameEntrances) {
                        it.saved?.let { ReportEffects.effectSaveEntranceChanges(it) }
                    } else {
                        ReportEffects.effectSaveEntranceChangesExternal(it, mapper)
                    }
                )
            }
        )

    fun msgApartmentsFromChanged(startApartment: Int?): ReportMessage = msgUpdateSavedAndSave(true) { s ->
        s.copy(
            saved = s.saved?.copy(
                apartmentFrom = startApartment
                    ?.takeIf { it < (s.saved.apartmentTo ?: s.entrance?.endApartments ?: 0) }
                    ?: s.saved.apartmentFrom
            )
        )
    }

    fun msgApartmentsToChanged(endApartment: Int?): ReportMessage = msgUpdateSavedAndSave(true) { s ->
        s.copy(
            saved = s.saved?.copy(
                apartmentTo = endApartment
                    ?.takeIf { it > (s.saved.apartmentFrom ?: s.entrance?.startApartments ?: 0) }
                    ?: s.saved.apartmentTo
            )
        )
    }

    fun msgCodeChanged(code: String): ReportMessage = msgUpdateSavedAndSave {
        it.copy(saved = it.saved?.copy(code = code))
    }

    fun msgFloorsChanged(floors: Int): ReportMessage = msgUpdateSavedAndSave {
        it.copy(saved = it.saved?.copy(floors = floors))
    }

    fun msgDescriptionChanged(description: String): ReportMessage = msgUpdateSavedAndSave {
        it.copy(saved = it.saved?.copy(description = description))
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

    fun msgChangeApartmentButtonGroup(apartment: ApartmentNumber, newButtonGroup: ReportApartmentButtonsMode): ReportMessage =
        msgState { s ->
            if (s.taskItem != null && s.entrance != null) {
                s.copy(
                    savedApartments = if (s.savedApartments.firstOrNull { it.apartmentNumber == apartment } != null) {
                        s.savedApartments.map {
                            if (it.apartmentNumber == apartment) {
                                it.copy(buttonGroup = newButtonGroup)
                            } else {
                                it
                            }
                        }
                    } else {
                        s.savedApartments + listOf(
                            ApartmentResult.empty(
                                s.taskItem,
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

    fun msgCloseEntranceClicked(): ReportMessage =
        msgEffect(ReportEffects.effectCloseCheck(true))

    fun msgCloseEntrance(): ReportMessage = msgEffects(
        { it.copy(entrance = it.entrance?.copy(state = EntranceState.CLOSED)) },
        {
            listOf(ReportEffects.effectCloseEntrance(true))
        }
    )

    fun msgDataReloaded(
        taskItem: TaskItem?,
        entrance: EntranceResultEntity?,
        apartments: List<ApartmentResult>,
        photos: List<PhotoWithUri>
    ): ReportMessage =
        msgState {
            it.copy(
                taskItem = taskItem ?: it.taskItem,
                saved = entrance ?: it.saved,
                savedApartments = apartments.takeIf { it.isNotEmpty() } ?: it.savedApartments,
                selectedEntrancePhotos = photos
            )
        }


    fun msgGPSLoading(enabled: Boolean): ReportMessage =
        msgState { it.copy(isGPSLoading = enabled) }
}