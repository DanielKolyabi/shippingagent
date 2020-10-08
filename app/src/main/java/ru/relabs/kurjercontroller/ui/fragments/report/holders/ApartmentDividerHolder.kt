package ru.relabs.kurjercontroller.ui.fragments.report.holders

import android.view.View
import ru.relabs.kurjercontroller.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.ui.fragments.report.models.ApartmentListModel


/**
 * Created by ProOrange on 11.08.2018.
 */
class ApartmentDividerHolder(
    itemView: View
) : BaseViewHolder<ApartmentListModel>(itemView) {

    override fun onBindViewHolder(item: ApartmentListModel) {
        if (item !is ApartmentListModel.Divider) return
    }
}