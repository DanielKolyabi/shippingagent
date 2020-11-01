package ru.relabs.kurjercontroller.presentation.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.fragment_tasks.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskFilters
import ru.relabs.kurjercontroller.domain.models.TaskId
import ru.relabs.kurjercontroller.presentation.base.fragment.BaseFragment
import ru.relabs.kurjercontroller.presentation.base.fragment.FragmentStyleable
import ru.relabs.kurjercontroller.presentation.base.fragment.IFragmentStyleable
import ru.relabs.kurjercontroller.presentation.base.recycler.DelegateAdapter
import ru.relabs.kurjercontroller.presentation.base.tea.debugCollector
import ru.relabs.kurjercontroller.presentation.base.tea.defaultController
import ru.relabs.kurjercontroller.presentation.base.tea.rendersCollector
import ru.relabs.kurjercontroller.presentation.base.tea.sendMessage
import ru.relabs.kurjercontroller.presentation.filters.editor.IFiltersEditorConsumer
import ru.relabs.kurjercontroller.presentation.filters.pager.IFiltersConsumer
import ru.relabs.kurjercontroller.presentation.host.HostActivity
import ru.relabs.kurjercontroller.presentation.taskDetails.IExaminedConsumer
import ru.relabs.kurjercontroller.utils.debug
import ru.relabs.kurjercontroller.utils.extensions.showDialog
import ru.relabs.kurjercontroller.utils.extensions.showSnackbar


/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */

class TasksFragment : BaseFragment(),
    IFragmentStyleable by FragmentStyleable(false),
    IExaminedConsumer,
    IFiltersEditorConsumer,
    IFiltersConsumer {

    private val controller = defaultController(TasksState(), TasksContext(this))
    private var renderJob: Job? = null
    private var shouldShowUpdateRequiredOnResume: Boolean = false
    private var taskUpdateRequiredDialogShowed: Boolean = false

    private val tasksAdapter = DelegateAdapter(
        TasksAdapter.loaderAdapter(),
        TasksAdapter.headerAdapter(),
        TasksAdapter.taskAdapter(
            { uiScope.sendMessage(controller, TasksMessages.msgTaskSelectClick(it)) },
            { uiScope.sendMessage(controller, TasksMessages.msgTaskClicked(it)) }
        ),
        TasksAdapter.blankAdapter(),
        TasksAdapter.searchAdapter {
            uiScope.sendMessage(controller, TasksMessages.msgSearch(it))
        }
    )

    override fun onResume() {
        super.onResume()
//        BaseYandexMapFragment.savedCameraPosition = null //TODO: Remove after yandex map refactor
        if (shouldShowUpdateRequiredOnResume) {
            showUpdateRequiredOnVisible()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val refreshTasks = arguments?.getBoolean(ARG_REFRESH_TASKS, false) ?: false
        controller.start(TasksMessages.msgInit(refreshTasks))
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.stop()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tasks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager =
            LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
        view.rv_list.layoutManager = layoutManager
        view.rv_list.adapter = tasksAdapter

        bindControls(view)

        renderJob = uiScope.launch {
            val renders = listOf(
                TasksRenders.renderList(tasksAdapter),
                TasksRenders.renderLoading(view.loading),
                TasksRenders.renderStartButton(view.btn_start),
                TasksRenders.renderOnlineButton(view.btn_online)
            )
            launch { controller.stateFlow().collect(rendersCollector(renders)) }
            launch { controller.stateFlow().collect(debugCollector { debug(it) }) }
        }
        controller.context.errorContext.attach(view)
        controller.context.showSnackbar = { withContext(Dispatchers.Main) { showSnackbar(resources.getString(it)) } }
        controller.context.showUpdateRequiredOnVisible = ::showUpdateRequiredOnVisible
        controller.context.showPartialTaskItemsLoadingError = ::showPartialTaskItemsLoadingError
        controller.context.showTaskItemsLoadingError = ::showTaskItemsLoadingError
    }

    private fun showPartialTaskItemsLoadingError(tasks: List<Task>) {
        showDialog(
            resources.getString(R.string.tasks_items_partial_update_fail, tasks.joinToString { it.name + "\n" }),
            R.string.ok to {
                uiScope.sendMessage(controller, TasksMessages.msgStartAfterPartialFail())
            }
        )
    }

    private fun showTaskItemsLoadingError() {
        showDialog(
            resources.getString(R.string.tasks_items_update_fail),
            R.string.ok to {}
        )
    }

    override fun onPause() {
        super.onPause()
        taskUpdateRequiredDialogShowed = false
    }

    private fun showUpdateRequiredOnVisible() {
        if (taskUpdateRequiredDialogShowed) {
            return
        }
        if (isVisible) {
            taskUpdateRequiredDialogShowed = true
            showDialog(
                R.string.task_update_required,
                R.string.ok to {
                    uiScope.sendMessage(controller, TasksMessages.msgRefresh())
                    shouldShowUpdateRequiredOnResume = false
                    taskUpdateRequiredDialogShowed = false
                },
                R.string.later to {
                    shouldShowUpdateRequiredOnResume = true
                    taskUpdateRequiredDialogShowed = false
                }
            )
        }
    }

    private fun bindControls(view: View) {
        view.iv_menu.setOnClickListener {
            (activity as? HostActivity)?.changeNavigationDrawerState()
        }
        view.btn_start.setOnClickListener {
            uiScope.sendMessage(controller, TasksMessages.msgStartClicked())
        }
        view.iv_update.setOnClickListener {
            uiScope.sendMessage(controller, TasksMessages.msgRefresh())
        }
        view.btn_online.setOnClickListener {
            uiScope.sendMessage(controller, TasksMessages.msgOnlineClicked())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        renderJob?.cancel()
        controller.context.errorContext.detach()
        controller.context.showSnackbar = {}
        controller.context.showUpdateRequiredOnVisible = {}
        controller.context.showPartialTaskItemsLoadingError = {}
        controller.context.showTaskItemsLoadingError = {}
    }

    override fun onExamined(task: Task) {
        uiScope.sendMessage(controller, TasksMessages.msgTaskExamined(task))
    }

    override fun interceptBackPressed(): Boolean {
        return false
    }

    //Filters Editor Start Clicked (for online)
    override fun onStartClicked(taskId: TaskId, filters: TaskFilters, withPlanned: Boolean) {
        if (taskId.id == -1) {
            uiScope.sendMessage(controller, TasksMessages.msgOnlineFiltersSelected(filters, withPlanned))
        } else {
            FirebaseCrashlytics.getInstance().log("Not expected taskId in onStartClicked")
        }
    }

    override fun onAllFiltersApplied() {
        uiScope.sendMessage(controller, TasksMessages.msgSelectedFiltersUpdated())
    }

    companion object {
        const val ARG_REFRESH_TASKS = "refresh_tasks"
        fun newInstance(refreshTasks: Boolean) = TasksFragment().apply {
            arguments = Bundle().apply {
                putBoolean(ARG_REFRESH_TASKS, refreshTasks)
            }
        }
    }
}