package ru.relabs.kurjercontroller.ui.fragments.entrancesList.holder

import android.view.View
import kotlinx.android.synthetic.main.holder_entrance.view.*
import ru.relabs.kurjer.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.ui.fragments.entrancesList.EntrancesListModel

/**
 * Created by ProOrange on 18.03.2019.
 */
class EntranceHolder(itemView: View, val onClicked: (num: Int) -> Unit) : BaseViewHolder<EntrancesListModel>(itemView) {
    override fun onBindViewHolder(item: EntrancesListModel) {
        if (item !is EntrancesListModel.Entrance) return

        itemView.entrance_button.setOnClickListener {
            onClicked(item.number)
        }
    }
}
