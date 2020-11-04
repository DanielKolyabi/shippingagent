package ru.relabs.kurjercontroller.presentation.reportPager

import kotlinx.android.synthetic.main.holder_report_task.view.*
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.presentation.base.recycler.IAdapterDelegate
import ru.relabs.kurjercontroller.presentation.base.recycler.delegateDefine
import ru.relabs.kurjercontroller.presentation.base.recycler.holderDefine

object ReportPagerTaskAdapter {
    fun taskButtonAdapter(
        onTaskClicked: (TaskItem) -> Unit
    ): IAdapterDelegate<ReportPagerTaskItem> = delegateDefine(
        { it is ReportPagerTaskItem.TaskButton },
        { p ->
            holderDefine(p, R.layout.holder_report_task, { it as ReportPagerTaskItem.TaskButton }) { (task, active) ->
                itemView.button.text = task.publisherName
                if (active) {
                    itemView.button.setBackgroundResource(R.drawable.abc_btn_colored_material)
                } else {
                    itemView.button.setBackgroundResource(R.drawable.abc_btn_default_mtrl_shape)
                }

                itemView.button.setOnClickListener {
                    onTaskClicked(task)
                }
            }
        }
    )
}