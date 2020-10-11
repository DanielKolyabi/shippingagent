package ru.relabs.kurjercontroller.presentation.tasks

import ru.relabs.kurjercontroller.domain.models.Task

sealed class TasksItem{
    data class TaskItem(
        val task: Task,
        val isTasksWithSameAddressPresented: Boolean,
        val isSelected: Boolean
    ): TasksItem()
    data class Header(val title: String): TasksItem()

    object Blank: TasksItem()
    data class Search(val filter: String): TasksItem()
    data class Loader(val text: String): TasksItem()
}