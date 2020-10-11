package ru.relabs.kurjercontroller.presentation.fragmentsOld.report.models

import ru.relabs.kurjercontroller.domain.models.EntrancePhotoModel

/**
 * Created by ProOrange on 30.08.2018.
 */

sealed class ReportPhotosListModel {
    object BlankPhoto : ReportPhotosListModel()
    object BlankMultiPhoto : ReportPhotosListModel()
    data class TaskItemPhoto(val photo: EntrancePhotoModel) : ReportPhotosListModel()
}