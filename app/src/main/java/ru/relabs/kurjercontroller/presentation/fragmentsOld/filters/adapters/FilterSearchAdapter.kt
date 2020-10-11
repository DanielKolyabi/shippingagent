package ru.relabs.kurjercontroller.presentation.fragmentsOld.filters.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import kotlinx.coroutines.runBlocking
import ru.relabs.kurjercontroller.domain.models.TaskFilter
import ru.relabs.kurjercontroller.providers.interfaces.IFilterSearch
import ru.relabs.kurjercontroller.orEmpty
import ru.relabs.kurjercontroller.utils.Left
import ru.relabs.kurjercontroller.utils.Right
import ru.relabs.kurjercontroller.utils.log
import java.lang.ref.WeakReference

/**
 * Created by ProOrange on 22.03.2019.
 */
class FilterSearchAdapter(
    context: Context,
    val filterSearch: IFilterSearch,
    val filterType: Int,
    val selectedFiltersReference: WeakReference<MutableList<TaskFilter>>,
    val withPlannedProvider: () -> Boolean
) : ArrayAdapter<TaskFilter>(context, 0), Filterable {
    private val results: MutableList<TaskFilter> = mutableListOf()

    override fun getView(pos: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(android.R.layout.simple_dropdown_item_1line, parent, false)
        val filter = getItem(pos)
        view.findViewById<TextView>(android.R.id.text1)?.text = filter.name
        return view
    }


    override fun getItem(index: Int): TaskFilter = results[index]

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
            return when(filters){
                is Right -> FilterResults().apply {
                    values = filters.value
                    count = filters.value.size
                }
                is Left -> {
                    filters.value.log()
                    FilterResults().apply {
                        values = emptyList<Filter>()
                        count = 0
                    }
                }
            }
        }

        override fun publishResults(constraint: CharSequence?, searchResults: FilterResults?) {
            if (searchResults != null && searchResults.count > 0) {
                results.clear()
                (searchResults.values as? List<TaskFilter>)?.forEach {
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