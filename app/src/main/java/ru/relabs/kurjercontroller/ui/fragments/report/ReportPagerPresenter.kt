package ru.relabs.kurjercontroller.ui.fragments.report

import kotlinx.android.synthetic.main.fragment_report_pager.*
import kotlinx.coroutines.Dispatchers
import ru.relabs.kurjercontroller.CancelableScope
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
            it.id == fragment.selectedTaskItemId
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

            },
            manager
        )
        fragment.view_pager?.adapter = fragment.pagerAdapter
        fragment.view_pager?.currentItem = 0
    }
}