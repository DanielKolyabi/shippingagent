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
            publisher_text.text = task.publishers.joinToString { it.name + "; " }
            control_dates_text.text =
                "${task.startControlDate.toString("dd.MM.yyyy")} - ${task.endControlDate.toString("dd.MM.yyyy")}"

            distribution_dates_text.text = task.publishers.map {
                it.name + ": " + it.startDistributionDate.toString("dd.MM.yyyy") + " - " + it.endDistributionDate.toString(
                    "dd.MM.yyyy"
                )
            }.joinToString("\n")

            storage_text.text = task.storages.joinToString { "$it; " }
            description_text.text = task.description
        }
    }
}