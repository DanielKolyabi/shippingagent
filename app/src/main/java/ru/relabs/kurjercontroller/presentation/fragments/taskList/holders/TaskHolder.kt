package ru.relabs.kurjercontroller.presentation.fragments.taskList.holders

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.View
import kotlinx.android.synthetic.main.holder_tasklist_task.view.*
import ru.relabs.kurjercontroller.presentation.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.domain.models.TaskModel
import ru.relabs.kurjercontroller.utils.extensions.performFlash
import ru.relabs.kurjercontroller.utils.extensions.setVisible
import ru.relabs.kurjercontroller.presentation.fragments.taskList.TaskListModel

/**
 * Created by ProOrange on 18.03.2019.
 */
class TaskHolder(
    val view: View,
    val onSelectedClicked: (position: Int) -> Unit,
    val onTaskClicked: (position: Int) -> Unit
) : BaseViewHolder<TaskListModel>(view) {
    var item: TaskListModel.TaskItem? = null

    override fun onBindViewHolder(item: TaskListModel) {
        if (item !is TaskListModel.TaskItem) return

        this.item = item
        view.title.text = item.task.name

        view.selected_icon.setOnClickListener {
            onSelectedClicked(this.adapterPosition)
        }

        view.container.setOnClickListener {
            onTaskClicked(this.adapterPosition)
        }

        view.active_icon.setVisible(item.task.androidState >= TaskModel.EXAMINED)
        if (item.task.firstExaminedDeviceId?.isNotBlank() == true && item.task.firstExaminedDeviceId != application().deviceUUID) {
            view.active_icon.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN)
        } else {
            view.active_icon.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
        }

        setIsSelected(item.selected)

        if (item.hasAddressIntersection) {
            view.setBackgroundColor(Color.GRAY)
        } else {
            view.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    fun setSelected() {
        item?.selected = true
        setIsSelected(true)
        itemView.performFlash()
    }

    fun setIsSelected(active: Boolean) {
        view.selected_icon.setImageDrawable(
            view.resources.getDrawable(
                if (active)
                    R.drawable.ic_chain_enabled
                else
                    R.drawable.ic_chain_disabled
            )
        )
    }
}
