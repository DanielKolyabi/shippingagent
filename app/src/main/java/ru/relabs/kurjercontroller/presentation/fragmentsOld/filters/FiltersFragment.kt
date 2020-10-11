package ru.relabs.kurjercontroller.presentation.fragmentsOld.filters

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_filters.*
import ru.relabs.kurjercontroller.BuildConfig
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.data.database.entities.FilterEntity
import ru.relabs.kurjercontroller.domain.models.TaskFilter
import ru.relabs.kurjercontroller.domain.models.TaskFilters
import ru.relabs.kurjercontroller.presentation.fragmentsOld.filters.adapters.FilterSearchAdapter
import ru.relabs.kurjercontroller.presentation.сustomView.FilterTagLayout
import ru.relabs.kurjercontroller.presentation.сustomView.InstantAutocompleteTextView
import ru.relabs.kurjercontroller.providers.RemoteFilterSearch
import java.lang.ref.WeakReference


/**
 * Created by ProOrange on 18.03.2019.
 */

class FiltersFragment : Fragment() {

    var onStartClicked: ((filters: TaskFilters, withPlanned: Boolean) -> Unit)? = null
    lateinit var adapter: FilterSearchAdapter
    lateinit var filters: TaskFilters
    var withPlanned: Boolean = false

    val presenter = FiltersPresenter(this)
    val filterSearch = RemoteFilterSearch(presenter.bgScope)
    val allFilters: MutableList<TaskFilter> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStartButtonCount(0, 0, false)

        bindControl()
        fillAllFilters()
        bindAllFilterControls()
        planned_tasks?.isChecked = withPlanned

        if (allFilters.any { it.isActive() }) {
            presenter.loadFilteredTasksCount(allFilters, withPlanned)
        }
    }

    private fun bindControl() {
        start_button.setOnClickListener {
            onStartClicked?.invoke(presenter.toTaskFiltersModel(allFilters), planned_tasks?.isChecked ?: false)
        }
        reload_button?.setOnClickListener {
            presenter.loadFilteredTasksCount(allFilters, planned_tasks?.isChecked ?: false)
        }
        planned_tasks?.setOnCheckedChangeListener { _, checked ->
            presenter.loadFilteredTasksCount(allFilters, checked)
        }
    }

    fun setStartButtonCount(closedCount: Int, plannedCount: Int, plannedEnabled: Boolean) {
        if ((closedCount + plannedCount) > BuildConfig.MAX_ADDRESSES_IN_FILTERS || closedCount < 0) {
            start_button?.setTextColor(Color.RED)
            start_button?.isEnabled = false
        } else {
            start_button?.setTextColor(Color.BLACK)
            start_button?.isEnabled = true
        }
        if (closedCount < 0) {
            start_button?.text = "Ошибка"
        } else if (!plannedEnabled) {
            start_button?.text =
                resources.getString(R.string.filter_apply_button, closedCount.toString())
        } else {
            start_button?.text =
                resources.getString(R.string.filter_apply_button, "$closedCount - $plannedCount")
        }
    }

    private fun bindFilterControl(
        textView: InstantAutocompleteTextView,
        container: FilterTagLayout,
        filterType: Int
    ) {
        context?.let {
            container.onFilterAppear = { filter ->
                allFilters.add(filter)
                presenter.loadFilteredTasksCount(allFilters, planned_tasks?.isChecked ?: false)
                textView?.performFiltering()
            }
            container.onFilterDisappear = { filter ->
                allFilters.remove(filter)
                presenter.loadFilteredTasksCount(allFilters, planned_tasks?.isChecked ?: false)
                textView?.performFiltering()
            }
            container.onFilterActiveChangedPredicate = { filter, newActive ->
                if (newActive) {
                    true
                } else {
                    allFilters.filter { it.type == filter.type && it.isActive() }.size > 1
                }
            }
            container.onFilterActiveChanged = {
                presenter.loadFilteredTasksCount(allFilters, planned_tasks?.isChecked ?: false)
            }

            adapter = FilterSearchAdapter(
                it,
                filterSearch,
                filterType,
                WeakReference(allFilters)
            ) { planned_tasks?.isChecked ?: false }

            textView.setAdapter(adapter)
            textView.threshold = 0
            textView.setOnClickListener {
                textView?.showDropDown()
                textView?.performFiltering()
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
                    if (allFilters.contains(item)) {
                        textView?.setText("")
                        return
                    }
                    container?.add(item)
                    textView?.setText("")
                }
            }
        }
    }

    private fun bindAllFilterControls() {
        bindFilterControl(publisher_filter, publisher_filters, FilterEntity.PUBLISHER_FILTER)
        bindFilterControl(brigade_filter, brigade_filters, FilterEntity.BRIGADE_FILTER)
        bindFilterControl(district_filter, district_filters, FilterEntity.DISTRICT_FILTER)
        bindFilterControl(region_filter, region_filters, FilterEntity.REGION_FILTER)
        bindFilterControl(user_filter, user_filters, FilterEntity.USER_FILTER)
    }

    private fun fillAllFilters() {
        fillFilters(publisher_filters, filters.publishers)
        allFilters.addAll(filters.publishers)
        fillFilters(brigade_filters, filters.brigades)
        allFilters.addAll(filters.brigades)
        fillFilters(district_filters, filters.districts)
        allFilters.addAll(filters.districts)
        fillFilters(region_filters, filters.regions)
        allFilters.addAll(filters.regions)
        fillFilters(user_filters, filters.users)
        allFilters.addAll(filters.users)
    }

    private fun fillFilters(view: FilterTagLayout?, filters: List<TaskFilter>) {
        view ?: return
        filters
            .sortedByDescending { it.fixed }
            .forEach {
                view.add(it)
            }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_filters, container, false)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            filters = it.getParcelable("filters") ?: TaskFilters.blank()
            withPlanned = it.getBoolean("with_planned") ?: false
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(filters: TaskFilters?, withPlanned: Boolean?) =
            FiltersFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("filters", filters)
                    putBoolean("with_planned", withPlanned ?: false)
                }
            }
    }
}