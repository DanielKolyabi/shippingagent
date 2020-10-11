package ru.relabs.kurjercontroller.presentation.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_tasks.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.presentation.base.fragment.BaseFragment
import ru.relabs.kurjercontroller.presentation.base.fragment.FragmentStyleable
import ru.relabs.kurjercontroller.presentation.base.fragment.IFragmentStyleable
import ru.relabs.kurjercontroller.presentation.base.recycler.DelegateAdapter
import ru.relabs.kurjercontroller.presentation.base.tea.debugCollector
import ru.relabs.kurjercontroller.presentation.base.tea.defaultController
import ru.relabs.kurjercontroller.presentation.base.tea.rendersCollector
import ru.relabs.kurjercontroller.presentation.base.tea.sendMessage
import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.base.BaseYandexMapFragment
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
    IExaminedConsumer {

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
        BaseYandexMapFragment.savedCameraPosition = null //TODO: Remove after yandex map refactor
        if(shouldShowUpdateRequiredOnResume){
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
                TasksRenders.renderStartButton(view.btn_start)
            )
            launch { controller.stateFlow().collect(rendersCollector(renders)) }
            launch { controller.stateFlow().collect(debugCollector { debug(it) }) }
        }
        controller.context.errorContext.attach(view)
        controller.context.showSnackbar = { withContext(Dispatchers.Main) { showSnackbar(resources.getString(it)) } }
        controller.context.showUpdateRequiredOnVisible = ::showUpdateRequiredOnVisible
    }

    override fun onPause() {
        super.onPause()
        taskUpdateRequiredDialogShowed = false
    }

    private fun showUpdateRequiredOnVisible() {
        if(taskUpdateRequiredDialogShowed){
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
    }

    override fun onExamined(task: Task) {
        uiScope.sendMessage(controller, TasksMessages.msgTaskExamined(task))
    }

    override fun interceptBackPressed(): Boolean {
        return false
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