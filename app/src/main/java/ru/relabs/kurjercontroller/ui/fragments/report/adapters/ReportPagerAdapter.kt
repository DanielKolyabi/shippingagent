package ru.relabs.kurjercontroller.ui.fragments.report.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import ru.relabs.kurjercontroller.models.EntranceModel
import ru.relabs.kurjercontroller.models.TaskItemModel
import ru.relabs.kurjercontroller.models.TaskModel
import ru.relabs.kurjercontroller.ui.fragments.report.ReportFragment

/**
 * Created by ProOrange on 15.04.2019.
 */

class ReportPagerAdapter(
    val task: TaskModel,
    val taskItem: TaskItemModel,
    val onEntranceClosed: (task: TaskModel, taskItem: TaskItemModel, entrance: EntranceModel) -> Unit,
    fm: FragmentManager
) :
    FragmentStatePagerAdapter(fm) {

    private fun openedEntrances(): List<EntranceModel> = taskItem.entrances.sortedBy { it.state == EntranceModel.CLOSED }//.filter { it.state == EntranceModel.CREATED }

    override fun getItem(position: Int): Fragment {
        val fragment = ReportFragment.newInstance(
            task,
            taskItem,
            openedEntrances()[position]
        )

        fragment.callback = object : ReportFragment.Callback {
            override fun onEntranceClosed(task: TaskModel, taskItem: TaskItemModel, entrance: EntranceModel) {
                this@ReportPagerAdapter.onEntranceClosed(task, taskItem, entrance)
            }
        }

        return fragment
    }


    override fun getCount(): Int = openedEntrances().size

    override fun getPageTitle(position: Int): CharSequence? {
        val entrance = openedEntrances()[position]
        return "Подъезд ${entrance.number} из $count. Кв. ${entrance.startApartments} - ${entrance.endApartments}"
    }
}