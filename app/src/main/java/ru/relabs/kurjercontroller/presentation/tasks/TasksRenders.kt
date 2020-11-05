package ru.relabs.kurjercontroller.presentation.tasks

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.presentation.base.DefaultListDiffCallback
import ru.relabs.kurjercontroller.presentation.base.recycler.DelegateAdapter
import ru.relabs.kurjercontroller.presentation.base.tea.renderT
import ru.relabs.kurjercontroller.utils.SearchUtils
import ru.relabs.kurjercontroller.utils.extensions.visible
import java.util.*

/**
 * Created by Daniil Kurchanov on 06.04.2020.
 */
object TasksRenders {
    fun renderList(adapter: DelegateAdapter<TasksItem>): TasksRender = renderT(
        { Triple(it.tasks, it.selectedTasks, it.loaders) to it.searchFilter },
        { (data, filter) ->
            val (tasks, selectedTasks, loaders) = data
            val intersections = searchIntersections(tasks, tasks.filter { selectedTasks.contains(it.id) })
            val newItems = if (tasks.isEmpty() && loaders > 0) {
                listOf(TasksItem.Loader(""))
            } else {
                val tasksWithGroups = tasks
                    .filter {
                        if (filter.isNotEmpty()) {
                            SearchUtils.isMatches(it.name, filter)
                        } else {
                            true
                        }
                    }
                    .groupBy { it.startControlDate }
                    .flatMap {
                        val date = DateTime(it.key)
                        val groupTitle =
                            date.dayOfWeek().getAsText(Locale("ru", "RU")).capitalize() + ", " + date.toString("dd.MM.yyyy")
                        listOf(TasksItem.Header(groupTitle)) + it.value.map { task ->
                            TasksItem.TaskItem(
                                task,
                                intersections.getOrElse(task) { false },
                                selectedTasks.any { it == task.id })
                        }
                    }

                listOf(TasksItem.Search(filter)) + tasksWithGroups + listOfNotNull(TasksItem.Blank.takeIf { selectedTasks.isNotEmpty() })
            }

            val diff = DiffUtil.calculateDiff(DefaultListDiffCallback(adapter.items, newItems) { o, n ->
                if (o is TasksItem.Search && n is TasksItem.Search) {
                    true
                } else {
                    null
                }
            })

            adapter.items.clear()
            adapter.items.addAll(newItems)
            diff.dispatchUpdatesTo(adapter)
        }
    )

    fun renderLoading(view: View): TasksRender = renderT(
        { it.tasks to (it.loaders > 0) },
        { (tasks, loading) ->
            view.visible = tasks.isNotEmpty() && loading
        }
    )

    fun renderStartButton(view: View): TasksRender = renderT(
        { it.selectedTasks.isNotEmpty() },
        { view.visible = it }
    )

    fun renderOnlineButton(view: View): TasksRender = renderT(
        { it.loaders == 0 },
        { view.isEnabled = it }
    )

    private fun searchIntersections(
        tasks: List<Task>,
        selectedTasks: List<Task>
    ): Map<Task, Boolean> {
        val result = mutableMapOf<Task, Boolean>()
        tasks.forEach { task ->
            selectedTasks.forEach { selectedTask ->
                val isSelectedTaskContainsTaskAddress = task.taskItems.any { taskItem ->
                    selectedTask.taskItems.any { selectedTaskItem -> selectedTaskItem.address.idnd == taskItem.address.idnd && selectedTaskItem.id != taskItem.id }
                }

                if (isSelectedTaskContainsTaskAddress) {
                    result[task] = true
                }
            }
        }
        return result
    }
}