package ru.relabs.kurjercontroller.ui.fragments.report.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.relabs.kurjercontroller.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.ui.fragments.report.models.ReportPhotosListModel

/**
 * Created by ProOrange on 30.08.2018.
 */
class ReportBlankMultiPhotoHolder(
    itemView: View,
    private val onPhotoClicked: (holder: RecyclerView.ViewHolder) -> Unit
) : BaseViewHolder<ReportPhotosListModel>(itemView) {
    override fun onBindViewHolder(item: ReportPhotosListModel) {
        if (item !is ReportPhotosListModel.BlankMultiPhoto) return
        itemView.setOnClickListener {
            onPhotoClicked(this)
        }
    }
}
