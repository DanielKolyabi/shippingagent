package ru.relabs.kurjercontroller.presentation.fragmentsOld.addressList.delegates

import android.view.LayoutInflater
import android.view.ViewGroup
import ru.relabs.kurjercontroller.presentation.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.presentation.delegateAdapter.IAdapterDelegate
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.presentation.fragmentsOld.addressList.AddressListModel
import ru.relabs.kurjercontroller.presentation.fragmentsOld.addressList.holders.AddressListTaskItemHolder

/**
 * Created by ProOrange on 11.08.2018.
 */
class AddressListTaskItemDelegate(
    private val onItemClicked: (task: AddressListModel.TaskItem) -> Unit
) : IAdapterDelegate<AddressListModel> {
    override fun isForViewType(data: List<AddressListModel>, position: Int): Boolean {
        return data[position] is AddressListModel.TaskItem
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<AddressListModel>,
        data: List<AddressListModel>,
        position: Int
    ) {
        holder.onBindViewHolder(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<AddressListModel> {
        return AddressListTaskItemHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.holder_addr_list_task, parent, false),
            onItemClicked
        )
    }
}