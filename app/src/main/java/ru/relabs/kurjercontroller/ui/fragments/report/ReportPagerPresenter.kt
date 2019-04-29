package ru.relabs.kurjercontroller.ui.fragments.report

import kotlinx.android.synthetic.main.fragment_report_pager.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.CancelableScope
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.models.EntranceModel
import ru.relabs.kurjercontroller.models.TaskItemModel
import ru.relabs.kurjercontroller.models.TaskModel
import ru.relabs.kurjercontroller.ui.fragments.report.adapters.ReportPagerAdapter

/**
 * Created by ProOrange on 18.03.2019.
 */

class ReportPagerPresenter(val fragment: ReportPagerFragment) {
    val bgScope = CancelableScope(Dispatchers.Default)

    fun getSelectedTask(): Pair<TaskModel, TaskItemModel> {
        val selectedTaskItem = fragment.taskItems.first {
            it.id == fragment.selectedTask.second && it.taskId == fragment.selectedTask.first
        }
        val selectedTask = fragment.tasks.first {
            it.id == selectedTaskItem.taskId
        }
        return Pair(selectedTask, selectedTaskItem)
    }

    fun updatePagerAdapter() {
        val manager = fragment.fragmentManager ?: return

        val selectedTask = getSelectedTask()
        fragment.pagerAdapter = ReportPagerAdapter(
            selectedTask.first,
            selectedTask.second,
            { task, taskItem, entrance ->
                onEntranceClosed(task, taskItem, entrance)
            },
            {
                fragment.taskItems.toList()
            },
            manager
        )
        fragment.view_pager?.adapter = fragment.pagerAdapter
        fragment.view_pager?.currentItem = 0
    }

    private fun onEntranceClosed(task: TaskModel, taskItem: TaskItemModel, entrance: EntranceModel) {
        val location = application().currentLocation
        bgScope.launch {
            var shouldRefreshUI = true

            application().tasksRepository.saveTaskReport(
                taskItem,
                entrance,
                task.publishers.first { it.name == taskItem.publisherName },
                location
            )

            application().tasksRepository.closeEntrance(taskItem.taskId, taskItem.id, entrance.number)

            val idx = fragment.taskItems.indexOfFirst {
                it.id == taskItem.id && it.taskId == task.id
            }
            if (idx < 0) {
                return@launch
            }
            val entranceIdx = fragment.taskItems[idx].entrances.indexOf(entrance)
            if (entranceIdx < 0) {
                return@launch
            }

            fragment.taskItems[idx].entrances[entranceIdx].state = EntranceModel.CLOSED

            if (fragment.taskItems[idx].entrances.none { it.state == EntranceModel.CREATED }) {

                fragment.taskItems.removeAt(idx)
                fragment.tasks.removeAt(idx)

                if (fragment.taskItems.isEmpty()) {
                    shouldRefreshUI = false
                    withContext(Dispatchers.Main) {
                        application().router.exit()
                    }
                } else {
                    fragment.selectedTask = Pair(fragment.taskItems.first().taskId, fragment.taskItems.first().id)
                }
            }

            if (shouldRefreshUI) {
                withContext(Dispatchers.Main) {
                    fragment.updateTasks()
                    updatePagerAdapter()
                }
            }
        }
    }

    fun onTaskChanged(taskNumber: Int) {
        fragment.selectedTask = Pair(fragment.taskItems[taskNumber].taskId, fragment.taskItems[taskNumber].id)
        fragment.updateTasks()
        updatePagerAdapter()
    }
}