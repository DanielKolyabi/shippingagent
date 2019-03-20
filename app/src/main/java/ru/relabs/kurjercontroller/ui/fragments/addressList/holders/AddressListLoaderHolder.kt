package ru.relabs.kurjercontroller.ui.fragments.addressList.holders

import android.view.View
import ru.relabs.kurjer.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.ui.fragments.addressList.AddressListModel

class AddressListLoaderHolder(itemView: View) : BaseViewHolder<AddressListModel>(itemView) {
    override fun onBindViewHolder(item: AddressListModel) {
        if (item !is AddressListModel.Loader) return
    }
}
