package ru.relabs.kurjercontroller.ui.fragments.report.delegates

import android.view.LayoutInflater
import android.view.ViewGroup
import ru.relabs.kurjer.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjer.ui.delegateAdapter.IAdapterDelegate
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.ui.fragments.report.holders.ApartmentDividerHolder
import ru.relabs.kurjercontroller.ui.fragments.report.holders.LookoutHolder
import ru.relabs.kurjercontroller.ui.fragments.report.models.ApartmentListModel

/**
 * Created by ProOrange on 11.08.2018.
 */
class ApartmentDividerDelegate : IAdapterDelegate<ApartmentListModel> {
    override fun isForViewType(data: List<ApartmentListModel>, position: Int): Boolean {
        return data[position] is ApartmentListModel.Divider
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<ApartmentListModel>,
        data: List<ApartmentListModel>,
        position: Int
    ) {
        holder.onBindViewHolder(data[position])
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<ApartmentListModel> {
        return ApartmentDividerHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.holder_divider, parent, false)
        )
    }
}