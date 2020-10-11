package ru.relabs.kurjercontroller.presentation.fragmentsOld.taskList.holders

import android.view.View
import kotlinx.android.synthetic.main.holder_loading.view.*
import ru.relabs.kurjercontroller.presentation.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.utils.extensions.setVisible
import ru.relabs.kurjercontroller.presentation.fragmentsOld.taskList.TaskListModel

/**
 * Created by ProOrange on 20.03.2019.
 */
class LoaderHolder(itemView: View) : BaseViewHolder<TaskListModel>(itemView) {
    override fun onBindViewHolder(item: TaskListModel) {
        if(item !is TaskListModel.Loader) return
        itemView.loading_text.setVisible(item.text.isNotBlank())
        if(item.text.isNotBlank()) {
            itemView.loading_text.text = item.text
        }
    }
}
