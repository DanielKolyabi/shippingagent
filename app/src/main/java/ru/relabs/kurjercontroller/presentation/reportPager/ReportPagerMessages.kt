package ru.relabs.kurjercontroller.presentation.reportPager

import ru.relabs.kurjercontroller.domain.models.*
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffect
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffects
import ru.relabs.kurjercontroller.presentation.base.tea.msgState
import ru.relabs.kurjercontroller.utils.debug
import kotlin.math.roundToInt

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */

object ReportPagerMessages {
    fun msgInit(
        taskIds: List<TaskItemWithTaskIds>,
        selectedTask: TaskItemWithTaskIds?
    ): ReportPagerMessage = msgEffects(
        { it },
        {
            listOf(
                ReportPagerEffects.effectInit(taskIds, selectedTask),
                ReportPagerEffects.effectSubscribeEvents()
            )
        }
    )

    fun msgAddLoaders(i: Int): ReportPagerMessage =
        msgState { it.copy(loaders = it.loaders + i) }

    fun msgTaskClicked(item: TaskItem): ReportPagerMessage =
        msgState { it.copy(selectedTask = item) }

    fun msgTasksLoaded(tasks: List<TaskItem>, selectedTask: TaskItem): ReportPagerMessage =
        msgState { it.copy(tasks = tasks, selectedTask = selectedTask, initialTask = selectedTask) }

    fun msgEntranceClosed(task: TaskId, taskItem: TaskItemId, entrance: EntranceNumber): ReportPagerMessage =
        msgEffect(ReportPagerEffects.effectCloseEntrance(task, taskItem, entrance))

    fun msgNavigateBack(): ReportPagerMessage =
        msgEffect(ReportPagerEffects.effectNavigateBack())

    fun msgCloseTaskItemEntrance(targetTaskItem: TaskItem, entrance: EntranceNumber): ReportPagerMessage = msgState { s ->
        val newTasks = s.tasks.map { t ->
            if (t.id == targetTaskItem.id && t.taskId == targetTaskItem.taskId) {
                t.copy(entrances = t.entrances.map { e ->
                    if (e.number == entrance) {
                        e.copy(state = EntranceState.CLOSED)
                    } else {
                        e
                    }
                })
            } else {
                t
            }
        }
        val updatedState = if (!s.nextAppData.isSettledUp) {
            s.copy(
                tasks = newTasks,
                nextAppData = NextAppCalculationData(
                    isSettledUp = true,
                    direction = if (entrance.number > (targetTaskItem.entrances.size.toFloat() / 2).roundToInt()) -1 else 1
                )
            )
        } else {
            s.copy(tasks = newTasks)
        }


        /*Найти такой же незакрытый подъезд в другом задании
          Если есть - открыть его
          Если нет - перейти к следующему подъезду в исходном задании или других заданиях
          Следующий подъезд считается исходя из текущего из логики описанной в гитлабе
         */

        val entranceInOtherTasks = updatedState.tasks.mapNotNull { task ->
            task.entrances
                .firstOrNull { it.number == entrance && it.state == EntranceState.CREATED }
                ?.let { task to it }
        }

        if (entranceInOtherTasks.isNotEmpty()) {
            val targetEntrance = entranceInOtherTasks.first()
            val entrancePosition = targetEntrance.first.entrances
                .sortedBy { it.state == EntranceState.CLOSED }
                .indexOfFirst { it.number == entrance }

            updatedState.copy(selectedTask = targetEntrance.first, selectedEntrancePosition = entrancePosition)
        } else {
            /*
            Найти все открытые подъезды во всех заданиях
            Найти номер следующего подъезда в заданном направлении для любого задания
            Найти этот подъезд во всех заданиях с приоритетом на initial task
             */
            val initialUpdatedTask = updatedState.tasks.firstOrNull { it.id == updatedState.initialTask?.id }
            val openedEntrancesInAllTasks = updatedState.tasks
                .flatMap { it.entrances }
                .filter { it.state == EntranceState.CREATED }
                .distinctBy { it.number }
                .map { it.number }
                .sortedBy { it.number }

            fun findNextEntranceNumberInDirection(dir: Int): EntranceNumber? {
                return if (dir > 0) {
                    openedEntrancesInAllTasks.firstOrNull { it.number > entrance.number }
                } else {
                    openedEntrancesInAllTasks.lastOrNull { it.number < entrance.number }
                }
            }

            val targetEntranceNumber = findNextEntranceNumberInDirection(updatedState.nextAppData.direction)
                ?: findNextEntranceNumberInDirection(updatedState.nextAppData.direction * -1)
                ?: openedEntrancesInAllTasks.firstOrNull()

            val allTasksWithInitialFirst =
                listOfNotNull(initialUpdatedTask) + updatedState.tasks.filter { it != initialUpdatedTask }

            val allTasksWithThisEntrance = allTasksWithInitialFirst
                .mapNotNull { item ->
                    item.entrances
                        .filter { it.state == EntranceState.CREATED }
                        .indexOfFirst { it.number == targetEntranceNumber }
                        .takeIf { it != -1 }
                        ?.let { item to it }
                }
            val taskWithThisEntrance = allTasksWithThisEntrance.firstOrNull()

            if (taskWithThisEntrance != null) {
                updatedState.copy(
                    selectedTask = taskWithThisEntrance.first,
                    selectedEntrancePosition = taskWithThisEntrance.second
                )
            } else {
                //Fallback to old implementation

                val selectedTask = newTasks.firstOrNull { it.id == s.selectedTask?.id }
                val entrances = selectedTask?.entrances?.sortedBy { it.state == EntranceState.CLOSED }
                val newAdapterPosition =
                    if (entrances?.getOrNull(s.selectedEntrancePosition)?.state == EntranceState.CREATED) {
                        s.selectedEntrancePosition
                    } else {
                        0
                    }
                updatedState.copy(
                    selectedTask = selectedTask,
                    selectedEntrancePosition = newAdapterPosition
                )
            }
        }
    }

    fun msgCloseTaskItem(targetTaskItem: TaskItem): ReportPagerMessage = msgEffects(
        { it },
        { s ->
            val otherTask = s.tasks.firstOrNull { it.id != targetTaskItem.id && !it.isClosed }
            if (otherTask == null) {
                listOf(ReportPagerEffects.effectNavigateBack())
            } else {
                emptyList()
            }
        }
    )

    fun msgTaskItemChanged(taskItem: TaskItem): ReportPagerMessage = msgState { s ->
        if (s.tasks.any { it.id == taskItem.id && it.taskId == taskItem.taskId }) {
            s.copy(
                tasks = s.tasks.map {
                    if (it.id == taskItem.id && it.taskId == taskItem.taskId) {
                        taskItem
                    } else {
                        it
                    }
                },
                selectedTask = taskItem.takeIf { it.id == taskItem.id && it.taskId == taskItem.taskId } ?: s.selectedTask
            )
        } else {
            s
        }
    }

    fun msgPageSelected(pos: Int): ReportPagerMessage = msgState {
        debug("Pager page selected: $pos")
        it.copy(selectedEntrancePosition = pos)
    }
}


