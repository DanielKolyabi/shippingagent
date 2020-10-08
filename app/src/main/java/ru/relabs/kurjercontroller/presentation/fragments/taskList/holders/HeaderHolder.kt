package ru.relabs.kurjercontroller.presentation.fragments.taskList.holders

import android.view.View
import kotlinx.android.synthetic.main.holder_tasklist_task.view.*
import ru.relabs.kurjercontroller.presentation.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.presentation.fragments.taskList.TaskListModel

/**
 * Created by ProOrange on 18.03.2019.
 */
class HeaderHolder(
    val view: View
) : BaseViewHolder<TaskListModel>(view) {
    override fun onBindViewHolder(item: TaskListModel) {
        if (item !is TaskListModel.GroupHeader) return

        view.title.text = item.title
    }
}
