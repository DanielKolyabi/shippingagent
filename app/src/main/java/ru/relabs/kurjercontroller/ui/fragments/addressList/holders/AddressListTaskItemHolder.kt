package ru.relabs.kurjercontroller.ui.fragments.addressList.holders

import android.graphics.Color
import android.view.View
import androidx.core.graphics.ColorUtils
import kotlinx.android.synthetic.main.holder_addr_list_task.view.*
import ru.relabs.kurjercontroller.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.utils.extensions.performFlash
import ru.relabs.kurjercontroller.utils.extensions.setVisible
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

        itemView.setBackgroundColor(ColorUtils.setAlphaComponent(item.taskItem.placemarkColor, 60))

        itemView.flare_new.setVisible(item.taskItem.isNew)
        itemView.task_button.text =
            (if (item.parentTask.filtered) "По фильтрам. " else "") +
                    (if (item.taskItem.buttonName.isNotEmpty()) item.taskItem.buttonName else item.taskItem.publisherName)

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

    fun flashSelectedColor() {

        itemView.performFlash()
    }
}