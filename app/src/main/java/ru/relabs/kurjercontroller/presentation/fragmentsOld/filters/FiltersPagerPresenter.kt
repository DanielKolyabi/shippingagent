package ru.relabs.kurjercontroller.presentation.fragmentsOld.filters

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.utils.CancelableScope
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.domain.models.TaskFilters
import ru.relabs.kurjercontroller.domain.models.TaskModel

class FiltersPagerPresenter(val fragment: FiltersPagerFragment) {
    fun onStartClicked(
        task: TaskModel,
        newFilters: TaskFilters,
        withPlanned: Boolean
    ) {
        bgScope.launch(Dispatchers.IO) {
            application().tasksRepository.saveFilters(task, newFilters, withPlanned)

            withContext(Dispatchers.Main) {
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
