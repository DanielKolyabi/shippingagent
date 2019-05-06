package ru.relabs.kurjercontroller.ui.fragments.report

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_report_pager.*
import kotlinx.coroutines.launch
import ru.relabs.kurjer.ui.delegateAdapter.DelegateAdapter
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.activity
import ru.relabs.kurjercontroller.models.EntranceModel
import ru.relabs.kurjercontroller.models.TaskItemModel
import ru.relabs.kurjercontroller.models.TaskModel
import ru.relabs.kurjercontroller.ui.extensions.setVisible
import ru.relabs.kurjercontroller.ui.fragments.report.adapters.ReportPagerAdapter
import ru.relabs.kurjercontroller.ui.fragments.report.delegates.ReportTasksDelegate
import ru.relabs.kurjercontroller.ui.fragments.report.models.ReportTasksListModel
import java.util.*

/**
 * Created by ProOrange on 18.03.2019.
 */
class ReportPagerFragment : Fragment() {

    var tasks: MutableList<TaskModel> = mutableListOf()
    var taskItems: MutableList<TaskItemModel> = mutableListOf()
    var selectedTask: Pair<Int, Int> = Pair(0,0)
    val presenter = ReportPagerPresenter(this)
    lateinit var pagerAdapter: ReportPagerAdapter
    val taskListAdapter = DelegateAdapter<ReportTasksListModel>()

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            val taskId = intent.getIntExtra("task_closed", 0)
            val taskItemId = intent.getIntExtra("task_item_closed", 0)
            val entranceNumber = intent.getIntExtra("entrance_number_closed", 0)

            presenter.bgScope.launch {
                presenter.onEntranceClosedRemote(taskId, taskItemId, entranceNumber)
            }
        }
    }
    private val intentFilter = IntentFilter("NOW")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loading.setVisible(false)
        taskListAdapter.addDelegate(ReportTasksDelegate { presenter.onTaskChanged(it) })
        tasks_list.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        tasks_list.adapter = taskListAdapter

        activity()?.changeTitle(getTitle())

        updateTasks()
        presenter.updatePagerAdapter()
    }

    fun updateTasks() {
        tasks_list.setVisible(tasks.size > 1)
        taskListAdapter.data.clear()
        taskListAdapter.data.addAll(tasks.mapIndexed { i, it ->
            ReportTasksListModel.TaskButton(it, taskItems[i], i, taskItems[i].id == selectedTask.second && taskItems[i].taskId == selectedTask.first)
        })
        taskListAdapter.notifyDataSetChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_report_pager, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tasks = it.getParcelableArrayList("tasks") ?: mutableListOf()
            taskItems = it.getParcelableArrayList("task_items") ?: mutableListOf()
            selectedTask = Pair(it.getInt("selected_task_id"), it.getInt("selected_task_item_id"))
        }
        activity?.registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onDestroy() {
        activity?.unregisterReceiver(broadcastReceiver)
        presenter.bgScope.terminate()
        super.onDestroy()
    }

    fun getTitle(): String {
        return taskItems.first().address.name
    }

    companion object {
        @JvmStatic
        fun newInstance(tasks: List<TaskModel>, taskItems: List<TaskItemModel>, selectedTaskId: Int, selectedTaskItemId: Int) =
            ReportPagerFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("tasks", ArrayList(tasks))
                    putParcelableArrayList("task_items", ArrayList(taskItems))
                    putInt("selected_task_id", selectedTaskId)
                    putInt("selected_task_item_id", selectedTaskItemId)
                }
            }
    }
}