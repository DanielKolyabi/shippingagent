package ru.relabs.kurjercontroller.ui.fragments.report.holders

import android.view.View
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.holder_report_appartament.view.*
import ru.relabs.kurjer.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.ui.fragments.report.models.ApartmentListModel
import ru.relabs.kurjercontroller.ui.fragments.report.adapters.ApartmentButtonsPagerAdapter


/**
 * Created by ProOrange on 11.08.2018.
 */
class EntranceHolder(
    itemView: View,
    val onStateChanged: (state: Int) -> Unit
) : BaseViewHolder<ApartmentListModel>(itemView) {
    var item: ApartmentListModel? = null

    override fun onBindViewHolder(item: ApartmentListModel) {
        this.item = item
        if (item !is ApartmentListModel.Entrance) return

        itemView.buttons_list?.setOnTouchListener { view, motionEvent ->
            itemView.buttons_list?.scrollX = 0
            return@setOnTouchListener true
        }

        itemView.appartament_number?.text = "Подъезд"
        itemView.buttons_list?.adapter = ApartmentButtonsPagerAdapter(
            itemView.context,
            item.state,
            { newState ->
                onStateChanged(newState)
            },
            {}
        )
        itemView.buttons_list?.currentItem = 1
    }
}