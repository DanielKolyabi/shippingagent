package ru.relabs.kurjercontroller.ui.fragments.filters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_filters.*
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.models.FilterModel
import ru.relabs.kurjercontroller.models.TaskFiltersModel
import ru.relabs.kurjercontroller.network.MockFilterSearch


/**
 * Created by ProOrange on 18.03.2019.
 */

const val FILTERS_REQUEST_CODE = 1

class FiltersFragment() : Fragment() {

    lateinit var filters: TaskFiltersModel
    val presenter = FiltersPresenter(this)
    val filterSearch = MockFilterSearch

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fillAllFilters()
        bindAllFilterControls()
    }

    private fun bindFilterControl(textView: AutoCompleteTextView, container: FilterTagLayout, filterName: String){
        context?.let{
            textView.setAdapter(FilterSearchAdapter(it, filterSearch, filterName, container))
            textView.onItemClickListener = object: AdapterView.OnItemClickListener{
                override fun onItemClick(adapter: AdapterView<*>?, view: View?, pos: Int, p3: Long) {
                    val item = adapter?.getItemAtPosition(pos) as? FilterModel
                    item ?: return
                    container.add(item)
                    textView.setText("")
                }
            }
        }
    }

    private fun bindAllFilterControls(){
        bindFilterControl(publisher_filter, publisher_filters, "izd")
        bindFilterControl(area_filter, area_filters, "izd")
        bindFilterControl(brigade_filter, brigade_filters, "izd")
        bindFilterControl(city_filter, city_filters, "izd")
        bindFilterControl(district_filter, district_filters, "izd")
        bindFilterControl(region_filter, region_filters, "izd")
        bindFilterControl(street_filter, street_filters, "izd")
        bindFilterControl(user_filter, user_filters, "izd")
    }

    private fun fillAllFilters(){
        fillFilters(publisher_filters, filters.publishers)
        fillFilters(area_filters, filters.areas)
        fillFilters(brigade_filters, filters.brigades)
        fillFilters(city_filters, filters.cities)
        fillFilters(district_filters, filters.districts)
        fillFilters(region_filters, filters.regions)
        fillFilters(street_filters, filters.streets)
        fillFilters(user_filters, filters.users)
    }

    private fun fillFilters(view: FilterTagLayout?, filters: List<FilterModel>){
        view ?: return
        filters.forEach {
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