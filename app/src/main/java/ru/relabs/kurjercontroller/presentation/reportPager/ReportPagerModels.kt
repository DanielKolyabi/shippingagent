package ru.relabs.kurjercontroller.presentation.reportPager

import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.relabs.kurjercontroller.domain.controllers.TaskEventController
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.domain.repositories.DatabaseRepository
import ru.relabs.kurjercontroller.presentation.base.tea.*

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */

data class ReportPagerState(
    val tasks: List<TaskItem> = emptyList(),
    val selectedTask: TaskItem? = null,
    val selectedEntrancePosition: Int = 0,
    val loaders: Int = 0
)

class ReportPagerContext(val errorContext: ErrorContextImpl = ErrorContextImpl()) :
    ErrorContext by errorContext,
    RouterContext by RouterContextMainImpl(),
    KoinComponent {
    val databaseRepository: DatabaseRepository by inject()
    val taskEventController: TaskEventController by inject()

    var showSnackbar: ((resId: Int) -> Unit)? = null
}

typealias ReportPagerMessage = ElmMessage<ReportPagerContext, ReportPagerState>
typealias ReportPagerEffect = ElmEffect<ReportPagerContext, ReportPagerState>
typealias ReportPagerRender = ElmRender<ReportPagerState>