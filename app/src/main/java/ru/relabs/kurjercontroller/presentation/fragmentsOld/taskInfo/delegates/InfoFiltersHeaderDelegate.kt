package ru.relabs.kurjercontroller.presentation.fragmentsOld.taskInfo.delegates

import android.view.LayoutInflater
import android.view.ViewGroup
import ru.relabs.kurjercontroller.presentation.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.presentation.delegateAdapter.IAdapterDelegate
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.presentation.fragmentsOld.taskInfo.TaskInfoModel
import ru.relabs.kurjercontroller.presentation.fragmentsOld.taskInfo.holders.InfoTableFiltersHeaderHolder

/**
 * Created by ProOrange on 29.08.2018.
 */
class InfoFiltersHeaderDelegate : IAdapterDelegate<TaskInfoModel> {
    override fun isForViewType(data: List<TaskInfoModel>, position: Int): Boolean {
        return data[position] is TaskInfoModel.DetailsFiltersTableHeader
    }

    override fun onBindViewHolder(holder: BaseViewHolder<TaskInfoModel>, data: List<TaskInfoModel>, position: Int) {
        holder.onBindViewHolder(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<TaskInfoModel> {
        return InfoTableFiltersHeaderHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.holder_task_details_filter_list_header,
                parent,
                false
            )
        )
    }
}