package ru.relabs.kurjercontroller.presentation.reportPager

import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.domain.repositories.DatabaseRepository
import ru.relabs.kurjercontroller.presentation.base.tea.*

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */

data class ReportTaskWithItem(
    val task: Task,
    val taskItem: TaskItem
)

data class ReportPagerState(
    val tasks: List<ReportTaskWithItem> = emptyList(),
    val selectedTask: ReportTaskWithItem? = null,
    val loaders: Int = 0
)

class ReportPagerContext(val errorContext: ErrorContextImpl = ErrorContextImpl()) :
    ErrorContext by errorContext,
    RouterContext by RouterContextMainImpl(),
    KoinComponent {
    val databaseRepository: DatabaseRepository by inject()
}

typealias ReportPagerMessage = ElmMessage<ReportPagerContext, ReportPagerState>
typealias ReportPagerEffect = ElmEffect<ReportPagerContext, ReportPagerState>
typealias ReportPagerRender = ElmRender<ReportPagerState>