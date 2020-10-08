package ru.relabs.kurjercontroller.presentation.fragments.taskInfo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.utils.CancelableScope
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.domain.models.TaskItemModel
import ru.relabs.kurjercontroller.presentation.activities.showError
import ru.relabs.kurjercontroller.presentation.fragments.AddressYandexMapScreen
import ru.relabs.kurjercontroller.presentation.fragments.TaskItemExplanationScreen
import ru.relabs.kurjercontroller.presentation.fragments.yandexMap.AddressWithColor
import ru.relabs.kurjercontroller.presentation.fragments.yandexMap.base.WRONG_METHOD_OUTLINE_COLOR

class TaskInfoPresenter(val fragment: TaskInfoFragment) {
    val bgScope = CancelableScope(Dispatchers.Default)

    fun onInfoClicked(taskItem: TaskItemModel) {
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
