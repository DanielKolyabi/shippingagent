package ru.relabs.kurjercontroller.presentation.taskDetails

import android.graphics.Color
import kotlinx.android.synthetic.main.holder_task_details_address_list_item.view.*
import kotlinx.android.synthetic.main.holder_task_details_filter_list_item.view.*
import kotlinx.android.synthetic.main.holder_task_details_list_info.view.*
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.presentation.base.recycler.IAdapterDelegate
import ru.relabs.kurjercontroller.presentation.base.recycler.delegateDefine
import ru.relabs.kurjercontroller.presentation.base.recycler.holderDefine
import ru.relabs.kurjercontroller.utils.extensions.formatted
import ru.relabs.kurjercontroller.utils.extensions.visible

object TaskDetailsAdapter {
    fun pageHeaderAdapter(): IAdapterDelegate<TaskDetailsItem> = delegateDefine(
        { it is TaskDetailsItem.PageHeader },
        { p ->
            holderDefine(p, R.layout.holder_task_details_list_info, { it as TaskDetailsItem.PageHeader }) { (task) ->
                with(itemView) {
                    tv_publisher.visible = task.publishers.isNotEmpty()
                    tv_publisher_label.visible = task.publishers.isNotEmpty()
                    tv_distribution_dates.visible = task.publishers.isNotEmpty()
                    tv_distribution_dates_label.visible = task.publishers.isNotEmpty()
                    tv_storage.visible = !task.filtered
                    tv_storage_label.visible = !task.filtered

                    tv_control_dates.text = "${task.startControlDate.formatted()} - ${task.endControlDate.formatted()}"
                    tv_distribution_dates.text = task.publishers.map {
                        it.name + ": " + it.startDistributionDate.formatted() + " - " + it.endDistributionDate.formatted()
                    }.joinToString("\n")
                    tv_description.text = task.description

                    if (task.filtered) {
                        if (task.publishers.isNotEmpty()) {
                            tv_publisher.text = task.publishers.joinToString { it.name + "; " }
                        }
                    } else {
                        tv_publisher.text = task.publishers.joinToString { it.name + "; " }
                        tv_storage.text = task.storages.joinToString { "${it.address}; " }
                    }
                }
            }
        }
    )

    fun listAddressHeaderAdapter(): IAdapterDelegate<TaskDetailsItem> = delegateDefine(
        { it is TaskDetailsItem.ListAddressesHeader },
        { p ->
            holderDefine(p, R.layout.holder_task_details_address_list_header) { it as TaskDetailsItem.ListAddressesHeader }
        }
    )

    fun listFiltersHeaderAdapter(): IAdapterDelegate<TaskDetailsItem> = delegateDefine(
        { it is TaskDetailsItem.ListFiltersHeader },
        { p ->
            holderDefine(p, R.layout.holder_task_details_filter_list_header) { it as TaskDetailsItem.ListFiltersHeader }
        }
    )

    fun listAddressItemAdapter(onInfoClicked: (TaskItem) -> Unit): IAdapterDelegate<TaskDetailsItem> = delegateDefine(
        { it is TaskDetailsItem.AddressItem },
        { p ->
            holderDefine(p, R.layout.holder_task_details_address_list_item, { it as TaskDetailsItem.AddressItem }) { (taskItem) ->
                with(itemView) {
                    tv_address.text = taskItem.address.name

                    ic_info.setOnClickListener {
                        onInfoClicked(taskItem)
                    }

                    if (taskItem.required) {
                        setBackgroundColor(Color.argb(80, 255, 219, 139))
                    } else {
                        setBackgroundColor(Color.TRANSPARENT)
                    }
                }
            }
        }
    )

    fun listFilterItemAdapter(): IAdapterDelegate<TaskDetailsItem> = delegateDefine(
        { it is TaskDetailsItem.FilterItem },
        { p ->
            holderDefine(p, R.layout.holder_task_details_filter_list_item, { it as TaskDetailsItem.FilterItem }) { (filterName, filterItem) ->
                with(itemView) {
                    tv_filter.text = filterName
                    tv_filter_value.text = filterItem.name
                }
            }
        }
    )
}