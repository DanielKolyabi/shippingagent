package ru.relabs.kurjercontroller.ui.fragments.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_report_pager.*
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.models.TaskItemModel
import ru.relabs.kurjercontroller.models.TaskModel
import ru.relabs.kurjercontroller.ui.extensions.setVisible
import ru.relabs.kurjercontroller.ui.fragments.report.adapters.ReportPagerAdapter
import java.util.*

/**
 * Created by ProOrange on 18.03.2019.
 */
class ReportPagerFragment : Fragment() {

    var tasks: List<TaskModel> = listOf()
    var taskItems: List<TaskItemModel> = listOf()
    var selectedTaskItemId: Int = 0
    val presenter = ReportPagerPresenter(this)
    lateinit var pagerAdapter: ReportPagerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loading.setVisible(false)
        presenter.updatePagerAdapter()
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
            tasks = it.getParcelableArrayList("tasks") ?: listOf()
            taskItems = it.getParcelableArrayList("task_items") ?: listOf()
            selectedTaskItemId = it.getInt("selected_task_id")
        }
    }

    override fun onDestroy() {
        presenter.bgScope.terminate()
        super.onDestroy()
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