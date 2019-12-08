package ru.relabs.kurjercontroller.ui.fragments.report.holders

import android.graphics.Color
import android.view.View
import androidx.core.graphics.ColorUtils
import kotlinx.android.synthetic.main.holder_report_appartament.view.*
import kotlinx.android.synthetic.main.holder_report_appartament_button_group_addition.view.*
import kotlinx.android.synthetic.main.holder_report_appartament_button_group_main.view.*
import ru.relabs.kurjer.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.ui.extensions.setSelectButtonActive
import ru.relabs.kurjercontroller.ui.fragments.report.models.ApartmentListModel


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