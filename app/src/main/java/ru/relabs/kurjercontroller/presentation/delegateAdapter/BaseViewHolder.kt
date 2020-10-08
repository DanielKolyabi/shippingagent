package ru.relabs.kurjercontroller.presentation.delegateAdapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by ProOrange on 11.08.2018.
 */
abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun onBindViewHolder(item: T)
}