package ru.relabs.kurjercontroller.presentation.tasks

import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.relabs.kurjercontroller.domain.controllers.TaskEventController
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.repositories.ControlRepository
import ru.relabs.kurjercontroller.domain.repositories.DatabaseRepository
import ru.relabs.kurjercontroller.domain.useCases.OnlineTaskUseCase
import ru.relabs.kurjercontroller.presentation.base.tea.*

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */

data class TasksState(
    val tasks: List<Task> = emptyList(),
    val selectedTasks: List<Task> = emptyList(),
    val loaders: Int = 0,
    val searchFilter: String = ""
)

class TasksContext(val consumer: TasksFragment, val errorContext: ErrorContextImpl = ErrorContextImpl()) :
    ErrorContext by errorContext,
    RouterContext by RouterContextMainImpl(),
    KoinComponent {

    val controlRepository: ControlRepository by inject()
    val databaseRepository: DatabaseRepository by inject()
    val taskEventController: TaskEventController by inject()
    val onlineTaskUseCase: OnlineTaskUseCase by inject()

    var showSnackbar: suspend (Int) -> Unit = {}
    var showUpdateRequiredOnVisible: () -> Unit = {}
}

typealias TasksMessage = ElmMessage<TasksContext, TasksState>
typealias TasksEffect = ElmEffect<TasksContext, TasksState>
typealias TasksRender = ElmRender<TasksState>