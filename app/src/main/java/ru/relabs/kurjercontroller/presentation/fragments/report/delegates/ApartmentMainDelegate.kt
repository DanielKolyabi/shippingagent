package ru.relabs.kurjercontroller.presentation.fragments.report.delegates

import android.view.LayoutInflater
import android.view.ViewGroup
import ru.relabs.kurjercontroller.presentation.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.presentation.delegateAdapter.IAdapterDelegate
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.presentation.fragments.report.holders.ApartmentMainHolder
import ru.relabs.kurjercontroller.presentation.fragments.report.models.ApartmentListModel

/**
 * Created by ProOrange on 11.08.2018.
 */
class ApartmentMainDelegate(
    private val onStateChanged: (apartmentNumber: Int, state: Int) -> Unit,
    private val onLongStateChanged: (apartmentNumber: Int, change: Int) -> Unit,
    val onDescriptionClicked: (apartmentNumber: Int) -> Unit
) : IAdapterDelegate<ApartmentListModel> {
    override fun isForViewType(data: List<ApartmentListModel>, position: Int): Boolean {
        return data[position] is ApartmentListModel.Apartment && (data[position] as ApartmentListModel.Apartment).buttonGroup == 0
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<ApartmentListModel>,
        data: List<ApartmentListModel>,
        position: Int
    ) {
        holder.onBindViewHolder(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<ApartmentListModel> {
        return ApartmentMainHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.holder_report_appartament_main, parent, false),
            onStateChanged,
            onLongStateChanged,
            onDescriptionClicked
        )
    }
}