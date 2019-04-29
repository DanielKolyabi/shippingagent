package ru.relabs.kurjercontroller.ui.fragments.taskInfo

import kotlinx.android.synthetic.main.fragment_taskinfo.*
import kotlinx.android.synthetic.main.fragment_tasklist.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.CancelableScope
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.models.TaskItemModel
import ru.relabs.kurjercontroller.ui.fragments.TaskItemExplanationScreen
import ru.relabs.kurjercontroller.ui.fragments.YandexMapScreen

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
        application().router.navigateTo(
            YandexMapScreen(
                fragment.task.taskItems.map { it.address }
            ) { address ->
                fragment.targetAddress = address
            }
        )
    }
}
