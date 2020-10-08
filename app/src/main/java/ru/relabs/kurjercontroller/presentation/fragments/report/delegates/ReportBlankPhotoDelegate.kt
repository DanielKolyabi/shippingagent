package ru.relabs.kurjercontroller.presentation.fragments.report.delegates

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.relabs.kurjercontroller.presentation.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.presentation.delegateAdapter.IAdapterDelegate
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.presentation.fragments.report.models.ReportPhotosListModel
import ru.relabs.kurjercontroller.presentation.fragments.report.holders.ReportBlankPhotoHolder

/**
 * Created by ProOrange on 30.08.2018.
 */

class ReportBlankPhotoDelegate(private val onPhotoClicked: (holder: RecyclerView.ViewHolder) -> Unit) :
    IAdapterDelegate<ReportPhotosListModel> {
    override fun isForViewType(data: List<ReportPhotosListModel>, position: Int): Boolean {
        return data[position] is ReportPhotosListModel.BlankPhoto
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<ReportPhotosListModel>,
        data: List<ReportPhotosListModel>,
        position: Int
    ) {
        holder.onBindViewHolder(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<ReportPhotosListModel> {
        return ReportBlankPhotoHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.holder_report_photo_blank, parent, false),
            onPhotoClicked
        )
    }
}

