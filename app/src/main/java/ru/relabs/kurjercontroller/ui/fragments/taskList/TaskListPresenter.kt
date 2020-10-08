package ru.relabs.kurjercontroller.ui.fragments.taskList

import kotlinx.android.synthetic.main.fragment_tasklist.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.utils.CancelableScope
import ru.relabs.kurjercontroller.utils.CustomLog
import ru.relabs.kurjercontroller.activity
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.domain.models.TaskFiltersModel
import ru.relabs.kurjercontroller.domain.models.TaskModel
import ru.relabs.kurjercontroller.network.DeliveryServerAPI
import ru.relabs.kurjercontroller.ui.activities.ErrorButtonsListener
import ru.relabs.kurjercontroller.ui.activities.showError
import ru.relabs.kurjercontroller.ui.activities.showErrorAsync
import ru.relabs.kurjercontroller.ui.activities.showErrorSuspend
import ru.relabs.kurjercontroller.ui.fragments.AddressListScreen
import ru.relabs.kurjercontroller.ui.fragments.FiltersScreen
import ru.relabs.kurjercontroller.ui.fragments.OnlineFiltersScreen
import ru.relabs.kurjercontroller.ui.fragments.TaskInfoScreen
import ru.relabs.kurjercontroller.ui.fragments.taskList.holders.TaskHolder

class TaskListPresenter(val fragment: TaskListFragment) {
    val bgScope = CancelableScope(Dispatchers.Default)
    var networkUpdateStarted = false

    fun onTaskClicked(pos: Int) {
        val item = fragment.adapter.data[pos] as? TaskListModel.TaskItem
        item ?: return
        application().router.navigateTo(TaskInfoScreen(item.task))
    }

    fun onTaskSelected(pos: Int) {
        if (pos < 0) {
            return
        }
        if (fragment.adapter.data[pos] !is TaskListModel.TaskItem) {
            return
        }
        if ((fragment.adapter.data[pos] as TaskListModel.TaskItem).task.androidState == TaskModel.CREATED) {
            fragment.context?.showError("Вы должны ознакомиться с заданием")
            return
        }
        val clickedItem = (fragment.adapter.data[pos] as TaskListModel.TaskItem)
        val selectedTasks = getSelectedTasks()
        val selectedFilteredTasksCount = selectedTasks.count { it.task.filtered }
        if (selectedFilteredTasksCount >= 3 && !clickedItem.selected && clickedItem.task.filtered) {
            fragment.context?.showError("Невозможно выбрать более 3 заданий с фильтрами")
            return
        }

        clickedItem.selected = !clickedItem.selected
        fragment.adapter.notifyItemChanged(pos)

        markIntersectedTasks()
        updateStartButton()
    }

    private fun getSelectedTasks(): List<TaskListModel.TaskItem> =
        fragment.adapter.data
            .filter { it is TaskListModel.TaskItem && it.selected }
            .mapNotNull { it as? TaskListModel.TaskItem }

    private fun markIntersectedTasks() {
        val tasks = fragment.adapter.data
        val selectedTasks = getSelectedTasks()
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
                (fragment.adapter.data[i] as TaskListModel.TaskItem).hasAddressIntersection =
                    newStates[i]
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
        val selectedTasks = getSelectedTasks().map { it.task }

        val selectedFilteredTasks = selectedTasks.filter { it.filtered }
        if (selectedFilteredTasks.isNotEmpty()) {
            val token = application().user.getUserCredentials()?.token
            if (token == null) {
                fragment.context?.showError("Что-то пошло не так. Ошибка: token_not_found")
                return
            }

            application().router.navigateTo(FiltersScreen(selectedFilteredTasks) {
                fragment.showLoading(true, text = "Загрузка адресов")
                bgScope.launch {
                    val notLoadedTasks = mutableListOf<TaskModel>()
                    selectedFilteredTasks.forEach {
                        val reloadedDbTask =
                            application().tasksRepository.getTask(it.id) ?: return@forEach
                        try {
                            application().tasksRepository.reloadFilteredTaskItems(
                                token,
                                reloadedDbTask
                            )
                        } catch (e: Exception) {
                            if (it.taskItems.isEmpty()) {
                                notLoadedTasks.add(it)
                            }
                        }
                    }

                    if (notLoadedTasks.isNotEmpty()) {
                        if (notLoadedTasks.size == selectedTasks.size) {
                            fragment.context?.showErrorAsync(
                                "Не удалось загрузить адреса",
                                object : ErrorButtonsListener {
                                    override fun positiveListener() {
                                        fragment.showLoading(false)
                                    }
                                })
                            return@launch
                        } else {
                            fragment.context?.showErrorAsync(
                                "Не удалось загрузить адреса для заданий:\n${notLoadedTasks.joinToString { it.name + "\n" }}\nЗадания были исключены"
                            )
                        }
                    }

                    fragment.showLoadingAsync(false)
                    withContext(Dispatchers.Main) {
                        application().router.navigateTo(AddressListScreen(selectedTasks
                            .filter { selected ->
                                notLoadedTasks.none { notLoaded ->
                                    notLoaded.id == selected.id
                                }
                            }
                            .map { it.id })
                        )
                    }
                }
            })
        } else {
            application().router.navigateTo(AddressListScreen(selectedTasks.map { it.id }))
        }

    }

    suspend fun startOnline() = withContext(Dispatchers.Main) {
        val exists = application().tasksRepository.isOnlineTaskExists()
        if (exists) {
            val idx = fragment.adapter.data.indexOfFirst {
                (it is TaskListModel.TaskItem) && it.task.isOnline
            }
            val holderView =
                fragment.tasks_list?.findViewHolderForAdapterPosition(idx) as? TaskHolder
            holderView?.setSelected()
            updateStartButton()
            return@withContext
        }
        val token = application().user.getUserCredentials()?.token
        if (token == null) {
            fragment.context?.showError("Произошла ошибка")
            return@withContext
        }

        application().router.navigateTo(
            OnlineFiltersScreen { filters, withPlanned ->
                application().router.exit()
                onOnlineFiltersReceived(filters, withPlanned, token)
            }
        )
    }

    fun onOnlineClicked() {
        fragment.online_button?.isEnabled = false
        val token = application().user.getUserCredentials()?.token ?: return
        bgScope.launch(Dispatchers.IO) {
            val hasAccess = try {
                DeliveryServerAPI.api.hasOnlineAccess(token).await().status
            } catch (e: Exception) {
                fragment.activity()?.showErrorAsync("Не удалось проверить права.")
                return@launch
            } finally {
                withContext(Dispatchers.Main) {
                    fragment.online_button?.isEnabled = true
                }
            }

            if (!hasAccess) {
                fragment.activity()?.showErrorAsync("У вас нет прав на составление заданий.")
            } else {
                startOnline()
            }
        }
    }

    fun onOnlineFiltersReceived(filters: TaskFiltersModel, withPlanned: Boolean, token: String) {
        fragment.showLoading(true, text = "Загрузка адресов")
        bgScope.launch {
            val task = application().tasksRepository.createOnlineTask(filters, withPlanned)
            val newTask = try {
                application().tasksRepository.reloadFilteredTaskItems(token, task)
            } catch (e: java.lang.Exception) {
                fragment.context?.showErrorSuspend("Не удалось загрузить список адресов.")
                return@launch
            }

            withContext(Dispatchers.Main) {
                fragment.showLoading(false)
                application().router.navigateTo(AddressListScreen(listOf(newTask.id)))
            }
        }
    }

    suspend fun removeOutdatedOnlineTask() = withContext(Dispatchers.IO) {
        val rep = application().tasksRepository
        rep.getOnlineTask()?.let {
            if (it.endControlDate.plusHours(1) < DateTime().apply {
                    minusMillis(millisOfDay)
                }) {
                rep.closeTaskById(it.id)
            }
        }
    }

    suspend fun loadTasks() = withContext(Dispatchers.IO) {
        removeOutdatedOnlineTask()

        fragment.populateTaskList(application().tasksRepository.getTasks().filter {
            it.state.toAndroidState() != TaskModel.COMPLETED && it.state.toAndroidState() != TaskModel.CANCELED
        })

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

        fragment.showLoadingAsync(true, true, "Загрузка заданий")

        try {
            application().tasksRepository.getAvailableEntranceKeys(user.token, true)
            application().tasksRepository.getAvailableEntranceEuroKeys(user.token, true)
        } catch (e: java.lang.Exception) {
            CustomLog.writeToFile(CustomLog.getStacktraceAsString(e))
        }

        networkUpdateStarted = true
        val tasks = try {
            application().tasksRepository.loadRemoteTasks(user.token)
        } catch (e: Exception) {
            fragment.context?.showErrorSuspend("Не удалось получить список заданий.")
            networkUpdateStarted = false
            fragment.showLoadingAsync(false)
            return@withContext
        }
        val mergeResult = application().tasksRepository.mergeTasks(tasks)
        fragment.showLoadingAsync(false)
        networkUpdateStarted = false
        loadTasks()

        when {
            mergeResult.isTasksChanged -> fragment.context?.showErrorSuspend("Задания были обновлены.")
            mergeResult.isNewTasksAdded -> fragment.context?.showErrorSuspend("Обновление прошло успешно.")
            else -> fragment.context?.showErrorSuspend("Нет новых заданий.")
        }
    }

}
