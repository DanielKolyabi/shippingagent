package ru.relabs.kurjercontroller.ui.fragments.taskInfo

import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.models.TaskItemModel
import ru.relabs.kurjercontroller.ui.fragments.TaskItemExplanationScreen

class TaskInfoPresenter(val fragment: TaskInfoFragment) {
    fun onInfoClicked(taskItem: TaskItemModel) {
        application().router.navigateTo(TaskItemExplanationScreen(taskItem))
    }
}
