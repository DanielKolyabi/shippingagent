package ru.relabs.kurjercontroller.ui.fragments.filters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import kotlinx.coroutines.runBlocking
import ru.relabs.kurjercontroller.models.FilterModel
import ru.relabs.kurjercontroller.network.IFilterSearch

/**
 * Created by ProOrange on 22.03.2019.
 */
class FilterSearchAdapter(
    context: Context,
    val filterSearch: IFilterSearch,
    val filterName: String,
    val selectedFiltersContainer: FilterTagLayout
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

    val filterObj = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filters = runBlocking {
                filterSearch.searchFilter(
                    filterName,
                    constraint.toString(),
                    selectedFiltersContainer.currentTags.map { it.id }
                ).await()
            }
            return FilterResults().apply {
                values = filters
                count = filters.size
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
                notifyDataSetInvalidated()
            }
        }
    }

    override fun getFilter(): Filter = filterObj
}