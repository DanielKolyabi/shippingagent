package ru.relabs.kurjercontroller.presentation.fragmentsOld

import android.widget.AutoCompleteTextView

/**
 * Created by ProOrange on 27.11.2018.
 */
interface ISearchableFragment {
    fun onSearchItems(filter: String): List<String>
    fun onItemSelected(item: String, searchView: AutoCompleteTextView)
}