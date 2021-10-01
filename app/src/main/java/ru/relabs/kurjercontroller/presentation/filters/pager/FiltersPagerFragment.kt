package ru.relabs.kurjercontroller.presentation.filters.pager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.terrakok.cicerone.Router
import kotlinx.android.synthetic.main.fragment_filters_pager.view.*
import kotlinx.android.synthetic.main.fragment_report_pager.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskFilters
import ru.relabs.kurjercontroller.domain.models.TaskId
import ru.relabs.kurjercontroller.domain.repositories.DatabaseRepository
import ru.relabs.kurjercontroller.presentation.base.fragment.BaseFragment
import ru.relabs.kurjercontroller.presentation.filters.editor.IFiltersEditorConsumer
import ru.relabs.kurjercontroller.utils.extensions.setVisible
import java.util.*

/**
 * Created by ProOrange on 18.03.2019.
 */
const val FILTERS_CONSUMER_REQUEST_CODE = 533

class FiltersPagerFragment : BaseFragment(),
    IFiltersEditorConsumer,
    KoinComponent {

    private val databaseRepository: DatabaseRepository by inject()
    private val router: Router by inject()
    private var pagerAdapter: FiltersPagerAdapter<FiltersPagerFragment>? = null
    private var tasks: List<Task> = emptyList()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loading.setVisible(false)
        pagerAdapter = FiltersPagerAdapter(
            tasks,
            this,
            requireFragmentManager()
        )
        view.view_pager.adapter = pagerAdapter
        view.view_pager.currentItem = 0

        view.iv_menu.setOnClickListener {
            router.exit()
        }
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
            tasks = it.getParcelableArrayList("tasks") ?: emptyList()
        }
    }

    override fun onDestroy() {
        supervisor.cancel()
        super.onDestroy()
    }

    override fun onStartClicked(taskId: TaskId, filters: TaskFilters, withPlanned: Boolean) {
        uiScope.launch(Dispatchers.IO) {
            databaseRepository.saveFilters(taskId, filters, withPlanned)

            withContext(Dispatchers.Main) {
                tasks = tasks.filter { it.id != taskId }
                if (tasks.isEmpty()) {
                    router.exit()
                    (targetFragment as? IFiltersConsumer)?.onAllFiltersApplied()
                } else {
                    pagerAdapter?.tasks = tasks
                    pagerAdapter?.notifyDataSetChanged()
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun <T> newInstance(tasks: List<Task>, targetFragment: T) where T : Fragment, T : IFiltersConsumer =
            FiltersPagerFragment().apply {
                setTargetFragment(targetFragment, FILTERS_CONSUMER_REQUEST_CODE)
                arguments = Bundle().apply {
                    putParcelableArrayList("tasks", ArrayList(tasks.map { it.copy(taskItems = mutableListOf()) }))
                }
            }
    }
}