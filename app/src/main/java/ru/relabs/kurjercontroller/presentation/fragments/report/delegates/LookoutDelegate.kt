package ru.relabs.kurjercontroller.presentation.fragments.report.delegates

import android.view.LayoutInflater
import android.view.ViewGroup
import ru.relabs.kurjercontroller.presentation.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.presentation.delegateAdapter.IAdapterDelegate
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.presentation.fragments.report.holders.LookoutHolder
import ru.relabs.kurjercontroller.presentation.fragments.report.models.ApartmentListModel

/**
 * Created by ProOrange on 11.08.2018.
 */
class LookoutDelegate(
    private val onStateChanged: (state: Int) -> Unit
) : IAdapterDelegate<ApartmentListModel> {
    override fun isForViewType(data: List<ApartmentListModel>, position: Int): Boolean {
        return data[position] is ApartmentListModel.Lookout
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<ApartmentListModel>,
        data: List<ApartmentListModel>,
        position: Int
    ) {
        holder.onBindViewHolder(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<ApartmentListModel> {
        return LookoutHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.holder_report_appartament, parent, false),
            onStateChanged
        )
    }
}