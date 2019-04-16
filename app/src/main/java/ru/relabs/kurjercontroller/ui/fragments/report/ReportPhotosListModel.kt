package ru.relabs.kurjercontroller.ui.fragments.report

import ru.relabs.kurjercontroller.models.EntrancePhotoModel

/**
 * Created by ProOrange on 30.08.2018.
 */

sealed class ReportPhotosListModel {
    object BlankPhoto : ReportPhotosListModel()
    object BlankMultiPhoto : ReportPhotosListModel()
    data class TaskItemPhoto(val photo: EntrancePhotoModel) : ReportPhotosListModel()
}