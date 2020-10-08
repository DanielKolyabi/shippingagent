package ru.relabs.kurjercontroller.presentation.fragments.report.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import ru.relabs.kurjercontroller.domain.models.EntranceModel
import ru.relabs.kurjercontroller.domain.models.TaskItemModel
import ru.relabs.kurjercontroller.domain.models.TaskModel
import ru.relabs.kurjercontroller.presentation.fragments.report.ReportFragment
import java.lang.ref.WeakReference

/**
 * Created by ProOrange on 15.04.2019.
 */

class ReportPagerAdapter(
    val task: TaskModel,
    val taskItem: TaskItemModel,
    val onEntranceClosed: (task: TaskModel, taskItem: TaskItemModel, entrance: EntranceModel) -> Unit,
    val getAllTaskItems: () -> List<TaskItemModel>,
    fm: FragmentManager
) :
    FragmentStatePagerAdapter(fm) {
    private val fragments: MutableMap<Int, WeakReference<ReportFragment>> = mutableMapOf()

    private fun openedEntrances(): List<EntranceModel> = taskItem.entrances.sortedBy { it.state == EntranceModel.CLOSED }//.filter { it.state == EntranceModel.CREATED }

    override fun getItem(position: Int): Fragment {
        val fragment = ReportFragment.newInstance(
            task,
            taskItem,
            openedEntrances()[position]
        )
        fragments[position] = WeakReference(fragment)

        fragment.callback = object : ReportFragment.Callback {
            override fun getAllTaskItems(): List<TaskItemModel> =
                this@ReportPagerAdapter.getAllTaskItems()


            override fun onEntranceClosed(task: TaskModel, taskItem: TaskItemModel, entrance: EntranceModel) {
                this@ReportPagerAdapter.onEntranceClosed(task, taskItem, entrance)
            }

            override fun onEntranceChanged(entrance: EntranceModel) {
                fragments.forEach { ref ->
                    ref.value.get()?.onChanged(entrance)
                }
            }
        }

        return fragment
    }


    override fun getCount(): Int = openedEntrances().size

    override fun getPageTitle(position: Int): CharSequence? {
        val entrance = openedEntrances()[position]
        return "Подъезд ${entrance.number} из $count"
    }
}