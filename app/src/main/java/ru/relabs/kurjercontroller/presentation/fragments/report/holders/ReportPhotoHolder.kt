package ru.relabs.kurjercontroller.presentation.fragments.report.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.holder_report_photo.view.*
import ru.relabs.kurjercontroller.presentation.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.presentation.fragments.report.models.ReportPhotosListModel

/**
 * Created by ProOrange on 30.08.2018.
 */

class ReportPhotoHolder(itemView: View, private val onRemoveClicked: (holder: RecyclerView.ViewHolder) -> Unit) :
    BaseViewHolder<ReportPhotosListModel>(itemView) {
    override fun onBindViewHolder(item: ReportPhotosListModel) {
        if (item !is ReportPhotosListModel.TaskItemPhoto) return
        Glide.with(itemView)
            .load(item.photo.realPath ?: item.photo.URI)
            .into(itemView.photo)
        itemView.remove.setOnClickListener {
            onRemoveClicked(this)
        }
    }
}
