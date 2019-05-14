package ru.relabs.kurjercontroller.ui.fragments.taskInfo.delegates

import android.view.LayoutInflater
import android.view.ViewGroup
import ru.relabs.kurjer.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjer.ui.delegateAdapter.IAdapterDelegate
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.ui.fragments.taskInfo.TaskInfoModel
import ru.relabs.kurjercontroller.ui.fragments.taskInfo.holders.InfoTableAddressesHeaderHolder
import ru.relabs.kurjercontroller.ui.fragments.taskInfo.holders.InfoTableFiltersHeaderHolder

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