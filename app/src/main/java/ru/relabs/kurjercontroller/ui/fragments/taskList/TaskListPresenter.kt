package ru.relabs.kurjercontroller.ui.fragments.taskList

import kotlinx.android.synthetic.main.fragment_tasklist.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.CancelableScope
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.models.TaskModel
import ru.relabs.kurjercontroller.models.toAndroidState
import ru.relabs.kurjercontroller.ui.activities.showError
import ru.relabs.kurjercontroller.ui.fragments.AddressListScreen
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
            if (task2.taskItems.find { it.address?.id == taskItem.address?.id } != null) {
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
            (it as? TaskListModel.TaskItem)?.task?.id
        }

        application().router.navigateTo(AddressListScreen(selectedTasks))
    }

    fun onOnlineClicked() {
//        bgScope.launch {
//            val taskFilters = application().tasksRepository.getTask(15).taskFilters
//            withContext(Dispatchers.Main) {
//                application().router.navigateTo(FiltersScreen(fragment, taskFilters))
//            }
//        }
    }

    suspend fun loadTasks() = withContext(Dispatchers.IO) {
        fragment.showLoading(true)
        fragment.populateTaskList(application().tasksRepository.getTasks().filter{
            it.state.toAndroidState() != TaskModel.COMPLETED && it.state.toAndroidState() != TaskModel.CANCELED
        })
        fragment.showLoading(false)

        withContext(Dispatchers.Main) { updateStartButton() }
    }

    suspend fun performNetworkUpdate() = withContext(Dispatchers.IO) {
        val user = application().user.getUserCredentials()
        if (user == null) {
            fragment.context?.showError("Что-то пошло не так. Перезагрузите приложение.")
            return@withContext
        }
        if (networkUpdateStarted) {
            return@withContext
        }

        fragment.showLoading(true, true)
        networkUpdateStarted = true
        val tasks = try {
            application().tasksRepository.loadRemoteTasks(user.token)
        } catch (e: Exception) {
            fragment.context?.showError("Не удалось получить список заданий.")
            networkUpdateStarted = false
            return@withContext
        }
        val mergeResult = application().tasksRepository.mergeTasks(tasks)
        fragment.showLoading(false)
        networkUpdateStarted = false
        loadTasks()

        if (mergeResult.isTasksChanged) {
            fragment.context?.showError("Задания были обновлены.")
        } else if (mergeResult.isNewTasksAdded) {
            fragment.context?.showError("Обновление прошло успешно.")
        } else {
            fragment.context?.showError("Нет новых заданий.")
        }
    }

}
