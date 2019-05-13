package ru.relabs.kurjercontroller.ui.fragments.report.holders

import android.view.View
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.holder_report_appartament.view.*
import ru.relabs.kurjer.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.ui.extensions.setVisible
import ru.relabs.kurjercontroller.ui.fragments.report.models.ApartmentListModel
import ru.relabs.kurjercontroller.ui.fragments.report.adapters.ApartmentButtonsPagerAdapter


/**
 * Created by ProOrange on 11.08.2018.
 */
class ApartmentHolder(
    itemView: View,
    val onButtonGroupChanged: (apartmentNumber: Int, buttonGroup: Int) -> Unit,
    val onStateChanged: (apartmentNumber: Int, state: Int) -> Unit,
    val onLongStateChanged: (apartmentNumber: Int, change: Int) -> Unit,
    val onDescriptionClicked: (apartmentNumber: Int) -> Unit
) : BaseViewHolder<ApartmentListModel>(itemView) {
    var item: ApartmentListModel? = null

    init {
        itemView.buttons_list?.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                (item as? ApartmentListModel.Apartment)?.let {
                    onButtonGroupChanged(it.number, position)
                }
            }
        })
    }

    override fun onBindViewHolder(item: ApartmentListModel) {
        this.item = item
        if (item !is ApartmentListModel.Apartment) return

        itemView.description_button?.setOnClickListener {
            onDescriptionClicked(item.number)
        }
        itemView.appartament_number?.text = item.number.toString()
        itemView.buttons_list?.adapter = ApartmentButtonsPagerAdapter(
            itemView.context,
            item.state,
            { newState ->
                onStateChanged(item.number, newState)
            },
            { newState ->
                onLongStateChanged(item.number, newState)
            }
        )
        itemView.buttons_list?.currentItem = item.buttonGroup
    }
}