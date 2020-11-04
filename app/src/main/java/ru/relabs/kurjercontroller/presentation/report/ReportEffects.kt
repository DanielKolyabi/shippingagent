package ru.relabs.kurjercontroller.presentation.report

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.net.toUri
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.data.database.entities.EntranceResultEntity
import ru.relabs.kurjercontroller.domain.models.*
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffect
import ru.relabs.kurjercontroller.utils.Either
import ru.relabs.kurjercontroller.utils.ImageUtils
import ru.relabs.kurjercontroller.utils.Left
import ru.relabs.kurjercontroller.utils.Right
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
            .map {
                PhotoWithUri(it, c.pathsProvider.getEntrancePhotoFile(taskItem, entrance, UUID.fromString(it.UUID)).toUri())
            }

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
        mapper: (ReportState) -> ReportState
    ): ReportEffect = { c, s ->
        if (s.taskItem != null && s.entrance != null && s.saved != null) {
            s.allTaskItemsIds.forEach {
                if (it.taskId == s.taskItem.taskId && it.taskItemId == s.taskItem.id) return@forEach
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

    fun effectChangeApartmentState(apartmentNumber: ApartmentNumber, newState: Int): ReportEffect = { c, s ->
        if (s.taskItem != null && s.entrance != null) {
            val apartment = s.savedApartments.firstOrNull { apartmentNumber == it.apartmentNumber }
                ?: ApartmentResult.empty(s.taskItem, s.entrance, apartmentNumber)
            val apartmentWithNewState = apartment.copy(buttonState = newState)
            messages.send(ReportMessages.msgUpdateApartment(apartmentWithNewState))
            messages.send(msgEffect(effectSaveApartmentsChanges(apartmentWithNewState)))
        } else {
            FirebaseCrashlytics.getInstance().log("task or entrance is null")
        }
    }

    fun effectChangeAllApartmentState(apartmentNumber: ApartmentNumber, newState: Int): ReportEffect = { c, s ->
        if (s.taskItem != null && s.entrance != null) {
            val targetApartment = s.savedApartments.firstOrNull { apartmentNumber == it.apartmentNumber }
                ?: ApartmentResult.empty(s.taskItem, s.entrance, apartmentNumber)
            val targetState = (targetApartment.buttonState xor newState) and newState

            val startApartment = s.saved?.apartmentFrom ?: s.entrance.startApartments
            val endApartment = s.saved?.apartmentTo ?: s.entrance.endApartments
            (startApartment..endApartment).forEach {
                val apartment = s.savedApartments.firstOrNull { a -> it == a.apartmentNumber.number }
                    ?: ApartmentResult.empty(s.taskItem, s.entrance, ApartmentNumber(it))

                val apartmentWithNewState = if (apartment.buttonState and newState != targetState) {
                    when {
                        newState == 1 && apartment.buttonState and 4 > 0 -> apartment.copy(buttonState = apartment.buttonState xor 4 xor 1)
                        newState == 4 && apartment.buttonState and 1 > 0 -> apartment.copy(buttonState = apartment.buttonState xor 1 xor 4)
                        newState == 16 && apartment.buttonState and 32 > 0 -> apartment.copy(buttonState = apartment.buttonState xor 32 xor 16)
                        newState == 32 && apartment.buttonState and 16 > 0 -> apartment.copy(buttonState = apartment.buttonState xor 16 xor 32)
                        else -> apartment.copy(buttonState = apartment.buttonState xor newState)
                    }
                } else {
                    apartment
                }

                messages.send(ReportMessages.msgUpdateApartment(apartmentWithNewState))
            }

            messages.send(msgEffect(effectSaveAllChanges()))
        } else {
            FirebaseCrashlytics.getInstance().log("task or entrance is null")
        }
    }

    fun effectCreatePhoto(multiplePhotos: Boolean): ReportEffect = { c, s ->
        if (s.taskItem == null || s.entrance == null) {
            c.showError("re:100", true)
        } else {
            val photoUUID = UUID.randomUUID()
            val photoFile = c.pathsProvider.getEntrancePhotoFile(s.taskItem, s.entrance, photoUUID)
            withContext(Dispatchers.Main) {
                c.requestPhoto(s.entrance.number, multiplePhotos, photoFile, photoUUID)
            }
        }
    }

    fun effectRemovePhoto(it: EntrancePhoto): ReportEffect = { c, s ->
        c.databaseRepository.removePhoto(it)
    }

    fun effectShowPhotoError(errorCode: Int): ReportEffect = { c, s ->
        c.showError("Не удалось сделать фотографию: re:photo:$errorCode", false)
    }

    fun effectSavePhotoFromFile(entrance: EntranceNumber, targetFile: File, uuid: UUID): ReportEffect = { c, s ->
        val bmp = BitmapFactory.decodeFile(targetFile.path)
        effectSavePhotoFromBitmap(entrance, bmp, targetFile, uuid)(c, s)
    }

    fun effectSavePhotoFromBitmap(entrance: EntranceNumber, bitmap: Bitmap, targetFile: File, uuid: UUID): ReportEffect =
        { c, s ->
            when (val task = s.taskItem) {
                null -> c.showError("re:102", true)
                else -> {
                    when (savePhotoFromBitmapToFile(bitmap, targetFile)) {
                        is Left -> messages.send(ReportMessages.msgPhotoError(6))
                        is Right -> {
                            val location = c.locationProvider.lastReceivedLocation()
                            val photo = c.databaseRepository.savePhoto(entrance, task, uuid, location)
                            val path = c.pathsProvider.getEntrancePhotoFileByID(task.id, entrance, uuid.toString())
                            messages.send(ReportMessages.msgNewPhoto(PhotoWithUri(photo, path.toUri())))
                        }
                    }
                }
            }
        }

    fun effectChangeButtonGroup(): ReportEffect = { c, s ->
        if (s.saved != null || s.entrance != null) {
            val currentButtonGroup =
                s.savedApartments.firstOrNull { it.apartmentNumber.number > 0 }?.buttonGroup ?: s.defaultReportType
            val newButtonGroup = when (currentButtonGroup) {
                ReportApartmentButtonsMode.Main -> ReportApartmentButtonsMode.Additional
                ReportApartmentButtonsMode.Additional -> ReportApartmentButtonsMode.Main
            }
            val startApartment = s.saved?.apartmentFrom ?: s.entrance?.startApartments ?: 0
            val endApartment = s.saved?.apartmentTo ?: s.entrance?.endApartments ?: 0
            (startApartment..endApartment).forEach {
                messages.send(ReportMessages.msgChangeApartmentButtonGroup(ApartmentNumber(it), newButtonGroup))
            }

            messages.send(msgEffect(effectSaveAllChanges()))
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
                currentDescription
            )
        }
    }
}