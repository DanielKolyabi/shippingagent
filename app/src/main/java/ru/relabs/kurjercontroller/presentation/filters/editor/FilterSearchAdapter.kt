package ru.relabs.kurjercontroller.presentation.filters.editor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filterable
import android.widget.TextView
import ru.relabs.kurjercontroller.domain.models.TaskFilter

/**
 * Created by ProOrange on 22.03.2019.
 */
class FilterSearchAdapter(context: Context) : ArrayAdapter<TaskFilter>(context, 0), Filterable {
    val results: MutableList<TaskFilter> = mutableListOf()

    override fun getView(pos: Int, convertView: View?, parent: ViewGroup): View {
        val view = LayoutInflater.from(context).inflate(android.R.layout.simple_dropdown_item_1line, parent, false)
        val filter = getItem(pos)
        view.findViewById<TextView>(android.R.id.text1)?.text = filter.name
        return view
    }

    override fun getItem(index: Int): TaskFilter = results[index]

    override fun getItemId(pos: Int): Long = pos.toLong()

    override fun getCount(): Int = results.size
}