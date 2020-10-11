package ru.relabs.kurjercontroller.presentation.fragmentsOld.filters.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import ru.relabs.kurjercontroller.domain.models.TaskFilters
import ru.relabs.kurjercontroller.domain.models.TaskModel
import ru.relabs.kurjercontroller.presentation.fragmentsOld.filters.FiltersFragment

/**
 * Created by ProOrange on 15.04.2019.
 */

class FiltersPagerAdapter(
    var tasks: List<TaskModel>,
    fm: FragmentManager,
    val onStartClicked: (task: TaskModel, newFilters: TaskFilters, withPlanned: Boolean) -> Unit
) : FragmentStatePagerAdapter(fm) {
    override fun getCount(): Int = tasks.size

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun getItem(position: Int): Fragment {

        val fragment = FiltersFragment.newInstance(
            tasks[position].taskFilters,
            tasks[position].withPlanned
        )

        fragment.onStartClicked = { filters, withPlanned ->
            onStartClicked(tasks[position], filters, withPlanned)
        }

        return fragment
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tasks[position].name
    }
}