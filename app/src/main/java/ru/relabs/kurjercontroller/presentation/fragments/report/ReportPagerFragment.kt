package ru.relabs.kurjercontroller.presentation.fragments.report

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_report_pager.*
import kotlinx.coroutines.launch
import ru.relabs.kurjercontroller.presentation.delegateAdapter.DelegateAdapter
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.activity
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.domain.models.TaskModel
import ru.relabs.kurjercontroller.utils.extensions.setVisible
import ru.relabs.kurjercontroller.presentation.fragments.report.adapters.ReportPagerAdapter
import ru.relabs.kurjercontroller.presentation.fragments.report.delegates.ReportTasksDelegate
import ru.relabs.kurjercontroller.presentation.fragments.report.models.ReportTasksListModel
import java.util.*

/**
 * Created by ProOrange on 18.03.2019.
 */
class ReportPagerFragment : Fragment() {

    val tasks: MutableList<TaskModel> = mutableListOf()
    val taskItems: MutableList<TaskItem> = mutableListOf()
    var selectedTask: Pair<Int, Int> = Pair(0, 0)
    val presenter = ReportPagerPresenter(this)
    lateinit var pagerAdapter: ReportPagerAdapter
    val taskListAdapter = DelegateAdapter<ReportTasksListModel>()
    var taskIds: IntArray? = null
    var taskItemIds: List<TaskItemIdWithTaskId>? = null

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
        presenter.initTasks(taskIds, taskItemIds)
    }

    fun updateTasks() {
        tasks_list?.setVisible(tasks.size > 1)
        taskListAdapter.data.clear()
        taskListAdapter.data.addAll(tasks.mapIndexed { i, it ->
            ReportTasksListModel.TaskButton(
                it,
                taskItems[i],
                i,
                taskItems[i].id == selectedTask.second && taskItems[i].taskId == selectedTask.first
            )
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
            selectedTask = Pair(it.getInt("selected_task_id"), it.getInt("selected_task_item_id"))
            taskIds = it.getIntArray("task_ids")
            taskItemIds = it.getParcelableArrayList("task_item_ids")
        }
        activity?.registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onDestroy() {
        activity?.unregisterReceiver(broadcastReceiver)
        presenter.bgScope.terminate()
        super.onDestroy()
    }

    fun getTitle(): String {
        return taskItems.firstOrNull()?.address?.name ?: "Загрузка"
    }

    companion object {
        @JvmStatic
        fun newInstance(
            tasks: List<TaskModel>,
            taskItems: List<TaskItem>,
            selectedTaskId: Int,
            selectedTaskItemId: Int
        ) =
            ReportPagerFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("task_item_ids", ArrayList(taskItems.map { TaskItemIdWithTaskId(it.taskId, it.id) }))
                    putIntArray("task_ids", tasks.map { it.id }.toIntArray())
//                    putParcelableArrayList("tasks", ArrayList(tasks))
//                    putParcelableArrayList("task_items", ArrayList(taskItems))
                    putInt("selected_task_id", selectedTaskId)
                    putInt("selected_task_item_id", selectedTaskItemId)
                }
            }
    }
}

data class TaskItemIdWithTaskId(
    val taskId: Int,
    val taskItemId: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(taskId)
        parcel.writeInt(taskItemId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TaskItemIdWithTaskId> {
        override fun createFromParcel(parcel: Parcel): TaskItemIdWithTaskId {
            return TaskItemIdWithTaskId(parcel)
        }

        override fun newArray(size: Int): Array<TaskItemIdWithTaskId?> {
            return arrayOfNulls(size)
        }
    }
}