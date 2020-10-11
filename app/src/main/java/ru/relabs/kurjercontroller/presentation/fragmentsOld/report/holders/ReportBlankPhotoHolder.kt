package ru.relabs.kurjercontroller.presentation.fragmentsOld.report.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.relabs.kurjercontroller.presentation.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.presentation.fragmentsOld.report.models.ReportPhotosListModel

/**
 * Created by ProOrange on 30.08.2018.
 */
class ReportBlankPhotoHolder(itemView: View, private val onPhotoClicked: (holder: RecyclerView.ViewHolder) -> Unit) :
    BaseViewHolder<ReportPhotosListModel>(itemView) {
    override fun onBindViewHolder(item: ReportPhotosListModel) {
        if (item !is ReportPhotosListModel.BlankPhoto) return
        itemView.setOnClickListener {
            onPhotoClicked(this)
        }
    }
}
