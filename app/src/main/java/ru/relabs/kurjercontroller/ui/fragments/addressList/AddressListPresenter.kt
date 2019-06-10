package ru.relabs.kurjercontroller.ui.fragments.addressList

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.CancelableScope
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.models.TaskItemModel
import ru.relabs.kurjercontroller.models.TaskModel
import ru.relabs.kurjercontroller.models.toAndroidState
import ru.relabs.kurjercontroller.ui.activities.ErrorButtonsListener
import ru.relabs.kurjercontroller.ui.activities.showError
import ru.relabs.kurjercontroller.ui.activities.showErrorSuspend
import ru.relabs.kurjercontroller.ui.fragments.AddressYandexMapScreen
import ru.relabs.kurjercontroller.ui.fragments.ReportScreen
import ru.relabs.kurjercontroller.ui.fragments.TasksYandexMapScreen
import ru.relabs.kurjercontroller.ui.fragments.yandexMap.AddressWithColor
import ru.relabs.kurjercontroller.ui.helpers.TaskAddressSorter

/**
 * Created by ProOrange on 18.03.2019.
 */

class AddressListPresenter(val fragment: AddressListFragment) {
    val bgScope = CancelableScope(Dispatchers.Default)
    var sortingMethod = TaskAddressSorter.ALPHABETIC

    fun changeSortingMethod(sorting: Int) {
        sortingMethod = sorting
        if (fragment.tasks.size == 1
            && fragment.tasks.first().filtered
            && sortingMethod == TaskAddressSorter.STANDART
        ) {

            sortingMethod = TaskAddressSorter.CLOSE_TIME
        }
        bgScope.launch(Dispatchers.Default) {
            applySorting()
        }
    }

    fun onTaskItemClicked(clickedTask: AddressListModel.TaskItem) {

        val taskItemsOnAddress = fragment.adapter.data
            .filter {
                (it as? AddressListModel.TaskItem)?.taskItem?.address?.idnd == clickedTask.taskItem.address.idnd
                        && (it as? AddressListModel.TaskItem)?.taskItem?.isClosed == clickedTask.taskItem.isClosed
            }
            .map {
                val task = it as AddressListModel.TaskItem
                Pair(task.parentTask, task.taskItem)
            }

        application().router.navigateTo(
            ReportScreen(
                taskItemsOnAddress,
                clickedTask.taskItem.taskId,
                clickedTask.taskItem.id
            )
        )
    }

    fun onSortingChanged(sortingMethod: Int) {
        bgScope.launch {
            changeSortingMethod(sortingMethod)
        }
    }

    fun onAddressMapClicked(items: List<TaskItemModel>) {
        val item = items.firstOrNull { it.closeTime != null } ?: items.first()

        application().router.navigateTo(
            AddressYandexMapScreen(
                listOf(
                    AddressWithColor(
                        item.address,
                        item.placemarkColor
                    )
                )
            ) {
                return@AddressYandexMapScreen
            })
    }

    fun onCloseTaskClicked() {
        if (fragment.tasks.size != 1) {
            fragment.context?.showError("Что-то пошло не так. Не можем закрыть задание.")
            fragment.updateCloseTaskButtonVisibility()
            return
        }

        bgScope.launch(Dispatchers.IO) {
            fragment.showLoading(true)
            if (fragment.tasks.first().isOnline) {
                application().tasksRepository.closeTaskById(fragment.tasks.first().id)
            } else {
                application().tasksRepository.closeTaskStatus(fragment.tasks.first())
            }
            withContext(Dispatchers.Main) {
                application().router.exit()
            }
        }
    }

    suspend fun applySorting() = withContext(Dispatchers.IO) {
        val items = mutableListOf<AddressListModel.TaskItem>()

        fragment.tasks.forEach { task ->
            items.addAll(
                task.taskItems.map {
                    AddressListModel.TaskItem(it, task)
                }
            )
        }

        val prepared = prepareTaskItemsForList(items)

        withContext(Dispatchers.Main) {
            fragment.adapter.data.clear()

            if (fragment.tasks.size == 1) {
                fragment.adapter.data.add(AddressListModel.SortingItem(sortingMethod, fragment.tasks.first().filtered))
            }
            fragment.adapter.data.addAll(prepared)

            fragment.adapter.notifyDataSetChanged()
        }
    }

    private fun prepareTaskItemsForList(taskItems: List<AddressListModel.TaskItem>): List<AddressListModel> {
        val sorted = if (sortingMethod == TaskAddressSorter.ALPHABETIC) {
            TaskAddressSorter.sortTaskItemsAlphabetic(taskItems)
        } else if (sortingMethod == TaskAddressSorter.CLOSE_TIME) {
            TaskAddressSorter.sortTaskItemsCloseTime(taskItems)
        } else {
            TaskAddressSorter.sortTaskItemsStandart(taskItems)

        }

        return TaskAddressSorter.getAddressesWithTasksList(sorted)
    }

    fun preloadTasks() {
        bgScope.launch {
            fragment.showLoading(true)
            fragment.tasks.clear()
            val tasks = fragment.taskIds.mapNotNull {
                application().tasksRepository.getTask(it)
            }
            fragment.tasks.addAll(tasks)
            applySorting()

            withContext(Dispatchers.Main) {
                fragment.updateCloseTaskButtonVisibility()
            }

            if (fragment.adapter.data.isEmpty()) {
                fragment.context?.showErrorSuspend("Открытых адресов не найдено", object : ErrorButtonsListener {
                    override fun positiveListener() {
                        application().router.exit()
                    }
                }, "Назад")
            } else {
                fragment.showLoading(false)
            }
        }
    }

    suspend fun checkTasks() = withContext(Dispatchers.IO) {
        val closedTasks = fragment.tasks.filter {
            it.taskItems.none {
                !it.isClosed
            }
        }
        closedTasks.forEach {
            if (it.state.toAndroidState() != TaskModel.COMPLETED) {
                application().tasksRepository.closeTaskStatus(it)
                application().tasksRepository.closeTaskById(it.id)
            }
            fragment.tasks.remove(it)
        }
        withContext(Dispatchers.Main) {
            if (fragment.tasks.isEmpty()) {
                application().router.exit()
            } else {
                applySorting()
            }
        }
    }

    fun onMapClicked() {
        var colorIdx = 0
        var lastTaskId = 0

        application().router.navigateTo(
            TasksYandexMapScreen(
                fragment.tasks,
                { address ->
                    fragment.targetAddress = address
                },
                {
                    fragment.shouldReloadData = true
                }
            )
        )
    }
}

