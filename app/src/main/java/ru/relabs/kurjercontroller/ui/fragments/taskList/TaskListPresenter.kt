package ru.relabs.kurjercontroller.ui.fragments.taskList

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.relabs.kurjercontroller.CancelableScope
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.ui.fragments.TaskInfoScreen

class TaskListPresenter(val fragment: TaskListFragment) {
    val bgScope = CancelableScope(Dispatchers.Default)

    fun onTaskClicked(pos: Int) {
        val item = fragment.adapter.data[pos] as? TaskListModel.TaskItem
        item ?: return
        application().router.navigateTo(TaskInfoScreen(item.task))
    }

    fun onTaskSelected(pos: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun onStartClicked() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun onOnlineClicked() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun performUpdate() = bgScope.launch {
        fragment.showLoading(true)
        fragment.populateTaskList(application().tasksLocalRepository.getTasks())
        fragment.showLoading(false)
    }

    fun performNetworkUpdate() = bgScope.launch {
        //TODO: Network update
    }
}
