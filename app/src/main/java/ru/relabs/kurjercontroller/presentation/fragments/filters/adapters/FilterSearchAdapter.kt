package ru.relabs.kurjercontroller.presentation.fragments.filters.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import kotlinx.coroutines.runBlocking
import ru.relabs.kurjercontroller.logError
import ru.relabs.kurjercontroller.domain.models.FilterModel
import ru.relabs.kurjercontroller.providers.interfaces.IFilterSearch
import ru.relabs.kurjercontroller.orEmpty
import java.lang.ref.WeakReference

/**
 * Created by ProOrange on 22.03.2019.
 */
class FilterSearchAdapter(
    context: Context,
    val filterSearch: IFilterSearch,
    val filterType: Int,
    val selectedFiltersReference: WeakReference<MutableList<FilterModel>>,
    val withPlannedProvider: () -> Boolean
) : ArrayAdapter<FilterModel>(context, 0), Filterable {
    private val results: MutableList<FilterModel> = mutableListOf()

    override fun getView(pos: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(android.R.layout.simple_dropdown_item_1line, parent, false)
        val filter = getItem(pos)
        view.findViewById<TextView>(android.R.id.text1)?.text = filter.name
        return view
    }


    override fun getItem(index: Int): FilterModel = results[index]

    override fun getItemId(pos: Int): Long = pos.toLong()

    override fun getCount(): Int = results.size

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filters = runBlocking {
                filterSearch.searchFilters(
                    filterType,
                    constraint.toString(),
                    selectedFiltersReference.get().orEmpty(),
                    withPlannedProvider()
                ).await()
            }
            if(filters.error != null){
                filters.error.logError()
                return FilterResults().apply{
                    values = null
                    count = 0
                }
            }
            return FilterResults().apply {
                values = filters.result
                count = filters.result.size
            }
        }

        override fun publishResults(constraint: CharSequence?, searchResults: FilterResults?) {
            if (searchResults != null && searchResults.count > 0) {
                results.clear()
                (searchResults.values as? List<FilterModel>)?.forEach {
                    results.add(it)
                }
                notifyDataSetChanged()
            } else {
                results.clear()
                notifyDataSetInvalidated()
            }
        }
    }
}