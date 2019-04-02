package ru.relabs.kurjercontroller.ui.fragments.taskInfo.holders

import android.view.View
import kotlinx.android.synthetic.main.holder_task_details_filter_list_item.view.*
import ru.relabs.kurjer.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.ui.fragments.taskInfo.TaskInfoModel

/**
 * Created by ProOrange on 29.08.2018.
 */
class InfoTableFilterItemHolder(itemView: View) :
    BaseViewHolder<TaskInfoModel>(itemView) {
    override fun onBindViewHolder(item: TaskInfoModel) {
        if (item !is TaskInfoModel.FilterItem) return
        val taskItem = item.filterItem
        with(itemView) {
            col1_header.text = item.filterName
            col2_header.text = taskItem.name
        }
    }
}