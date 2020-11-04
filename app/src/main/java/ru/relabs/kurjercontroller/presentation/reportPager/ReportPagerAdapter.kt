package ru.relabs.kurjercontroller.presentation.reportPager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import ru.relabs.kurjercontroller.domain.models.Entrance
import ru.relabs.kurjercontroller.domain.models.EntranceState
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.presentation.report.ReportFragment
import java.lang.ref.WeakReference

/**
 * Created by ProOrange on 15.04.2019.
 */

class ReportPagerAdapter(
    fm: FragmentManager
) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private var taskItemInternal: TaskItem? = null
    private var allTaskItems: List<TaskItem> = emptyList()
    private val fragments: MutableMap<Int, WeakReference<ReportFragment>> = mutableMapOf()

    fun setTaskWithItem(taskWithItem: TaskItem?, allTaskItems: List<TaskItem>) {
        taskItemInternal = taskWithItem
        this.allTaskItems = allTaskItems
        notifyDataSetChanged()
    }

    private fun openedEntrances(): List<Entrance> = taskItemInternal
        ?.entrances
        ?.sortedBy { it.state == EntranceState.CLOSED }
        ?: emptyList()

    override fun getItem(position: Int): Fragment {
        val ctxTask = taskItemInternal ?: return LoadingFragment()

        val fragment = ReportFragment.newInstance(
            ctxTask,
            openedEntrances()[position],
            allTaskItems.map { TaskItemWithTaskIds(it.taskId, it.id) }
        )
        fragments[position] = WeakReference(fragment)

        return fragment
    }


    override fun getCount(): Int = openedEntrances().size

    override fun getPageTitle(position: Int): CharSequence? {
        val entrance = openedEntrances()[position]
        return "Подъезд ${entrance.number.number} из $count"
    }
}