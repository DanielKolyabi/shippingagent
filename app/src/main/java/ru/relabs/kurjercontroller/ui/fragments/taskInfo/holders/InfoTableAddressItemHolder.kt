package ru.relabs.kurjercontroller.ui.fragments.taskInfo.holders

import android.graphics.Color
import android.view.View
import kotlinx.android.synthetic.main.holder_task_details_address_list_item.view.*
import ru.relabs.kurjer.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.models.TaskItemModel
import ru.relabs.kurjercontroller.ui.fragments.taskInfo.TaskInfoModel

/**
 * Created by ProOrange on 29.08.2018.
 */
class InfoTableAddressItemHolder(itemView: View, val onInfoClicked: (item: TaskItemModel) -> Unit) :
    BaseViewHolder<TaskInfoModel>(itemView) {
    override fun onBindViewHolder(item: TaskInfoModel) {
        if (item !is TaskInfoModel.TaskItem) return
        val taskItem = item.taskItem
        with(itemView) {
            address_text.text = taskItem.address.name

            info_icon.setOnClickListener {
                onInfoClicked(taskItem)
            }
        }
    }
}