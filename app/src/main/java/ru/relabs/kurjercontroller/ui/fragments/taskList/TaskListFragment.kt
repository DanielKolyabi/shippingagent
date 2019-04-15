package ru.relabs.kurjercontroller.ui.fragments.taskList


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_tasklist.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.relabs.kurjer.ui.delegateAdapter.DelegateAdapter
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.activity
import ru.relabs.kurjercontroller.models.TaskModel
import ru.relabs.kurjercontroller.ui.fragments.ISearchableFragment
import ru.relabs.kurjercontroller.ui.fragments.taskList.delegates.HeaderDelegate
import ru.relabs.kurjercontroller.ui.fragments.taskList.delegates.LoaderDelegate
import ru.relabs.kurjercontroller.ui.fragments.taskList.delegates.TaskDelegate
import java.util.*

class TaskListFragment : Fragment(), ISearchableFragment {
    override fun onSearchItems(filter: String): List<String> {
        return listOf()
        //TODO: Fix search
//        return adapter.data.asSequence()
//            .filter {
//                it is TaskListModel.TaskItem
//            }
//            .filter {
//                val task = (it as? TaskListModel.TaskItem)?.task
//                task?.publishers?.any { it.name.toLowerCase().contains(filter.toLowerCase()) } ?: false
//            }
//            .map {
//                it as TaskListModel.TaskItem
//            }
//            .toList()
    }

    override fun onItemSelected(item: String, searchView: AutoCompleteTextView) {
        /*val itemIndex = adapter.data.indexOfFirst {
            if (it is TaskListModel.TaskItem) {
                "${it.task.publisher} №${it.task.edition}".contains(item)
            } else {
                false
            }
        }
        if (itemIndex < 0) {
            return
        }
        tasks_list.smoothScrollToPosition(itemIndex)*/
    }

    var shouldNetworkUpdate: Boolean = false
    val presenter = TaskListPresenter(this)
    val adapter = DelegateAdapter<TaskListModel>().apply {
        addDelegate(HeaderDelegate())
        addDelegate(TaskDelegate(
            { presenter.onTaskSelected(it) },
            { presenter.onTaskClicked(it) }
        ))
        addDelegate(LoaderDelegate())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        presenter.updateStartButton()

        tasks_list?.layoutManager = LinearLayoutManager(context)
        tasks_list?.adapter = adapter

        start_button?.setOnClickListener {
            presenter.onStartClicked()
        }
        online_button?.setOnClickListener {
            presenter.onOnlineClicked()
        }
        activity()?.findViewById<View>(R.id.refresh_button)?.setOnClickListener {
            presenter.bgScope.launch {
                presenter.performNetworkUpdate()
            }
        }

        if (shouldNetworkUpdate) {
            presenter.bgScope.launch {
                presenter.performNetworkUpdate()
                shouldNetworkUpdate = false
            }
        } else {
            presenter.bgScope.launch {
                presenter.loadTasks()
            }
        }

    }


    suspend fun showLoading(visible: Boolean) = withContext(Dispatchers.Main) {
        if (visible) {
            online_button?.isEnabled = false
            adapter.data.add(0, TaskListModel.Loader)
            tasks_list?.isNestedScrollingEnabled = false
            adapter.notifyDataSetChanged()
        } else {
            online_button?.isEnabled = true
            adapter.data.removeAll {
                it is TaskListModel.Loader
            }
            tasks_list?.isNestedScrollingEnabled = false
            adapter.notifyDataSetChanged()
        }
    }

    suspend fun populateTaskList(tasks: List<TaskModel>) = withContext(Dispatchers.Main) {
        adapter.data.clear()
        tasks.groupBy {
            it.startControlDate
        }.forEach { (date, tasks) ->
            val title = date.dayOfWeek().getAsText(Locale("ru", "RU")).capitalize() + ", " + date.toString("dd.MM.yyyy")
            adapter.data.add(TaskListModel.GroupHeader(title))
            adapter.data.addAll(tasks.map { TaskListModel.TaskItem(it, false, false) })
        }
        adapter.notifyDataSetChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tasklist, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            shouldNetworkUpdate = it.getBoolean("should_network_update", false)
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(shouldNetworkUpdate: Boolean) =
            TaskListFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("should_network_update", shouldNetworkUpdate)
                }
            }
    }
}
