package ru.relabs.kurjercontroller.ui.fragments.filters

import kotlinx.android.synthetic.main.fragment_filters_pager.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.CancelableScope
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.models.TaskFiltersModel
import ru.relabs.kurjercontroller.models.TaskModel
import ru.relabs.kurjercontroller.ui.extensions.setVisible

class FiltersPagerPresenter(val fragment: FiltersPagerFragment) {
    fun onStartClicked(
        task: TaskModel,
        newFilters: TaskFiltersModel
    ) {
        bgScope.launch(Dispatchers.IO) {
            application().tasksRepository.saveFilters(task, newFilters)

            withContext(Dispatchers.Main){
                fragment.tasks.removeAll { it.id == task.id }
                if (fragment.tasks.size == 0) {
                    application().router.exit()
                    fragment.onAllFiltersApplied?.invoke()
                } else {
                    fragment.pagerAdapter.tasks = fragment.tasks
                    fragment.pagerAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    val bgScope = CancelableScope(Dispatchers.Default)
}
