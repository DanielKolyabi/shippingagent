package ru.relabs.kurjercontroller.presentation.fragmentsOld.taskInfo.delegates

import android.view.LayoutInflater
import android.view.ViewGroup
import ru.relabs.kurjercontroller.presentation.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.presentation.delegateAdapter.IAdapterDelegate
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.presentation.fragmentsOld.taskInfo.TaskInfoModel
import ru.relabs.kurjercontroller.presentation.fragmentsOld.taskInfo.holders.InfoTableAddressItemHolder

/**
 * Created by ProOrange on 29.08.2018.
 */
class InfoAddressItemDelegate(val onInfoClicked: (item: TaskItem) -> Unit) : IAdapterDelegate<TaskInfoModel> {
    override fun isForViewType(data: List<TaskInfoModel>, position: Int): Boolean {
        return data[position] is TaskInfoModel.TaskItem
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<TaskInfoModel>,
        data: List<TaskInfoModel>,
        position: Int
    ) {
        holder.onBindViewHolder(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<TaskInfoModel> {
        return InfoTableAddressItemHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.holder_task_details_address_list_item, parent, false),
            onInfoClicked
        )
    }
}