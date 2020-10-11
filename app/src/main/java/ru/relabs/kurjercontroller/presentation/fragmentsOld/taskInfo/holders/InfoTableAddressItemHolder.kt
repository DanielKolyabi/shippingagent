package ru.relabs.kurjercontroller.presentation.fragmentsOld.taskInfo.holders

import android.graphics.Color
import android.view.View
import kotlinx.android.synthetic.main.holder_task_details_address_list_item.view.*
import ru.relabs.kurjercontroller.presentation.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.presentation.fragmentsOld.taskInfo.TaskInfoModel

/**
 * Created by ProOrange on 29.08.2018.
 */
class InfoTableAddressItemHolder(itemView: View, val onInfoClicked: (item: TaskItem) -> Unit) :
    BaseViewHolder<TaskInfoModel>(itemView) {
    override fun onBindViewHolder(item: TaskInfoModel) {
        if (item !is TaskInfoModel.TaskItem) return
        val taskItem = item.taskItem
        with(itemView) {
            address_text.text = taskItem.address.name

            info_icon.setOnClickListener {
                onInfoClicked(taskItem)
            }

            if(taskItem.required){
                setBackgroundColor(Color.argb(80, 255, 219, 139))
            }else{
                setBackgroundColor(Color.TRANSPARENT)
            }
        }
    }
}