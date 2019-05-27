package ru.relabs.kurjercontroller.ui.fragments.addressList.holders

import android.graphics.Color
import android.view.View
import kotlinx.android.synthetic.main.holder_addr_list_task.view.*
import ru.relabs.kurjer.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.ui.fragments.addressList.AddressListModel

/**
 * Created by ProOrange on 11.08.2018.
 */
class AddressListTaskItemHolder(
    itemView: View,
    val onItemClicked: (item: AddressListModel.TaskItem) -> Unit
) : BaseViewHolder<AddressListModel>(itemView) {

    override fun onBindViewHolder(item: AddressListModel) {
        if (item !is AddressListModel.TaskItem) return
        itemView.task_button.text =
            (if (item.parentTask.filtered) "По фильтрам. " else "") + item.taskItem.publisherName

        if (item.taskItem.isClosed) {
            itemView.task_button.setTextColor(Color.parseColor("#66000000"))
        } else {
            itemView.task_button.setTextColor(Color.parseColor("#ff000000"))
        }

        itemView.task_button.setOnClickListener {
            onItemClicked(item)
        }


        if (item.taskItem.required) {
            itemView.task_button.setBackgroundColor(Color.argb(80, 255, 219, 139))
        } else {
            itemView.task_button.setBackgroundColor(itemView.resources.getColor(R.color.button_material_light))
        }
    }
}