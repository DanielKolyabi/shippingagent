package ru.relabs.kurjercontroller.ui.fragments.taskInfo.holders

import android.view.View
import kotlinx.android.synthetic.main.holder_task_details_list_info.view.*
import ru.relabs.kurjer.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.ui.fragments.taskInfo.TaskInfoModel

/**
 * Created by ProOrange on 29.08.2018.
 */
class InfoTableInfoHolder(itemView: View) : BaseViewHolder<TaskInfoModel>(itemView) {
    override fun onBindViewHolder(item: TaskInfoModel) {
        if (item !is TaskInfoModel.Task) return
        val task = item.task
        with(itemView) {
            publisher_text.text = "${task.publisher} №${task.edition}"
            control_dates_text.text = "${task.startControlDate.toString("dd.MM.yyyy")} - ${task.endControlDate.toString("dd.MM.yyyy")}"
            distribution_dates_text.text = "${task.startDistributionDate.toString("dd.MM.yyyy")} - ${task.endDistributionDate.toString("dd.MM.yyyy")}"
            storage_text.text = task.storage
            description_text.text = task.description
        }
    }
}