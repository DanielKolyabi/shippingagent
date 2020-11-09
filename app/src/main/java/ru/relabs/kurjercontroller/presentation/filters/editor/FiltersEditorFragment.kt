package ru.relabs.kurjercontroller.presentation.filters.editor

import android.content.Context
import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.fragment_filters.view.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.models.FilterType
import ru.relabs.kurjercontroller.domain.models.TaskFilter
import ru.relabs.kurjercontroller.domain.models.TaskFilters
import ru.relabs.kurjercontroller.domain.models.TaskId
import ru.relabs.kurjercontroller.presentation.base.TextChangeListener
import ru.relabs.kurjercontroller.presentation.base.fragment.BaseFragment
import ru.relabs.kurjercontroller.presentation.base.tea.debugCollector
import ru.relabs.kurjercontroller.presentation.base.tea.defaultController
import ru.relabs.kurjercontroller.presentation.base.tea.rendersCollector
import ru.relabs.kurjercontroller.presentation.base.tea.sendMessage
import ru.relabs.kurjercontroller.presentation.сustomView.FilterTagLayout
import ru.relabs.kurjercontroller.presentation.сustomView.InstantAutocompleteTextView
import ru.relabs.kurjercontroller.utils.debug


/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */

class FiltersEditorFragment : BaseFragment(), KoinComponent {

    private val controller = defaultController(FiltersEditorState(), FiltersEditorContext())
    private var renderJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val taskId = arguments?.getParcelable<TaskId>(ARG_KEY_TASK_ID)
        val filters = arguments?.getParcelable(ARG_KEY_FILTERS) ?: TaskFilters.blank()
        val withPlanned = arguments?.getBoolean(ARG_KEY_PLANNED) ?: false
        val withNavBar = arguments?.getBoolean(ARG_WITH_NAV_BAR) ?: false

        if (taskId == null) {
            FirebaseCrashlytics.getInstance().log("taskId is null")
            return
        }

        controller.start(FiltersEditorMessages.msgInit(taskId, filters, withPlanned, withNavBar))
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
        return inflater.inflate(R.layout.fragment_filters, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ctx = requireContext()
        val filterListRenders = listOf(
            *bindFilterControlAndMakeRenders(ctx, view.publisher_filter, view.publisher_filters, FilterType.Publisher),
            *bindFilterControlAndMakeRenders(ctx, view.brigade_filter, view.brigade_filters, FilterType.Brigade),
            *bindFilterControlAndMakeRenders(ctx, view.district_filter, view.district_filters, FilterType.District),
            *bindFilterControlAndMakeRenders(ctx, view.region_filter, view.region_filters, FilterType.Region),
            *bindFilterControlAndMakeRenders(ctx, view.user_filter, view.user_filters, FilterType.User)
        )

        bindControls(view)

        renderJob = uiScope.launch {
            val renders = filterListRenders + listOf(
                FiltersEditorRenders.renderPlannedCheck(view.planned_tasks),
                FiltersEditorRenders.renderStartButton(view.start_button),
                FiltersEditorRenders.renderNavBar(view.top_app_bar)
            )
            launch { controller.stateFlow().collect(rendersCollector(renders)) }
            launch { controller.stateFlow().collect(debugCollector { debug(it) }) }
        }
        controller.context.errorContext.attach(view)
        controller.context.performStart = ::performStart
    }

    private fun performStart(taskId: TaskId, filters: List<TaskFilter>, withPlanned: Boolean) {
        val taskFilters = TaskFilters(
            publishers = filters.filter { it.type == FilterType.Publisher },
            districts = filters.filter { it.type == FilterType.District },
            regions = filters.filter { it.type == FilterType.Region },
            brigades = filters.filter { it.type == FilterType.Brigade },
            users = filters.filter { it.type == FilterType.User }
        )
        (targetFragment as? IFiltersEditorConsumer)?.onStartClicked(taskId, taskFilters, withPlanned)
    }

    private fun bindControls(view: View) {
        view.planned_tasks.setOnCheckedChangeListener { _, isChecked ->
            uiScope.sendMessage(controller, FiltersEditorMessages.msgPlannedChanged(isChecked))
        }
        view.reload_button.setOnClickListener {
            uiScope.sendMessage(controller, FiltersEditorMessages.msgRefreshCounts())
        }
        view.start_button.setOnClickListener {
            uiScope.sendMessage(controller, FiltersEditorMessages.msgStartClicked())
        }
        view.iv_menu.setOnClickListener {
            uiScope.sendMessage(controller, FiltersEditorMessages.msgNavigateBack())
        }
    }

    private fun makeFilterRenders(
        adapter: FilterSearchAdapter,
        tags: FilterTagLayout,
        text: EditText,
        type: FilterType,
        watcher: TextWatcher
    ): Array<FiltersEditorRender> {
        return arrayOf(
            FiltersEditorRenders.renderFilterResults(adapter, type),
            FiltersEditorRenders.renderFilters(tags, type),
            FiltersEditorRenders.renderFilterSearch(text, type, watcher)
        )
    }

    private fun bindFilterControlAndMakeRenders(
        context: Context,
        textView: InstantAutocompleteTextView,
        container: FilterTagLayout,
        type: FilterType
    ): Array<FiltersEditorRender> {
        val adapter = FilterSearchAdapter(context)
        container.onFilterRemoveClicked = {
            uiScope.sendMessage(controller, FiltersEditorMessages.msgFilterRemoveClicked(it))
            uiScope.sendMessage(controller, FiltersEditorMessages.msgSearchFilter(textView.text.toString(), type))
        }

        textView.setAdapter(adapter)
        textView.threshold = 0
        textView.setOnClickListener {
            if(!textView.isPopupShowing){
                uiScope.sendMessage(controller, FiltersEditorMessages.msgSearchFilter(textView.text.toString(), type))
                textView.showDropDown()
            }
        }
        textView.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                uiScope.sendMessage(controller, FiltersEditorMessages.msgSearchFieldClicked(type))
                uiScope.sendMessage(controller, FiltersEditorMessages.msgSearchFilter(textView.text.toString(), type))
            }
        }
        textView.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                adapter: AdapterView<*>?,
                view: View?,
                pos: Int,
                p3: Long
            ) {
                val item = adapter?.getItemAtPosition(pos) as? TaskFilter
                item ?: return
                uiScope.sendMessage(controller, FiltersEditorMessages.msgSearchFilterSelected(item, type))
            }
        }

        val textWatcher = TextChangeListener {
            uiScope.sendMessage(controller, FiltersEditorMessages.msgSearchFilter(it, type))
        }
        textView.addTextChangedListener(textWatcher)

        return makeFilterRenders(adapter, container, textView, type, textWatcher)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        renderJob?.cancel()
        controller.context.performStart = { _, _, _ -> Unit }
        controller.context.errorContext.detach()
    }

    override fun interceptBackPressed(): Boolean {
        return false
    }

    companion object {
        const val ARG_KEY_FILTERS = "filters"
        const val ARG_KEY_TASK_ID = "taskId"
        const val ARG_KEY_PLANNED = "with_planned"
        const val ARG_WITH_NAV_BAR = "with_nav_bar"


        fun <T> newInstance(
            taskId: TaskId,
            filters: TaskFilters?,
            withPlanned: Boolean?,
            withNavBar: Boolean?,
            targetFragment: T
        ) where T : Fragment, T : IFiltersEditorConsumer = FiltersEditorFragment().apply {
            setTargetFragment(targetFragment, -1)
            arguments = Bundle().apply {
                putParcelable(ARG_KEY_FILTERS, filters)
                putParcelable(ARG_KEY_TASK_ID, taskId)
                putBoolean(ARG_KEY_PLANNED, withPlanned ?: false)
                putBoolean(ARG_WITH_NAV_BAR, withNavBar ?: false)
            }
        }
    }
}