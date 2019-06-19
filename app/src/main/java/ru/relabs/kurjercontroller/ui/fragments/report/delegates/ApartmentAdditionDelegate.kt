package ru.relabs.kurjercontroller.ui.fragments.report.delegates

import android.view.LayoutInflater
import android.view.ViewGroup
import ru.relabs.kurjer.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjer.ui.delegateAdapter.IAdapterDelegate
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.ui.fragments.report.holders.ApartmentAdditionHolder
import ru.relabs.kurjercontroller.ui.fragments.report.models.ApartmentListModel
import ru.relabs.kurjercontroller.ui.fragments.report.holders.ApartmentHolder
import ru.relabs.kurjercontroller.ui.fragments.report.holders.ApartmentMainHolder

/**
 * Created by ProOrange on 11.08.2018.
 */
class ApartmentAdditionDelegate(
    private val onStateChanged: (apartmentNumber: Int, state: Int) -> Unit,
    private val onLongStateChanged: (apartmentNumber: Int, change: Int) -> Unit,
    val onDescriptionClicked: (apartmentNumber: Int) -> Unit
) : IAdapterDelegate<ApartmentListModel> {
    override fun isForViewType(data: List<ApartmentListModel>, position: Int): Boolean {
        return data[position] is ApartmentListModel.Apartment && (data[position] as ApartmentListModel.Apartment).buttonGroup == 1
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<ApartmentListModel>,
        data: List<ApartmentListModel>,
        position: Int
    ) {
        holder.onBindViewHolder(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<ApartmentListModel> {
        return ApartmentAdditionHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.holder_report_appartament_addition, parent, false),
            onStateChanged,
            onLongStateChanged,
            onDescriptionClicked
        )
    }
}