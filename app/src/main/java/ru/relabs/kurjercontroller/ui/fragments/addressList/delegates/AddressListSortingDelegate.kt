package ru.relabs.kurjercontroller.ui.fragments.addressList.delegates

import android.view.LayoutInflater
import android.view.ViewGroup
import ru.relabs.kurjer.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjer.ui.delegateAdapter.IAdapterDelegate
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.ui.fragments.addressList.AddressListModel
import ru.relabs.kurjercontroller.ui.fragments.addressList.holders.AddressListSortingHolder

/**
 * Created by ProOrange on 29.08.2018.
 */
class AddressListSortingDelegate(val onSortingChanged: (sortingMethod: Int) -> Unit) :
    IAdapterDelegate<AddressListModel> {
    override fun isForViewType(data: List<AddressListModel>, position: Int): Boolean {
        return data[position] is AddressListModel.SortingItem
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<AddressListModel>,
        data: List<AddressListModel>,
        position: Int
    ) {
        holder.onBindViewHolder(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<AddressListModel> {
        return AddressListSortingHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.holder_addr_list_sorting, parent, false),
            { onSortingChanged(it) }
        )
    }
}