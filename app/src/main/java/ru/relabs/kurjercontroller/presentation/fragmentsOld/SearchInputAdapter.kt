package ru.relabs.kurjercontroller.presentation.fragmentsOld

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.fragment.app.FragmentManager
import ru.relabs.kurjercontroller.R

/**
 * Created by ProOrange on 27.11.2018.
 */

class SearchInputAdapter(ctx: Context, resId: Int, textId: Int, val fragmentManager: FragmentManager?) :
    ArrayAdapter<String>(ctx, resId, textId) {

    override fun getFilter(): Filter {
        return stringFilter
    }


    val stringFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            if (constraint?.toString().orEmpty().isEmpty()) {
                return FilterResults()
            }
            val current = fragmentManager?.findFragmentById(R.id.fragment_container) as? ISearchableFragment
            current ?: return FilterResults()

            val data = current.onSearchItems(constraint?.toString().orEmpty())

            return FilterResults().apply {
                count = data.size
                values = data
            }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            results ?: return
            val filteredList = results.values;
            if (results.count > 0) {
                clear();
                (filteredList as? List<String>)?.forEach {
                    add(it)
                }
                notifyDataSetChanged();
            }
        }
    }
}