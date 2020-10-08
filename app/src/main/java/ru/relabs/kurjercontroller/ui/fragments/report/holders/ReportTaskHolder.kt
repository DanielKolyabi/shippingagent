package ru.relabs.kurjercontroller.ui.fragments.report.holders

import android.view.View
import kotlinx.android.synthetic.main.holder_report_task.view.*
import ru.relabs.kurjercontroller.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.ui.fragments.report.models.ReportTasksListModel

class ReportTaskHolder(itemView: View, val onTaskClicked: (pos: Int) -> Unit) :
    BaseViewHolder<ReportTasksListModel>(itemView) {
    override fun onBindViewHolder(item: ReportTasksListModel) {
        if (item !is ReportTasksListModel.TaskButton) return
        itemView.button.text = item.taskItem.publisherName
        if (item.active) {
            itemView.button.setBackgroundResource(R.drawable.abc_btn_colored_material)
        } else {
            itemView.button.setBackgroundResource(R.drawable.abc_btn_default_mtrl_shape)
        }

        itemView.button.setOnClickListener {
            onTaskClicked(item.pos)
        }
    }
}
