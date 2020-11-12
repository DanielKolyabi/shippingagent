package ru.relabs.kurjercontroller.presentation.reportPager

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.fragment_report_pager.view.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.models.TaskId
import ru.relabs.kurjercontroller.domain.models.TaskItemId
import ru.relabs.kurjercontroller.presentation.base.fragment.BaseFragment
import ru.relabs.kurjercontroller.presentation.base.recycler.DelegateAdapter
import ru.relabs.kurjercontroller.presentation.base.tea.debugCollector
import ru.relabs.kurjercontroller.presentation.base.tea.defaultController
import ru.relabs.kurjercontroller.presentation.base.tea.rendersCollector
import ru.relabs.kurjercontroller.presentation.base.tea.sendMessage
import ru.relabs.kurjercontroller.utils.debug
import java.util.*


/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */

class ReportPagerFragment : BaseFragment() {

    private val controller = defaultController(ReportPagerState(), ReportPagerContext())
    private var renderJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val taskIds = arguments?.getParcelableArrayList<TaskItemWithTaskIds>(ARG_TASKS)?.toList()
        val selectedTask = arguments?.getParcelable<TaskItemWithTaskIds>(ARG_SELECTED_TASK)
        if (taskIds == null) {
            FirebaseCrashlytics.getInstance().log("taskIds is null")
            return
        }
        controller.start(ReportPagerMessages.msgInit(taskIds, selectedTask))
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
        return inflater.inflate(R.layout.fragment_report_pager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val taskButtonsAdapter = DelegateAdapter(
            ReportPagerTaskAdapter.taskButtonAdapter {
                uiScope.sendMessage(controller, ReportPagerMessages.msgTaskClicked(it))
            }
        )

        val layoutManager =
            LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
        view.tasks_list.layoutManager = layoutManager
        view.tasks_list.adapter = taskButtonsAdapter

        val pagerAdapter = ReportPagerAdapter(requireFragmentManager())
        view.view_pager.adapter = pagerAdapter

        bindControls(view)

        renderJob = uiScope.launch {
            val renders = listOf(
                ReportPagerRenders.renderLoading(view.loading),
                ReportPagerRenders.renderTasks(taskButtonsAdapter, view.tasks_list),
                ReportPagerRenders.renderPager(pagerAdapter),
                ReportPagerRenders.renderTitle(view.tv_title)
            )
            launch { controller.stateFlow().collect(rendersCollector(renders)) }
            launch { controller.stateFlow().collect(debugCollector { debug(it) }) }
        }
        controller.context.errorContext.attach(view)
    }

    private fun bindControls(
        view: View
    ) {
        view.iv_menu.setOnClickListener {
            uiScope.sendMessage(controller, ReportPagerMessages.msgNavigateBack())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        renderJob?.cancel()
        controller.context.errorContext.detach()
    }

    override fun interceptBackPressed(): Boolean {
        return false
    }

    companion object {
        const val ARG_TASKS = "tasks"
        const val ARG_SELECTED_TASK = "selected_task"

        fun newInstance(taskWithItemIds: List<TaskItemWithTaskIds>, selectedItem: TaskItemWithTaskIds) =
            ReportPagerFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_TASKS, ArrayList(taskWithItemIds))
                    putParcelable(ARG_SELECTED_TASK, selectedItem)
                }
            }
    }
}

@Parcelize
data class TaskItemWithTaskIds(
    val taskId: TaskId,
    val taskItemId: TaskItemId
) : Parcelable