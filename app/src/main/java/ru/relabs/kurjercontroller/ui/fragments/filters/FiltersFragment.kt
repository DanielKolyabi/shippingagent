package ru.relabs.kurjercontroller.ui.fragments.filters

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_filters.*
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.database.entities.FilterEntity
import ru.relabs.kurjercontroller.models.FilterModel
import ru.relabs.kurjercontroller.models.TaskFiltersModel
import ru.relabs.kurjercontroller.providers.RemoteFilterSearch
import ru.relabs.kurjercontroller.ui.сustomView.FilterTagLayout
import ru.relabs.kurjercontroller.ui.fragments.filters.adapters.FilterSearchAdapter
import java.lang.ref.WeakReference


/**
 * Created by ProOrange on 18.03.2019.
 */
const val ADDRESS_LIMIT = 300
class FiltersFragment : Fragment() {

    var defaultTextColor: ColorStateList? = null
    var onStartClicked: ((filters: TaskFiltersModel) -> Unit)? = null

    lateinit var filters: TaskFiltersModel
    val presenter = FiltersPresenter(this)
    val filterSearch = RemoteFilterSearch(
        presenter.bgScope,
        application().user.getUserCredentials()?.token ?: ""
    )
    val allFilters: MutableList<FilterModel> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultTextColor = start_button.textColors
        setStartButtonCount("0")

        bindControl()
        fillAllFilters()
        bindAllFilterControls()


        if (allFilters.any { it.isActive() }) {
            presenter.loadFilteredTasksCount(allFilters)
        }

    }

    private fun bindControl() {
        start_button.setOnClickListener {
            onStartClicked?.invoke(presenter.toTaskFiltersModel(allFilters))
        }
    }

    fun setStartButtonCount(count: String) {
        val intCount = count.toIntOrNull()
        if(intCount == null || intCount > ADDRESS_LIMIT || intCount < 1){
            start_button.setTextColor(Color.RED)
            start_button.isEnabled = false
        }else{
            start_button.setTextColor(defaultTextColor)
            start_button.isEnabled = true
        }
        start_button.text = resources.getString(R.string.filter_apply_button, count)
    }

    private fun bindFilterControl(textView: AutoCompleteTextView, container: FilterTagLayout, filterType: Int) {
        context?.let {
            container.onFilterAppear = { filter ->
                allFilters.add(filter)
                presenter.loadFilteredTasksCount(allFilters)
            }
            container.onFilterDisappear = { filter ->
                allFilters.remove(filter)
                presenter.loadFilteredTasksCount(allFilters)
            }
            container.onFilterActiveChangedPredicate = { filter, newActive ->
                if (newActive) {
                    true
                } else {
                    allFilters.filter { it.isActive() }.size > 1
                }
            }
            container.onFilterActiveChanged = {
                presenter.loadFilteredTasksCount(allFilters)
            }

            val adapter = FilterSearchAdapter(
                    it,
                    filterSearch,
                    filterType,
                    WeakReference(allFilters)
                )

            textView.setAdapter(adapter)
            textView.threshold = 0
            textView.setOnClickListener {
                textView?.showDropDown()
            }
            textView.onItemClickListener = object : AdapterView.OnItemClickListener {
                override fun onItemClick(adapter: AdapterView<*>?, view: View?, pos: Int, p3: Long) {
                    val item = adapter?.getItemAtPosition(pos) as? FilterModel
                    item ?: return
                    if (allFilters.contains(item)) {
                        textView.setText("")
                        return
                    }
                    container.add(item)
                    textView.setText("")
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

    private fun fillFilters(view: FilterTagLayout?, filters: List<FilterModel>) {
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
            filters = it.getParcelable("filters") ?: TaskFiltersModel.blank()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(filters: TaskFiltersModel?) =
            FiltersFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("filters", filters)
                }
            }
    }
}