package ru.relabs.kurjercontroller.ui.fragments.addressList.holders

import android.graphics.Color
import android.view.View
import kotlinx.android.synthetic.main.holder_addr_list_address.view.*
import ru.relabs.kurjer.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.models.AddressModel
import ru.relabs.kurjercontroller.models.TaskItemModel
import ru.relabs.kurjercontroller.ui.extensions.performFlash
import ru.relabs.kurjercontroller.ui.fragments.addressList.AddressListModel


/**
 * Created by ProOrange on 11.08.2018.
 */
class AddressListAddressHolder(
    private val onMapClick: (items: List<TaskItemModel>) -> Unit,
    itemView: View
) : BaseViewHolder<AddressListModel>(itemView) {
    override fun onBindViewHolder(item: AddressListModel) {
        if (item !is AddressListModel.Address) return
        val address = item.taskItems.first().address.name
        itemView.address_text.text = address
        val isAddressClosed = item.taskItems.find {
            !it.isClosed
        } == null

        if (isAddressClosed) {
            itemView.address_text.setTextColor(Color.parseColor("#CCCCCC"))
            itemView.map_icon.alpha = 0.4f
            itemView.map_icon.isClickable = false
        } else {
            itemView.address_text.setTextColor(itemView.resources.getColor(R.color.black))
            itemView.map_icon.alpha = 1f
            itemView.map_icon.isClickable = true
        }

        itemView.map_icon.setOnClickListener {
            onMapClick(item.taskItems)
        }
    }

    fun flashSelectedColor() {
        itemView.performFlash()
    }
}