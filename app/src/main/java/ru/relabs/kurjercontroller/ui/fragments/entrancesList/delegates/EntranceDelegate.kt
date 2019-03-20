package ru.relabs.kurjercontroller.ui.fragments.entrancesList.delegates

import android.view.LayoutInflater
import android.view.ViewGroup
import ru.relabs.kurjer.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjer.ui.delegateAdapter.IAdapterDelegate
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.ui.fragments.addressList.AddressListModel
import ru.relabs.kurjercontroller.ui.fragments.entrancesList.EntrancesListModel
import ru.relabs.kurjercontroller.ui.fragments.entrancesList.holder.EntranceHolder

/**
 * Created by ProOrange on 18.03.2019.
 */
class EntranceDelegate(val onClicked: (num: Int) -> Unit) : IAdapterDelegate<EntrancesListModel> {

    override fun isForViewType(data: List<EntrancesListModel>, position: Int): Boolean {
        return data[position] is EntrancesListModel.Entrance
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<EntrancesListModel>,
        data: List<EntrancesListModel>,
        position: Int
    ) {
        holder.onBindViewHolder(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<EntrancesListModel> {
        return EntranceHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.holder_entrance,
                parent,
                false
            ),
            onClicked
        )
    }
}
