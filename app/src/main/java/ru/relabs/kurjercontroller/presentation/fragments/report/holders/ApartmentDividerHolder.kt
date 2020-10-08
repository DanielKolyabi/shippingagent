package ru.relabs.kurjercontroller.presentation.fragments.report.holders

import android.view.View
import ru.relabs.kurjercontroller.presentation.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.presentation.fragments.report.models.ApartmentListModel


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