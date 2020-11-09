package ru.relabs.kurjercontroller.presentation.taskDetails

import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.relabs.kurjercontroller.domain.models.Address
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.repositories.DatabaseRepository
import ru.relabs.kurjercontroller.presentation.base.tea.*

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */

data class TaskDetailsState(
    val loaders: Int = 0,
    val task: Task? = null,
    val targetAddress: Address? = null
)

class TaskDetailsContext(val errorContext: ErrorContextImpl = ErrorContextImpl()) :
    ErrorContext by errorContext,
    RouterContext by RouterContextMainImpl(),
    KoinComponent {

    val database: DatabaseRepository by inject()

    var onExamine: (Task) -> Unit = {}
    var showFatalError: suspend (String) -> Unit = {}
    var showSnackbar: suspend (Int) -> Unit = {}
    var addressClickedConsumer: () -> TaskDetailsFragment? = { null }
}

typealias TaskDetailsMessage = ElmMessage<TaskDetailsContext, TaskDetailsState>
typealias TaskDetailsEffect = ElmEffect<TaskDetailsContext, TaskDetailsState>
typealias TaskDetailsRender = ElmRender<TaskDetailsState>