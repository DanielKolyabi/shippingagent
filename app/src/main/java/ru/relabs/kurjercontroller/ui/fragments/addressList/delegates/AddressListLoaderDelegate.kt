package ru.relabs.kurjercontroller.ui.fragments.addressList.delegates

import android.view.LayoutInflater
import android.view.ViewGroup
import ru.relabs.kurjercontroller.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.ui.delegateAdapter.IAdapterDelegate
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.ui.fragments.addressList.AddressListModel
import ru.relabs.kurjercontroller.ui.fragments.addressList.holders.AddressListLoaderHolder

class AddressListLoaderDelegate : IAdapterDelegate<AddressListModel> {

    override fun isForViewType(data: List<AddressListModel>, position: Int): Boolean {
        return data[position] is AddressListModel.Loader
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<AddressListModel>,
        data: List<AddressListModel>,
        position: Int
    ) {
        holder.onBindViewHolder(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<AddressListModel> {
        return AddressListLoaderHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.holder_loading,
                parent,
                false
            )
        )
    }
}
