package ru.relabs.kurjercontroller.presentation.fragmentsOld.taskInfo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.utils.CancelableScope
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.presentation.splash.showError
import ru.relabs.kurjercontroller.presentation.fragmentsOld.AddressYandexMapScreen
import ru.relabs.kurjercontroller.presentation.fragmentsOld.TaskItemExplanationScreen
import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.AddressWithColor
import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.base.WRONG_METHOD_OUTLINE_COLOR

class TaskInfoPresenter(val fragment: TaskInfoFragment) {
    val bgScope = CancelableScope(Dispatchers.Default)

    fun onInfoClicked(taskItem: TaskItem) {
        application().router.navigateTo(TaskItemExplanationScreen(taskItem))
    }

    fun onExamineClicked() {
        bgScope.launch {
            application().tasksRepository.examineTaskStatus(fragment.task)
            withContext(Dispatchers.Main) {
                application().router.exit()
            }
        }
    }

    fun onShowMapClicked() {
        if (fragment.task.taskItems.isEmpty()) {
            fragment.context?.showError("Адреса не загружены")
            return
        }
        application().router.navigateTo(
            AddressYandexMapScreen(
                fragment.task.taskItems.map { AddressWithColor(it.address, it.placemarkColor, if(it.wrongMethod) WRONG_METHOD_OUTLINE_COLOR else it.placemarkColor) },
                fragment.task.taskItems.map { it.deliverymanId },
                fragment.task.storages
            ) { address ->
                fragment.targetAddress = address
            }
        )
    }
}
