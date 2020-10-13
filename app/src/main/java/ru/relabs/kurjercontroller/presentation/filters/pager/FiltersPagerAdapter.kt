package ru.relabs.kurjercontroller.presentation.filters.pager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskFilters
import ru.relabs.kurjercontroller.domain.models.TaskId
import ru.relabs.kurjercontroller.presentation.filters.editor.FiltersEditorFragment
import ru.relabs.kurjercontroller.presentation.filters.editor.IFiltersEditorConsumer

/**
 * Created by ProOrange on 15.04.2019.
 */

const val FILTERS_EDITOR_REQUEST_CODE = 532

class FiltersPagerAdapter<T>(
    var tasks: List<Task>,
    private val targetFragment: T,
    fm: FragmentManager
) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
        where T : Fragment, T : IFiltersEditorConsumer {

    override fun getCount(): Int = tasks.size

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun getItem(position: Int): Fragment {
        val taskData = tasks.getOrNull(position)
        if(taskData == null){
            //TODO: Crashlytics
        }

        return FiltersEditorFragment.newInstance(
            taskData?.id ?: TaskId(-1),
            taskData?.taskFilters ?: TaskFilters.blank(),
            taskData?.withPlanned ?: false,
            targetFragment
        )
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tasks[position].name
    }
}