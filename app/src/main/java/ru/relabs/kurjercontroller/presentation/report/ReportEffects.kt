package ru.relabs.kurjercontroller.presentation.report

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toUri
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.*
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.data.database.entities.EntranceResultEntity
import ru.relabs.kurjercontroller.domain.controllers.TaskEvent
import ru.relabs.kurjercontroller.domain.models.*
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffect
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffects
import ru.relabs.kurjercontroller.utils.*
import ru.relabs.kurjercontroller.utils.extensions.isLocationExpired
import java.io.File
import java.util.*

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */
object ReportEffects {

    fun effectLoadSavedData(taskItem: TaskItem, entrance: Entrance): ReportEffect = { c, s ->
        messages.send(ReportMessages.msgAddLoaders(1))
        val savedEntrance = c.databaseRepository.getEntranceResult(taskItem, entrance)
            ?: EntranceResultEntity.fromEntrance(taskItem, entrance)
        val savedApartments = c.databaseRepository.getEntranceApartments(taskItem, entrance)
        val entranceEuroKeys = when (val r = c.controlRepository.getAvailableEntranceEuroKeys()) {
            is Left -> {
                FirebaseCrashlytics.getInstance().recordException(r.value)
                listOf("Ошибка")
            }
            is Right -> {
                r.value
            }
        }
        val entranceKeys = when (val r = c.controlRepository.getAvailableEntranceKeys()) {
            is Left -> {
                FirebaseCrashlytics.getInstance().recordException(r.value)
                listOf("Ошибка")
            }
            is Right -> {
                r.value
            }
        }
        val photos = c.databaseRepository.getEntrancePhotos(taskItem, entrance)
            .map { PhotoWithUri(it, it.getFile(c.pathsProvider).toUri()) }

        messages.send(ReportMessages.msgDataLoaded(savedEntrance, savedApartments, entranceEuroKeys, entranceKeys, photos))
        messages.send(ReportMessages.msgAddLoaders(-1))
    }

    fun effectSaveApartmentsChanges(apartment: ApartmentResult): ReportEffect = { c, s ->
        c.databaseRepository.saveApartmentResult(apartment)
    }

    fun effectSaveEntranceChanges(
        entrance: EntranceResultEntity
    ): ReportEffect = { c, s ->
        c.databaseRepository.saveEntranceResult(entrance)
    }

    fun effectSaveEntranceChangesExternal(
        s: ReportState,
        mapper: (ReportState) -> ReportState
    ): ReportEffect = { c, _ ->
        if (s.taskItem != null && s.entrance != null && s.saved != null) {
            s.allTaskItemsIds.forEach {
                val savedResult = c.databaseRepository.getEntranceResult(it.taskId, it.taskItemId, s.entrance.number)
                val entrance = c.databaseRepository.getEntrance(it.taskId, it.taskItemId, s.entrance.number)
                if (savedResult != null && entrance != null) {
                    val fakeState = s.copy(saved = savedResult, entrance = entrance)
                    val mappedFakeState = mapper(fakeState)
                    mappedFakeState.saved?.let {
                        messages.send(msgEffect(effectSaveEntranceChanges(it)))
                    }
                }
            }
        }
    }

    fun effectSaveAllChanges(): ReportEffect = { c, s ->
        s.saved?.let { effectSaveEntranceChanges(it)(c, s) }
        s.savedApartments.forEach {
            effectSaveApartmentsChanges(it)(c, s)
        }
    }

    fun effectNavigateBack(): ReportEffect = { c, s ->
        withContext(Dispatchers.Main) {
            c.router.exit()
        }
    }

    fun effectChangeApartmentDescription(apartmentNumber: ApartmentNumber, description: String): ReportEffect = { c, s ->
        if (s.taskItem != null && s.entrance != null) {
            if (apartmentNumber.number == -1) {
                messages.send(ReportMessages.msgDescriptionChanged(description))
            } else {
                val apartment = s.savedApartments.firstOrNull { apartmentNumber == it.apartmentNumber }
                    ?: ApartmentResult.empty(s.taskItem, s.entrance, apartmentNumber)
                val apartmentWithNewState = apartment.copy(description = description)
                messages.send(ReportMessages.msgUpdateApartment(apartmentWithNewState))
                messages.send(msgEffect(effectSaveApartmentsChanges(apartmentWithNewState)))
            }
        } else {
            FirebaseCrashlytics.getInstance().log("task or entrance is null")
        }
    }

    fun effectChangeApartmentState(
        apartmentNumber: ApartmentNumber,
        newState: Int,
        buttonsMode: ReportApartmentButtonsMode
    ): ReportEffect = { c, s ->
        if (s.taskItem != null && s.entrance != null) {
            if (s.entrance.state == EntranceState.CREATED && s.saved?.entranceClosed != true) {
                val apartment = s.savedApartments.firstOrNull { apartmentNumber == it.apartmentNumber }
                    ?: ApartmentResult.empty(s.taskItem, s.entrance, apartmentNumber)
                val apartmentWithNewState = apartment.copy(buttonState = newState, buttonGroup = buttonsMode)
                messages.send(ReportMessages.msgUpdateApartment(apartmentWithNewState))
                messages.send(msgEffect(effectSaveApartmentsChanges(apartmentWithNewState)))
            }
        } else {
            FirebaseCrashlytics.getInstance().log("task or entrance is null")
        }
    }

    fun effectChangeAllApartmentState(
        apartmentNumber: ApartmentNumber,
        clickedState: Int,
        buttonsMode: ReportApartmentButtonsMode,
        disableOnly: Boolean = false
    ): ReportEffect = { c, s ->
        if (s.taskItem != null && s.entrance != null) {
            if (s.entrance.state == EntranceState.CREATED && s.saved?.entranceClosed != true) {
                val targetApartment = s.savedApartments.firstOrNull { apartmentNumber == it.apartmentNumber }
                    ?: ApartmentResult.empty(s.taskItem, s.entrance, apartmentNumber)
                val targetState = if (!disableOnly) {
                    (targetApartment.buttonState xor clickedState) and clickedState
                } else {
                    targetApartment.buttonState and clickedState.inv()
                }

                val startApartment = s.saved?.apartmentFrom ?: s.entrance.startApartments
                val endApartment = s.saved?.apartmentTo ?: s.entrance.endApartments
                (startApartment..endApartment).forEach {
                    val apartment = s.savedApartments.firstOrNull { a -> it == a.apartmentNumber.number }
                        ?: ApartmentResult.empty(s.taskItem, s.entrance, ApartmentNumber(it)).copy(buttonGroup = buttonsMode)

                    val apartmentWithNewState = if (apartment.buttonState and clickedState != targetState) {
                        when {
                            clickedState == 1 -> apartment.copy(buttonState = (apartment.buttonState and (4 or 64).inv()) xor 1)
                            clickedState == 4 -> apartment.copy(buttonState = (apartment.buttonState and (1 or 64).inv()) xor 4)
                            clickedState == 16 && apartment.buttonState and 32 > 0 -> apartment.copy(buttonState = apartment.buttonState xor 32 xor 16)
                            clickedState == 32 && apartment.buttonState and 16 > 0 -> apartment.copy(buttonState = apartment.buttonState xor 16 xor 32)
                            clickedState == 64 -> apartment.copy(buttonState = (apartment.buttonState and (1 or 4).inv()) xor 64)
                            else -> apartment.copy(buttonState = apartment.buttonState xor clickedState)
                        }
                    } else {
                        apartment
                    }

                    messages.send(ReportMessages.msgUpdateApartment(apartmentWithNewState))
                }

                messages.send(msgEffect(effectSaveAllChanges()))
            }
        } else {
            FirebaseCrashlytics.getInstance().log("task or entrance is null")
        }
    }

    fun effectCreatePhoto(multiplePhotos: Boolean, isEntrancePhoto: Boolean): ReportEffect = { c, s ->
        if (s.taskItem == null || s.entrance == null) {
            c.showError("re:100", true)
        } else {
            if (s.entrance.state == EntranceState.CREATED && s.saved?.entranceClosed != true) {
                val photoUUID = UUID.randomUUID()
                val photoFile = c.pathsProvider.getEntrancePhotoFile(s.taskItem, s.entrance, photoUUID)
                withContext(Dispatchers.Main) {
                    c.requestPhoto(s.entrance.number, multiplePhotos, photoFile, photoUUID, isEntrancePhoto)
                }
            }
        }
    }

    fun effectRemovePhoto(it: EntrancePhoto): ReportEffect = { c, s ->
        if (s.entrance?.state == EntranceState.CREATED && s.saved?.entranceClosed != true) {
            c.databaseRepository.removePhoto(it)
        }
    }

    fun effectShowPhotoError(errorCode: Int): ReportEffect = { c, s ->
        c.showError("Не удалось сделать фотографию: re:photo:$errorCode", false)
    }

    fun effectSavePhotoFromFile(
        entrance: EntranceNumber,
        photoUri: Uri,
        targetFile: File,
        uuid: UUID,
        isEntrancePhoto: Boolean
    ): ReportEffect =
        { c, s ->
            val contentResolver = c.contentResolver()
            if (contentResolver == null) {
                messages.send(msgEffect(effectShowPhotoError(8)))
            } else {
                val bmp = BitmapFactory.decodeStream(contentResolver.openInputStream(photoUri))
                if (bmp == null) {
                    CustomLog.writeToFile("Photo creation failed. Uri: ${photoUri}, File: ${targetFile.path}")
                    messages.send(msgEffect(effectShowPhotoError(7)))
                } else {
                    effectSavePhotoFromBitmap(entrance, bmp, targetFile, uuid, isEntrancePhoto)(c, s)
                }
            }
        }

    fun effectSavePhotoFromBitmap(
        entrance: EntranceNumber,
        bitmap: Bitmap,
        targetFile: File,
        uuid: UUID,
        isEntrancePhoto: Boolean
    ): ReportEffect =
        { c, s ->
            when (val task = s.taskItem) {
                null -> c.showError("re:102", true)
                else -> {
                    when (savePhotoFromBitmapToFile(bitmap, targetFile)) {
                        is Left -> messages.send(ReportMessages.msgPhotoError(6))
                        is Right -> {
                            val location = c.locationProvider.lastReceivedLocation()
                            val photo =
                                c.reportUseCase.savePhoto(entrance, task, uuid, location, isEntrancePhoto, s.allTaskItemsIds)
                            val path = photo.getFile(c.pathsProvider)
                            messages.send(ReportMessages.msgNewPhoto(PhotoWithUri(photo, path.toUri())))
                        }
                    }
                }
            }
        }

    fun effectChangeButtonGroup(): ReportEffect = { c, s ->
        if (s.saved != null || s.entrance != null) {
            val currentButtonGroup = s.savedApartments.firstOrNull { it.apartmentNumber.number > 0 }?.buttonGroup
                ?: s.taskItem?.defaultReportType
                ?: ReportApartmentButtonsMode.Main
            val newButtonGroup = when (currentButtonGroup) {
                ReportApartmentButtonsMode.Main -> ReportApartmentButtonsMode.Additional
                ReportApartmentButtonsMode.Additional -> ReportApartmentButtonsMode.Main
            }
            val startApartment = s.saved?.apartmentFrom ?: s.entrance?.startApartments ?: 0
            val endApartment = s.saved?.apartmentTo ?: s.entrance?.endApartments ?: 0
            (startApartment..endApartment).forEach {
                messages.send(ReportMessages.msgChangeApartmentButtonGroup(ApartmentNumber(it), newButtonGroup))
            }

            s.taskItem?.let {
                c.databaseRepository.effectChangeButtonGroup(it, newButtonGroup)
                c.eventController.send(TaskEvent.TaskItemChanged(s.taskItem.copy(defaultReportType = newButtonGroup)))
            }
        }
    }

    private fun savePhotoFromBitmapToFile(bitmap: Bitmap, targetFile: File): Either<Exception, File> = Either.of {
        val resized = ImageUtils.resizeBitmap(bitmap, 1024f, 768f)
        bitmap.recycle()
        ImageUtils.saveImage(resized, targetFile)
        targetFile
    }

    fun effectShowDescriptionInput(entranceNumber: ApartmentNumber): ReportEffect = { c, s ->
        val currentDescription = if (entranceNumber.number == ENTRANCE_NUMBER_TASK_ITEM) {
            s.saved?.description ?: ""
        } else {
            s.savedApartments.firstOrNull { it.apartmentNumber == entranceNumber }?.description ?: ""
        }
        withContext(Dispatchers.Main) {
            c.showDescriptionInputDialog(
                entranceNumber,
                currentDescription,
                when (entranceNumber.number == ENTRANCE_NUMBER_TASK_ITEM) {
                    true -> s.entrance?.state == EntranceState.CREATED
                    else -> s.entrance?.state == EntranceState.CREATED && s.saved?.entranceClosed != true
                }

            )
        }
    }

    fun effectShowAppsChangedPhotoRequiredError(): ReportEffect = { c, s ->
        c.showErrorMessage(R.string.app_changed_photo_required)
    }

    fun effectShowNotDeterminedPhotoRequiredError(): ReportEffect = { c, s ->
        c.showErrorMessage(R.string.not_determine_photo_required)
    }

    fun effectCloseEntranceClicked(): ReportEffect = { c, s ->
        if (s.saved == null || s.entrance == null) {
            c.showError("re:101", false)
        } else {
            c.showCloseEntranceDialog()
        }
    }

    fun effectCloseEntrance(withRemove: Boolean): ReportEffect = { c, s ->
        if (s.taskItem == null || s.entrance == null) {
            c.showError("re:101", false)
        } else {
            messages.send(ReportMessages.msgAddLoaders(1))
            val coordinates = c.locationProvider.lastReceivedLocation()
            c.reportUseCase.createReport(s.taskItem, s.entrance, coordinates, withRemove)
            messages.send(ReportMessages.msgAddLoaders(-1))
        }
    }

    fun effectCloseCheck(withLocationLoading: Boolean): ReportEffect = { c, s ->
        messages.send(ReportMessages.msgAddLoaders(1))
        val selected = s.saved
        val taskItem = s.taskItem
        if (selected == null || taskItem == null) {
            c.showError("re:106", true)
        } else {
            val startApartmentsChanged =
                selected.apartmentFrom != s.entrance?.startApartments && selected.apartmentFrom != null
            val endApartmentsChanged = selected.apartmentTo != s.entrance?.endApartments && selected.apartmentTo != null
            val isAnyApartmentUndetermined = s.savedApartments.any { it.buttonState and 64 > 0 }
            val isAnyAppsChanged = startApartmentsChanged || endApartmentsChanged
            val appsIntervalPhotoRequired = isAnyAppsChanged && s.selectedEntrancePhotos.none { it.photo.isEntrancePhoto }
            val undeterminedAppPhotoRequired = isAnyApartmentUndetermined && s.selectedEntrancePhotos.isEmpty()

            val location = c.locationProvider.lastReceivedLocation()
            CustomLog.writeToFile(
                "GPS LOG: Close check with location(${location?.latitude}, ${location?.longitude}, " +
                        "${Date(location?.time ?: 0).formatedWithSecs()})"
            )

            if (appsIntervalPhotoRequired) {
                messages.send(msgEffect(effectShowAppsChangedPhotoRequiredError()))
            } else if (undeterminedAppPhotoRequired) {
                messages.send(msgEffect(effectShowNotDeterminedPhotoRequiredError()))
            } else if (withLocationLoading && (location == null || Date(location.time).isLocationExpired())) {
                coroutineScope {
                    messages.send(ReportMessages.msgAddLoaders(1))
                    messages.send(ReportMessages.msgGPSLoading(true))
                    val delayJob = async { delay(c.settingsRepository.closeGpsUpdateTime.close * 1000L) }
                    val gpsJob = async(Dispatchers.Default) {
                        c.locationProvider.updatesChannel().apply {
                            receive()
                            CustomLog.writeToFile("GPS LOG: Received new location")
                            cancel()
                        }
                    }
                    listOf(delayJob, gpsJob).awaitFirst()
                    delayJob.cancel()
                    gpsJob.cancel()
                    CustomLog.writeToFile("GPS LOG: Got force coordinates")
                    messages.send(ReportMessages.msgGPSLoading(false))
                    messages.send(ReportMessages.msgAddLoaders(-1))
                    messages.send(msgEffect(effectCloseCheck(false)))
                }
            } else {
                val distance = location?.let {
                    calculateDistance(
                        location.latitude,
                        location.longitude,
                        taskItem.address.lat,
                        taskItem.address.long
                    )
                } ?: Int.MAX_VALUE.toDouble()

                val shadowClose: Boolean = withContext(Dispatchers.Main) {
                    if (c.settingsRepository.isCloseRadiusRequired) {
                        when {
                            location == null -> {
                                c.showCloseError(R.string.report_close_location_null_error, false)
                                true
                            }
                            distance > taskItem.closeRadius -> {
                                c.showCloseError(R.string.report_close_location_far_error, false)
                                true
                            }
                            else -> {
                                messages.send(msgEffect(effectCloseEntranceClicked()))
                                false
                            }
                        }
                    } else {
                        when {
                            location == null -> {
                                c.showCloseError(R.string.report_close_location_null_warning, true)
                                false
                            }
                            distance > taskItem.closeRadius -> {
                                c.showCloseError(R.string.report_close_location_far_warning, true)
                                false
                            }
                            else -> {
                                messages.send(msgEffect(effectCloseEntranceClicked()))
                                false
                            }
                        }
                    }
                }
                if (shadowClose) {
                    effectCloseEntrance(false)(c, s)
                }
            }
        }
        messages.send(ReportMessages.msgAddLoaders(-1))
    }

    fun effectValidateRadiusAndRequestPhoto(multiple: Boolean, entrancePhoto: Boolean): ReportEffect = { c, s ->
        effectValidatePhotoRadiusAnd({ msgEffect(effectCreatePhoto(multiple, entrancePhoto)) }, false)(c, s)
    }

    fun effectValidateRadiusAndSavePhoto(
        entrance: EntranceNumber,
        photoUri: Uri,
        targetFile: File,
        uuid: UUID,
        isEntrancePhoto: Boolean,
        multiplePhoto: Boolean
    ): ReportEffect = { c, s ->
        effectValidatePhotoRadiusAnd(
            {
                msgEffects(
                    { it },
                    {
                        listOfNotNull(
                            effectSavePhotoFromFile(entrance, photoUri, targetFile, uuid, isEntrancePhoto),
                            effectCreatePhoto(multiplePhoto, isEntrancePhoto).takeIf { multiplePhoto }
                        )
                    }
                )
            },
            withAnyRadiusWarning = true
        )(c, s)
    }

    private fun effectValidatePhotoRadiusAnd(
        msgFactory: () -> ReportMessage,
        withAnyRadiusWarning: Boolean,
        withLocationLoading: Boolean = true
    ): ReportEffect = { c, s ->
        messages.send(ReportMessages.msgAddLoaders(1))
        when (val selected = s.taskItem) {
            null -> c.showError("re:106", true)
            else -> {
                val location = c.locationProvider.lastReceivedLocation()
                val distance = location?.let {
                    calculateDistance(
                        location.latitude,
                        location.longitude,
                        selected.address.lat,
                        selected.address.long
                    )
                } ?: Int.MAX_VALUE.toDouble()

                val locationNotValid = location == null || Date(location.time).isLocationExpired(60 * 1000)
                CustomLog.writeToFile(
                    "Validate photo radius (valid: ${!locationNotValid}): " +
                            "${location?.latitude}, ${location?.longitude}, ${location?.time}, " +
                            "photoAnyDistance: ${!c.settingsRepository.isPhotoRadiusRequired}, " +
                            "allowedDistance: ${s.taskItem.closeRadius}, " +
                            "distance: $distance, " +
                            "targetTaskItem: ${selected.id}"
                )

                if (locationNotValid && withLocationLoading) {
                    coroutineScope {
                        messages.send(ReportMessages.msgAddLoaders(1))
                        messages.send(ReportMessages.msgGPSLoading(true))
                        val delayJob = async { delay(c.settingsRepository.closeGpsUpdateTime.photo * 1000L) }
                        val gpsJob = async(Dispatchers.Default) {
                            c.locationProvider.updatesChannel().apply {
                                receive()
                                cancel()
                            }
                        }
                        listOf(delayJob, gpsJob).awaitFirst()
                        listOf(delayJob, gpsJob).forEach {
                            if (it.isActive) {
                                it.cancel()
                            }
                        }
                        messages.send(ReportMessages.msgGPSLoading(false))
                        messages.send(ReportMessages.msgAddLoaders(-1))
                        messages.send(msgEffect(effectValidatePhotoRadiusAnd(msgFactory, withAnyRadiusWarning, false)))
                    }
                } else {
                    if (!c.settingsRepository.isPhotoRadiusRequired) {
                        if (distance > s.taskItem.closeRadius && withAnyRadiusWarning) {
                            withContext(Dispatchers.Main) {
                                c.showCloseError(R.string.report_close_location_far_warning, false)
                            }
                        }
                        messages.send(msgFactory())
                    } else {
                        when {
                            locationNotValid -> withContext(Dispatchers.Main) {
                                c.showCloseError(R.string.report_close_location_null_error, false)
                            }
                            distance > s.taskItem.closeRadius -> withContext(Dispatchers.Main) {
                                c.showCloseError(R.string.report_close_location_far_error, false)
                            }
                            else ->
                                messages.send(msgFactory())

                        }
                    }
                }
            }
        }
        messages.send(ReportMessages.msgAddLoaders(-1))
    }
}