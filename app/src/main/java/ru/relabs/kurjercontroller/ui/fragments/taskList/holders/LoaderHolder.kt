package ru.relabs.kurjercontroller.ui.fragments.taskList.holders

import android.view.View
import ru.relabs.kurjer.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.ui.fragments.taskList.TaskListModel

/**
 * Created by ProOrange on 20.03.2019.
 */
class LoaderHolder(itemView: View) : BaseViewHolder<TaskListModel>(itemView) {
    override fun onBindViewHolder(item: TaskListModel) {
        if(item !is TaskListModel.Loader) return
    }
}
