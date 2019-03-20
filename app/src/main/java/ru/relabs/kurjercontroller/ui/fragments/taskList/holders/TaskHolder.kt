package ru.relabs.kurjercontroller.ui.fragments.taskList.holders

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.View
import kotlinx.android.synthetic.main.holder_tasklist_task.view.*
import ru.relabs.kurjer.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.ui.extensions.setVisible
import ru.relabs.kurjercontroller.ui.fragments.taskList.TaskListModel

/**
 * Created by ProOrange on 18.03.2019.
 */
class TaskHolder(
    val view: View,
    val onSelectedClicked: (position: Int) -> Unit,
    val onTaskClicked: (position: Int) -> Unit
) : BaseViewHolder<TaskListModel>(view) {
    override fun onBindViewHolder(item: TaskListModel) {
        if (item !is TaskListModel.TaskItem) return

        view.title.text = "${item.task.publisher} №${item.task.edition}"

        view.selected_icon.setOnClickListener {
            onSelectedClicked(this.adapterPosition)
        }

        view.container.setOnClickListener {
            onTaskClicked(this.adapterPosition)
        }

        if (item.hasAddressIntersection) {
            view.setBackgroundColor(Color.GRAY)
        } else {
            view.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    fun setIsSelected(selected: Boolean, byOtherUser: Boolean) {
        view.active_icon.setVisible(selected)
        if (byOtherUser) {
            view.active_icon.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN)
        } else {
            view.active_icon.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
        }
    }

    fun setIsActive(active: Boolean) {
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
