package ru.relabs.kurjercontroller.ui.fragments.taskList

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.relabs.kurjercontroller.CancelableScope
import ru.relabs.kurjercontroller.application

class TaskListPresenter(val fragment: TaskListFragment) {
    val bgScope = CancelableScope(Dispatchers.Default)

    fun onTaskClicked() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun onTaskSelected() {
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
        //TODO: Network update
        fragment.populateTaskList(application().tasksLocalRepository.getTasks())
        fragment.showLoading(false)
    }
}
