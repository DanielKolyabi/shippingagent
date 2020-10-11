package ru.relabs.kurjercontroller.presentation.fragmentsOld.taskList.delegates

import android.view.LayoutInflater
import android.view.ViewGroup
import ru.relabs.kurjercontroller.presentation.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.presentation.delegateAdapter.IAdapterDelegate
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.presentation.fragmentsOld.taskList.TaskListModel
import ru.relabs.kurjercontroller.presentation.fragmentsOld.taskList.holders.LoaderHolder

/**
 * Created by ProOrange on 20.03.2019.
 */
class LoaderDelegate : IAdapterDelegate<TaskListModel> {

    override fun isForViewType(data: List<TaskListModel>, position: Int): Boolean {
        return data[position] is TaskListModel.Loader
    }

    override fun onBindViewHolder(holder: BaseViewHolder<TaskListModel>, data: List<TaskListModel>, position: Int) {
        holder.onBindViewHolder(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<TaskListModel> {
        return LoaderHolder(LayoutInflater.from(parent.context).inflate(R.layout.holder_loading, parent, false))
    }
}
