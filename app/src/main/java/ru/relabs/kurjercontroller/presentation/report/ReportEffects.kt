package ru.relabs.kurjercontroller.presentation.report

import androidx.core.net.toUri
import com.google.firebase.crashlytics.FirebaseCrashlytics
import ru.relabs.kurjercontroller.domain.models.Entrance
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.utils.Left
import ru.relabs.kurjercontroller.utils.Right
import java.util.*

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */
object ReportEffects {

    fun effectLoadSavedData(task: Task, taskItem: TaskItem, entrance: Entrance): ReportEffect = { c, s ->
        messages.send(ReportMessages.msgAddLoaders(1))
        val savedEntrance = c.databaseRepository.getEntranceResult(taskItem, entrance)
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
}