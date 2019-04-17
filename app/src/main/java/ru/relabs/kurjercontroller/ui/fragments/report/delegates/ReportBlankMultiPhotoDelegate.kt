package ru.relabs.kurjercontroller.ui.fragments.report.delegates

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.relabs.kurjer.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjer.ui.delegateAdapter.IAdapterDelegate
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.ui.fragments.report.models.ReportPhotosListModel
import ru.relabs.kurjercontroller.ui.fragments.report.holders.ReportBlankMultiPhotoHolder

/**
 * Created by ProOrange on 28.11.2018.
 */

class ReportBlankMultiPhotoDelegate(private val onPhotoClicked: (holder: RecyclerView.ViewHolder) -> Unit) :
    IAdapterDelegate<ReportPhotosListModel> {
    override fun isForViewType(data: List<ReportPhotosListModel>, position: Int): Boolean {
        return data[position] is ReportPhotosListModel.BlankMultiPhoto
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<ReportPhotosListModel>,
        data: List<ReportPhotosListModel>,
        position: Int
    ) {
        holder.onBindViewHolder(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<ReportPhotosListModel> {
        return ReportBlankMultiPhotoHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.holder_report_photo_blank_multi, parent, false),
            onPhotoClicked
        )
    }
}

