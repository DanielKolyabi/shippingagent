package ru.relabs.kurjercontroller.ui.fragments.taskList

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.CancelableScope
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.models.TaskModel
import ru.relabs.kurjercontroller.ui.fragments.FiltersScreen
import ru.relabs.kurjercontroller.ui.fragments.TaskInfoScreen

class TaskListPresenter(val fragment: TaskListFragment) {
    val bgScope = CancelableScope(Dispatchers.Default)

    fun onTaskClicked(pos: Int) {
        val item = fragment.adapter.data[pos] as? TaskListModel.TaskItem
        item ?: return
        application().router.navigateTo(TaskInfoScreen(item.task))
    }

    fun onTaskSelected(pos: Int) {
        if (fragment.adapter.data[pos] !is TaskListModel.TaskItem) {
            return
        }
        (fragment.adapter.data[pos] as TaskListModel.TaskItem).selected =
            !(fragment.adapter.data[pos] as TaskListModel.TaskItem).selected
        fragment.adapter.notifyItemChanged(pos)

        markIntersectedTasks()
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
            if (task2.taskItems.find { it.address.id == taskItem.address.id } != null) {
                return true
            }
        }
        return false
    }

    fun onStartClicked() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun onOnlineClicked() {
        bgScope.launch {
            val taskFilters = application().tasksLocalRepository.getTask(15).taskFilters
            withContext(Dispatchers.Main) {
                application().router.navigateTo(FiltersScreen(fragment, taskFilters))
            }
        }
    }

    suspend fun loadTasks() = withContext(Dispatchers.IO) {
        fragment.showLoading(true)
        fragment.populateTaskList(application().tasksLocalRepository.getTasks())
        fragment.showLoading(false)
    }

    suspend fun performNetworkUpdate() = withContext(Dispatchers.IO) {
        //TODO: Network update

        loadTasks()
    }
}
