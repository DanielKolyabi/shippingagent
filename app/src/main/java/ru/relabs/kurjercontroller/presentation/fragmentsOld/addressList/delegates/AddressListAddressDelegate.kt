package ru.relabs.kurjercontroller.presentation.fragmentsOld.addressList.delegates

import android.view.LayoutInflater
import android.view.ViewGroup
import ru.relabs.kurjercontroller.presentation.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.presentation.delegateAdapter.IAdapterDelegate
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.presentation.fragmentsOld.addressList.AddressListModel
import ru.relabs.kurjercontroller.presentation.fragmentsOld.addressList.holders.AddressListAddressHolder

/**
 * Created by ProOrange on 11.08.2018.
 */
class AddressListAddressDelegate(
    private val onMapClick: (items: List<TaskItem>) -> Unit
) : IAdapterDelegate<AddressListModel> {
    override fun isForViewType(data: List<AddressListModel>, position: Int): Boolean {
        return data[position] is AddressListModel.Address
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<AddressListModel>,
        data: List<AddressListModel>,
        position: Int
    ) {
        holder.onBindViewHolder(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<AddressListModel> {
        return AddressListAddressHolder(
            onMapClick,
            LayoutInflater.from(parent.context).inflate(R.layout.holder_addr_list_address, parent, false)
        )
    }
}