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
    val onEntranceClosed: (task: Task, taskItem: TaskItem, entrance: Entrance) -> Unit,
    val getAllTaskItems: () -> List<TaskItem>,
    fm: FragmentManager
) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private var taskWithItemInternal: ReportTaskWithItem? = null
    private val fragments: MutableMap<Int, WeakReference<ReportFragment>> = mutableMapOf()

    fun setTaskWithItem(taskWithItem: ReportTaskWithItem?){
        taskWithItemInternal = taskWithItem
        notifyDataSetChanged()
    }

    private fun openedEntrances(): List<Entrance> = taskWithItemInternal?.taskItem
        ?.entrances
        ?.sortedBy { it.state == EntranceState.CLOSED }
        ?: emptyList()

    override fun getItem(position: Int): Fragment {
        val ctxTask = taskWithItemInternal ?: return LoadingFragment()

        val fragment = ReportFragment.newInstance(
            ctxTask.task,
            ctxTask.taskItem,
            openedEntrances()[position]
        )
        fragments[position] = WeakReference(fragment)

        //TODO: Refactor?
//        fragment.callback = object : ReportFragment.Callback {
//            override fun getAllTaskItems(): List<TaskItem> =
//                this@ReportPagerAdapter.getAllTaskItems()
//
//
//            override fun onEntranceClosed(task: Task, taskItem: TaskItem, entrance: Entrance) {
//                this@ReportPagerAdapter.onEntranceClosed(task, taskItem, entrance)
//            }
//
//            override fun onEntranceChanged(entrance: Entrance) {
//                fragments.forEach { ref ->
//                    ref.value.get()?.onChanged(entrance)
//                }
//            }
//        }

        return fragment
    }


    override fun getCount(): Int = openedEntrances().size

    override fun getPageTitle(position: Int): CharSequence? {
        val entrance = openedEntrances()[position]
        return "Подъезд ${entrance.number} из $count"
    }
}