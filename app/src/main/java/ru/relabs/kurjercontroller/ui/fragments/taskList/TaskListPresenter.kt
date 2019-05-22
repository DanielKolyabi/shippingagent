package ru.relabs.kurjercontroller.ui.fragments.taskList

import android.util.Log
import kotlinx.android.synthetic.main.fragment_tasklist.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.CancelableScope
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.models.TaskFiltersModel
import ru.relabs.kurjercontroller.models.TaskModel
import ru.relabs.kurjercontroller.models.toAndroidState
import ru.relabs.kurjercontroller.ui.activities.showError
import ru.relabs.kurjercontroller.ui.activities.showErrorSuspend
import ru.relabs.kurjercontroller.ui.fragments.AddressListScreen
import ru.relabs.kurjercontroller.ui.fragments.FiltersScreen
import ru.relabs.kurjercontroller.ui.fragments.TaskInfoScreen

class TaskListPresenter(val fragment: TaskListFragment) {
    val bgScope = CancelableScope(Dispatchers.Default)
    var networkUpdateStarted = false

    fun onTaskClicked(pos: Int) {
        val item = fragment.adapter.data[pos] as? TaskListModel.TaskItem
        item ?: return
        application().router.navigateTo(TaskInfoScreen(item.task))
    }

    fun onTaskSelected(pos: Int) {
        if (fragment.adapter.data[pos] !is TaskListModel.TaskItem) {
            return
        }
        if ((fragment.adapter.data[pos] as TaskListModel.TaskItem).task.androidState == TaskModel.CREATED) {
            fragment.context?.showError("Вы должны ознакомиться с заданием")
            return
        }

        (fragment.adapter.data[pos] as TaskListModel.TaskItem).selected =
            !(fragment.adapter.data[pos] as TaskListModel.TaskItem).selected
        fragment.adapter.notifyItemChanged(pos)

        markIntersectedTasks()
        updateStartButton()
    }

    private fun markIntersectedTasks() {
        val tasks = fragment.adapter.data
        val selectedTasks = tasks.filter { it is TaskListModel.TaskItem && it.selected }
        val oldStates = tasks.map {
            if (it !is TaskListModel.TaskItem) false
            else it.hasAddressIntersection
        }

        val newStates = oldStates.map { false }.toMutableList()

        for (selectedTask in selectedTasks) {
            for ((i, task) in tasks.withIndex()) {
                if (task == selectedTask) continue
                if (task !is TaskListModel.TaskItem) continue
                if (selectedTask !is TaskListModel.TaskItem) continue
                if (newStates[i]) continue

                if (isTasksHasIntersectedAddresses(selectedTask.task, task.task)) {
                    newStates[i] = true
                }
            }
        }

        oldStates.forEachIndexed { i, state ->
            if (state != newStates[i]) {
                (fragment.adapter.data[i] as TaskListModel.TaskItem).hasAddressIntersection = newStates[i]
                fragment.adapter.notifyItemChanged(i)
            }
        }
    }

    private fun isTasksHasIntersectedAddresses(task1: TaskModel, task2: TaskModel): Boolean {
        for (taskItem in task1.taskItems) {
            if (task2.taskItems.any { it.address.idnd == taskItem.address.idnd }) {
                return true
            }
        }
        return false
    }

    fun updateStartButton() {
        val isSelected = fragment.adapter.data.any {
            (it as? TaskListModel.TaskItem)?.selected ?: false
        }
        fragment.start_button?.isEnabled = isSelected
    }

    fun onStartClicked() {
        val selectedTasks = fragment.adapter.data.filter {
            (it as? TaskListModel.TaskItem)?.selected ?: false
        }.mapNotNull {
            (it as? TaskListModel.TaskItem)?.task
        }

        val selectedFilteredTasks = selectedTasks.filter { it.filtered }
        if (selectedFilteredTasks.isNotEmpty()) {
            application().router.navigateTo(FiltersScreen(selectedFilteredTasks) {
                //TODO: Reload TaskItems
                //Загруженные TaskItems отправить в базу данных
                //Если загрузить не удалось:
                //--Если в базе есть - взять из неё
                //--Если нет - уведомить пользователя о невозможности получить данные и исключить задание
                //----Если были исключены все задания - уведомить пользователя и оставить на текущем экране
                application().router.replaceScreen(AddressListScreen(selectedTasks.map { it.id }))
            })
        } else {
            application().router.navigateTo(AddressListScreen(selectedTasks.map { it.id }))
        }

    }

    fun onOnlineClicked() {
        bgScope.launch {
            withContext(Dispatchers.Main) {
                application().router.navigateTo(
                    FiltersScreen(
                        listOf(
                            TaskModel(
                                -1,
                                -1,
                                "",
                                DateTime(),
                                DateTime(),
                                "",
                                listOf(),
                                listOf(),
                                listOf(),
                                TaskFiltersModel.blank(),
                                0,
                                0,
                                null,
                                true
                            )
                        )
                    ) {
                        Log.d("Filters", "Applied")
                    }
                )
            }
        }
    }

    suspend fun loadTasks() = withContext(Dispatchers.IO) {
        fragment.showLoading(true)
        fragment.populateTaskList(application().tasksRepository.getTasks().filter {
            it.state.toAndroidState() != TaskModel.COMPLETED && it.state.toAndroidState() != TaskModel.CANCELED
        })
        fragment.showLoading(false)

        withContext(Dispatchers.Main) { updateStartButton() }
    }

    suspend fun performNetworkUpdate() = withContext(Dispatchers.IO) {

        val user = application().user.getUserCredentials()
        if (user == null) {
            fragment.context?.showErrorSuspend("Что-то пошло не так. Перезагрузите приложение.")
            return@withContext
        }
        if (networkUpdateStarted) {
            return@withContext
        }

        fragment.showLoading(true, true)

        application().tasksRepository.getAvailableEntranceKeys(user.token, true)
        application().tasksRepository.getAvailableEntranceEuroKeys(user.token, true)

        networkUpdateStarted = true
        val tasks = try {
            application().tasksRepository.loadRemoteTasks(user.token)
        } catch (e: Exception) {
            fragment.context?.showErrorSuspend("Не удалось получить список заданий.")
            networkUpdateStarted = false
            return@withContext
        }
        val mergeResult = application().tasksRepository.mergeTasks(tasks)
        fragment.showLoading(false)
        networkUpdateStarted = false
        loadTasks()

        when {
            mergeResult.isTasksChanged -> fragment.context?.showErrorSuspend("Задания были обновлены.")
            mergeResult.isNewTasksAdded -> fragment.context?.showErrorSuspend("Обновление прошло успешно.")
            else -> fragment.context?.showErrorSuspend("Нет новых заданий.")
        }
    }

}
