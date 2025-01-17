package ru.relabs.kurjercontroller.presentation.addresses

import org.joda.time.DateTime
import ru.relabs.kurjercontroller.domain.models.*
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffect
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffects
import ru.relabs.kurjercontroller.presentation.base.tea.msgState
import ru.relabs.kurjercontroller.utils.CustomLog
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
        { listOf(AddressesEffects.effectNavigateBack(it.exits)) }
    )

    fun msgSearch(searchText: String): AddressesMessage =
        msgState { it.copy(searchFilter = searchText) }

    fun msgTaskItemClosed(taskId: TaskId, taskItemId: TaskItemId): AddressesMessage = msgEffects(
        { s ->
            val newTasks = s.tasks.map { t ->
                t.copy(
                    taskItems = t.taskItems.map { ti ->
                        if (ti.id == taskItemId && ti.taskId == taskId) {
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
                AddressesEffects.effectLoadTasks(it.tasks.map { it.id }),
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
            if(s.tasks.isEmpty()){
                CustomLog.writeToFile("Navigate back from addresses, task removed, none tasks left")
            }
            listOfNotNull(
                AddressesEffects.effectNavigateBack(s.exits).takeIf { s.tasks.isEmpty() }
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

    fun msgYandexMapAddressSelected(address: Address): AddressesMessage =
        msgEffect(AddressesEffects.effectYandexMapAddressSelected(address))

    fun msgTaskItemsAdded(): AddressesMessage =
        msgEffects({ it }, { listOf(AddressesEffects.effectLoadTasks(it.tasks.map { it.id })) })

    fun msgTaskItemChanged(item: TaskItem): AddressesMessage = msgState { s ->
        if (s.tasks.any { it.id == item.taskId }) {
            s.copy(
                tasks = s.tasks.map { t ->
                    if (t.taskItems.any { it.id == item.id && it.taskId == item.taskId }) {
                        t.copy(taskItems = t.taskItems.map { ti ->
                            if (ti.id == item.id && ti.taskId == item.taskId) {
                                item
                            } else {
                                ti
                            }
                        })
                    } else {
                        t
                    }
                }
            )
        } else {
            s
        }
    }

    fun msgEntranceClosed(taskId: TaskId, taskItemId: TaskItemId, number: EntranceNumber): AddressesMessage = msgState { s ->
        if (s.tasks.any { it.id == taskId }) {
            s.copy(
                tasks = s.tasks.map { t ->
                    if (t.taskItems.any { it.id == taskItemId && it.taskId == taskId }) {
                        t.copy(taskItems = t.taskItems.map { ti ->
                            if (ti.id == taskItemId && ti.taskId == taskId) {
                                ti.copy(entrances = ti.entrances.map { e ->
                                    if (e.number == number) {
                                        e.copy(state = EntranceState.CLOSED)
                                    } else {
                                        e
                                    }
                                })
                            } else {
                                ti
                            }
                        })
                    } else {
                        t
                    }
                }
            )
        } else {
            s
        }

    }

    fun msgTaskItemClosedByDeliveryMan(taskItemId: TaskItemId, closeTime: DateTime): AddressesMessage = msgState { s ->
        s.copy(tasks = s.tasks.map { t ->
            t.copy(
                taskItems = t.taskItems.map { ti ->
                    if (ti.id == taskItemId) {
                        ti.copy(isNew = true, closeTime = closeTime)
                    } else {
                        ti
                    }
                }
            )
        })
    }
}