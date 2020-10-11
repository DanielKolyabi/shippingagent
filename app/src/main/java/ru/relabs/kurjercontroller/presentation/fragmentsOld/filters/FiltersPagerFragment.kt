package ru.relabs.kurjercontroller.presentation.fragmentsOld.filters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_report_pager.*
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.models.TaskModel
import ru.relabs.kurjercontroller.utils.extensions.setVisible
import ru.relabs.kurjercontroller.presentation.fragmentsOld.filters.adapters.FiltersPagerAdapter
import java.util.*

/**
 * Created by ProOrange on 18.03.2019.
 */
class FiltersPagerFragment : Fragment() {

    var tasks: MutableList<TaskModel> = mutableListOf()
    val presenter = FiltersPagerPresenter(this)
    lateinit var pagerAdapter: FiltersPagerAdapter
    var onAllFiltersApplied: (() -> Unit)? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loading.setVisible(false)

        updatePagerAdapter()
    }

    private fun updatePagerAdapter() {
        val manager = fragmentManager ?: return
        pagerAdapter = FiltersPagerAdapter(tasks, manager) { task, newFilters, withPlanned ->
            presenter.onStartClicked(task, newFilters, withPlanned)
        }
        view_pager?.adapter = pagerAdapter
        view_pager?.currentItem = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_filters_pager, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tasks = it.getParcelableArrayList("tasks") ?: mutableListOf()
        }
    }

    override fun onDestroy() {
        presenter.bgScope.terminate()
        super.onDestroy()
    }

    companion object {
        @JvmStatic
        fun newInstance(tasks: List<TaskModel>) =
            FiltersPagerFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("tasks", ArrayList(tasks.map { it.copy(taskItems = mutableListOf()) }))
                }
            }
    }
}