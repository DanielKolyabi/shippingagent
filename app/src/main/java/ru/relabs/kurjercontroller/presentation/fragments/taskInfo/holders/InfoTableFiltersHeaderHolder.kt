package ru.relabs.kurjercontroller.presentation.fragments.taskInfo.holders

import android.view.View
import ru.relabs.kurjercontroller.presentation.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.presentation.fragments.taskInfo.TaskInfoModel

/**
 * Created by ProOrange on 29.08.2018.
 */
class InfoTableFiltersHeaderHolder(itemView: View) : BaseViewHolder<TaskInfoModel>(itemView) {
    override fun onBindViewHolder(item: TaskInfoModel) {
        if (item !is TaskInfoModel.DetailsFiltersTableHeader) return
    }
}