package ru.relabs.kurjercontroller.ui.fragments.taskInfo.delegates

import android.view.LayoutInflater
import android.view.ViewGroup
import ru.relabs.kurjer.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjer.ui.delegateAdapter.IAdapterDelegate
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.ui.fragments.taskInfo.TaskInfoModel
import ru.relabs.kurjercontroller.ui.fragments.taskInfo.holders.InfoTableHeaderHolder

/**
 * Created by ProOrange on 29.08.2018.
 */
class InfoHeaderDelegate : IAdapterDelegate<TaskInfoModel> {
    override fun isForViewType(data: List<TaskInfoModel>, position: Int): Boolean {
        return data[position] is TaskInfoModel.DetailsTableHeader
    }

    override fun onBindViewHolder(holder: BaseViewHolder<TaskInfoModel>, data: List<TaskInfoModel>, position: Int) {
        holder.onBindViewHolder(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<TaskInfoModel> {
        return InfoTableHeaderHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.holder_task_details_list_header,
                parent,
                false
            )
        )
    }
}