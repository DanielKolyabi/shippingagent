package ru.relabs.kurjercontroller.presentation.taskDetails

import ru.relabs.kurjercontroller.domain.models.Task

interface IExaminedConsumer {
    fun onExamined(task: Task)
}