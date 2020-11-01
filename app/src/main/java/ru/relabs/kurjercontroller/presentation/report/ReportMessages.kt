package ru.relabs.kurjercontroller.presentation.report

import ru.relabs.kurjercontroller.data.database.entities.EntranceResultEntity
import ru.relabs.kurjercontroller.domain.models.*
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffects
import ru.relabs.kurjercontroller.presentation.base.tea.msgState
import ru.relabs.kurjercontroller.presentation.reportPager.ReportTaskWithItem

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
        savedEntrance: EntranceResultEntity?,
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
                entranceEuroKeys =  entranceEuroKeys,
                selectedEuroKey = savedEntrance?.euroKey ?: entranceEuroKeys.firstOrNull() ?: "",
                selectedEntrancePhotos = photos
            )
        }
}