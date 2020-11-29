package ru.relabs.kurjercontroller.presentation.reportPager

import android.view.View
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.presentation.base.recycler.DelegateAdapter
import ru.relabs.kurjercontroller.presentation.base.tea.renderT
import ru.relabs.kurjercontroller.utils.extensions.visible

/**
 * Created by Daniil Kurchanov on 06.04.2020.
 */
object ReportPagerRenders {
    fun renderLoading(loader: View): ReportPagerRender = renderT(
        { it.loaders > 0 },
        { loader.visible = it }
    )

    fun renderTasks(adapter: DelegateAdapter<ReportPagerTaskItem>, list: View): ReportPagerRender = renderT(
        { it.tasks to (it.selectedTask ?: it.tasks.firstOrNull()) },
        { (tasks, selectedTask) ->
            list.visible = tasks.size > 1
            adapter.items.clear()
            adapter.items.addAll(tasks.map { ReportPagerTaskItem.TaskButton(it, it.id == selectedTask?.id) })
            adapter.notifyDataSetChanged()
        }
    )

    fun renderPager(adapter: ReportPagerAdapter): ReportPagerRender = renderT(
        { it.tasks to (it.selectedTask ?: it.tasks.firstOrNull()) },
        { (tasks, selectedTask) ->
            if (selectedTask != null) {
                adapter.setTaskWithItem(selectedTask, tasks)
            } else {
                adapter.setTaskWithItem(null, tasks)
            }
        }
    )

    fun renderPagerPosition(pager: ViewPager): ReportPagerRender = renderT(
        { it.selectedEntrancePosition },
        {
            if (pager.currentItem != it) {
                pager.currentItem = it
            }
        }
    )

    fun renderTitle(view: TextView): ReportPagerRender = renderT(
        { it.tasks.firstOrNull()?.address?.name ?: view.resources.getString(R.string.loading) },
        { view.text = it }
    )
}