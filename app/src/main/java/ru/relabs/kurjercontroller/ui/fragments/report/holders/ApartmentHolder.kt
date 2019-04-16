package ru.relabs.kurjercontroller.ui.fragments.report.holders

import android.view.View
import kotlinx.android.synthetic.main.holder_report_appartament.view.*
import ru.relabs.kurjer.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.ui.fragments.report.ApartmentListModel


/**
 * Created by ProOrange on 11.08.2018.
 */
class ApartmentHolder(
    itemView: View
) : BaseViewHolder<ApartmentListModel>(itemView) {
    override fun onBindViewHolder(item: ApartmentListModel) {
        if (item !is ApartmentListModel.Apartment) return
        itemView.appartament_number.text = item.number.toString()
    }
}