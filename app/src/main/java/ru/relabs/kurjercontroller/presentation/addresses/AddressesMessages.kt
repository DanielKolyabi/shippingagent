package ru.relabs.kurjercontroller.presentation.addresses

import org.joda.time.DateTime
import ru.relabs.kurjercontroller.domain.models.*
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffect
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffects
import ru.relabs.kurjercontroller.presentation.base.tea.msgEmpty
import ru.relabs.kurjercontroller.presentation.base.tea.msgState
import ru.relabs.kurjercontroller.utils.SearchUtils

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */

object AddressesMessages {
    fun msgInit(taskIds: List<TaskId>): AddressesMessage = msgEffects(
        { it },
        {
            listOf(
                AddressesEffects.effectLoadTasks(taskIds),
                AddressesEffects.effectLaunchEventConsumer()
            )
        }
    )

    fun msgAddLoaders(i: Int): AddressesMessage =
        msgState { it.copy(loaders = it.loaders + i) }

    fun msgTaskItemClicked(item: TaskItem, task: Task): AddressesMessage =
        msgEffect(AddressesEffects.effectNavigateReport(task, item))

    fun msgAddressMapClicked(addressTaskItems: List<TaskItem>): AddressesMessage =
        msgEffect(AddressesEffects.effectOpenYandexMap(addressTaskItems))

    fun msgSortingChanged(sorting: AddressesSortingMethod): AddressesMessage =
        msgState { it.copy(sorting = sorting) }

    fun msgTasksLoaded(tasks: List<Task>): AddressesMessage =
        msgState {
            it.copy(
                tasks = tasks,
                sorting = if (tasks.size == 1 && tasks.firstOrNull()?.filtered == true) {
                    AddressesSortingMethod.STANDARD
                } else {
                    AddressesSortingMethod.CLOSE_TIME
                }
            )
        }

    fun msgNavigateBack(): AddressesMessage = msgEffects(
        { it.copy(exits = it.exits + 1) },
        { listOf(AddressesEffects.effectNavigateBack()) }
    )

    fun msgSearch(searchText: String): AddressesMessage =
        msgState { it.copy(searchFilter = searchText) }

    fun msgTaskItemClosed(taskItemId: TaskItemId): AddressesMessage = msgEffects(
        { s ->
            val newTasks = s.tasks.map { t ->
                t.copy(
                    taskItems = t.taskItems.map { ti ->
                        if (ti.id == taskItemId) {
                            ti.copy(closeTime = DateTime.now())
                        } else {
                            ti
                        }
                    }
                )
            }
            val visibleOpenedTaskItemsCount = newTasks.map { t ->
                t.taskItems.filter {
                    SearchUtils.isMatches(it.address.name, s.searchFilter) && !it.isClosed
                }.size
            }.sum()

            s.copy(
                tasks = newTasks,
                searchFilter = if (visibleOpenedTaskItemsCount == 0) {
                    ""
                } else {
                    s.searchFilter
                }
            )
        },
        {
            listOf(
                AddressesEffects.effectValidateTasks()
            )
        }
    )

    fun msgRemoveTask(id: TaskId): AddressesMessage = msgEffects(
        { s ->
            val newTasks = s.tasks.filter { it.id != id }
            s.copy(tasks = newTasks, exits = if (newTasks.isEmpty()) s.exits.inc() else s.exits)
        },
        { s ->
            listOfNotNull(
                AddressesEffects.effectNavigateBack().takeIf { s.tasks.isEmpty() }
            )
        }
    )

    fun msgSelectedListAddress(address: Address?): AddressesMessage =
        msgState { it.copy(selectedListAddress = address) }

    fun msgGlobalMapClicked(): AddressesMessage = msgEffects(
        { it },
        { s ->
            listOf(AddressesEffects.effectOpenGlobalMap())
        }
    )

    fun msgCloseTaskClicked(): AddressesMessage =
        msgEffect(AddressesEffects.effectCloseCurrentTask())
}