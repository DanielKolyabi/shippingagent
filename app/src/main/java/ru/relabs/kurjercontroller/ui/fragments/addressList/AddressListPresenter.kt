package ru.relabs.kurjercontroller.ui.fragments.addressList

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.CancelableScope
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.models.AddressModel
import ru.relabs.kurjercontroller.ui.activities.ErrorButtonsListener
import ru.relabs.kurjercontroller.ui.activities.showError
import ru.relabs.kurjercontroller.ui.fragments.ReportScreen
import ru.relabs.kurjercontroller.ui.fragments.YandexMapScreen
import ru.relabs.kurjercontroller.ui.helpers.TaskAddressSorter

/**
 * Created by ProOrange on 18.03.2019.
 */

class AddressListPresenter(val fragment: AddressListFragment) {
    val bgScope = CancelableScope(Dispatchers.Default)
    var sortingMethod = TaskAddressSorter.ALPHABETIC

    suspend fun changeSortingMethod(sorting: Int) {
        sortingMethod = sorting
        applySorting()
    }

    fun onTaskItemClicked(clickedTask: AddressListModel.TaskItem) {

        val taskItemsOnAddress = fragment.adapter.data
            .filter {
                (it as? AddressListModel.TaskItem)?.taskItem?.address?.id == clickedTask.taskItem.address.id
            }
            .map {
                val task = it as AddressListModel.TaskItem
                Pair(task.parentTask, task.taskItem)
            }

        application().router.navigateTo(ReportScreen(taskItemsOnAddress, clickedTask.taskItem.id))
    }

    fun onSortingChanged(sortingMethod: Int) {
        bgScope.launch {
            changeSortingMethod(sortingMethod)
        }
    }

    fun onAddressMapClicked(addressModel: AddressModel) {
        application().router.navigateTo(YandexMapScreen(addressModel))
    }

    fun onCloseTaskClicked() {
        TODO("close task if all required taskItems closed. Send status to sirius") //To change body of created functions use File | Settings | File Templates.
    }

    suspend fun applySorting() = withContext(Dispatchers.IO) {
        val items = mutableListOf<AddressListModel.TaskItem>()

        fragment.tasks.forEach { task ->
            items.addAll(
                task.getTaskItems().map {
                    AddressListModel.TaskItem(it, task)
                }
            )
        }

        val prepared = prepareTaskItemsForList(items)

        withContext(Dispatchers.Main) {
            fragment.adapter.data.clear()


            if (fragment.tasks.size == 1) {
                fragment.adapter.data.add(AddressListModel.SortingItem(sortingMethod))
            }
            fragment.adapter.data.addAll(prepared)

            fragment.adapter.notifyDataSetChanged()
        }
    }

    private fun prepareTaskItemsForList(taskItems: List<AddressListModel.TaskItem>): List<AddressListModel> {
        val sorted = if (sortingMethod == TaskAddressSorter.ALPHABETIC) {
            TaskAddressSorter.sortTaskItemsAlphabetic(taskItems)
        } else {
            TaskAddressSorter.sortTaskItemsStandart(taskItems)
        }

        return TaskAddressSorter.getAddressesWithTasksList(sorted)
    }

    fun preloadTasks() {
        bgScope.launch {
            fragment.showLoading(true)
            val tasks = fragment.taskIds.mapNotNull {
                application().tasksRepository.getTask(it)
            }

            fragment.tasks.addAll(tasks)
            applySorting()

            if (fragment.tasks.isEmpty()) {
                fragment.context?.showError("Что-то сломалось :(", object : ErrorButtonsListener {
                    override fun positiveListener() {
                        application().router.exit()
                    }

                    override fun negativeListener() {}
                }, "Назад")
            } else {
                fragment.showLoading(false)
            }
        }
    }
}
