package ru.relabs.kurjercontroller.ui.fragments.report.delegates

import android.view.LayoutInflater
import android.view.ViewGroup
import ru.relabs.kurjer.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjer.ui.delegateAdapter.IAdapterDelegate
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.ui.fragments.report.holders.ReportTaskHolder
import ru.relabs.kurjercontroller.ui.fragments.report.models.ReportTasksListModel

/**
 * Created by ProOrange on 30.08.2018.
 */
class ReportTasksDelegate(val onTaskClicked: (pos: Int) -> Unit) : IAdapterDelegate<ReportTasksListModel> {
    override fun isForViewType(data: List<ReportTasksListModel>, position: Int): Boolean {
        return data[position] is ReportTasksListModel.TaskButton
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<ReportTasksListModel>,
        data: List<ReportTasksListModel>,
        position: Int
    ) {
        holder.onBindViewHolder(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<ReportTasksListModel> {
        return ReportTaskHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.holder_report_task, parent, false),
            onTaskClicked
        )
    }
}