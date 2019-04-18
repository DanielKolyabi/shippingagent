package ru.relabs.kurjercontroller.ui.fragments.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_report_pager.*
import ru.relabs.kurjer.ui.delegateAdapter.DelegateAdapter
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.activity
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
    var selectedTaskItemId: Int = 0
    val presenter = ReportPagerPresenter(this)
    lateinit var pagerAdapter: ReportPagerAdapter
    val taskListAdapter = DelegateAdapter<ReportTasksListModel>()

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
            ReportTasksListModel.TaskButton(it, taskItems[i], i, taskItems[i].id == selectedTaskItemId)
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
            selectedTaskItemId = it.getInt("selected_task_id")
        }
    }

    override fun onDestroy() {
        presenter.bgScope.terminate()
        super.onDestroy()
    }

    fun getTitle(): String {
        return taskItems.first().address.name
    }

    companion object {
        @JvmStatic
        fun newInstance(tasks: List<TaskModel>, taskItems: List<TaskItemModel>, selectedTaskItemId: Int) =
            ReportPagerFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("tasks", ArrayList(tasks))
                    putParcelableArrayList("task_items", ArrayList(taskItems))
                    putInt("selected_task_id", selectedTaskItemId)
                }
            }
    }
}