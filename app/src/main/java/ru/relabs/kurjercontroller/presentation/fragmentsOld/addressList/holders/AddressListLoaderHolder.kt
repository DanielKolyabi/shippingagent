package ru.relabs.kurjercontroller.presentation.fragmentsOld.addressList.holders

import android.view.View
import ru.relabs.kurjercontroller.presentation.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.presentation.fragmentsOld.addressList.AddressListModel

class AddressListLoaderHolder(itemView: View) : BaseViewHolder<AddressListModel>(itemView) {
    override fun onBindViewHolder(item: AddressListModel) {
        if (item !is AddressListModel.Loader) return
    }
}
