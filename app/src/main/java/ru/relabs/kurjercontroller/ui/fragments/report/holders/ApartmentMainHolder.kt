package ru.relabs.kurjercontroller.ui.fragments.report.holders

import android.view.View
import kotlinx.android.synthetic.main.holder_report_appartament.view.*
import kotlinx.android.synthetic.main.holder_report_appartament_button_group_addition.view.*
import kotlinx.android.synthetic.main.holder_report_appartament_button_group_main.view.*
import ru.relabs.kurjer.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.ui.extensions.setSelectButtonActive
import ru.relabs.kurjercontroller.ui.fragments.report.models.ApartmentListModel


/**
 * Created by ProOrange on 11.08.2018.
 */
class ApartmentMainHolder(
    itemView: View,
    val onStateChanged: (apartmentNumber: Int, state: Int) -> Unit,
    val onLongStateChanged: (apartmentNumber: Int, change: Int) -> Unit,
    val onDescriptionClicked: (apartmentNumber: Int) -> Unit
) : BaseViewHolder<ApartmentListModel>(itemView) {
    var item: ApartmentListModel? = null

    override fun onBindViewHolder(item: ApartmentListModel) {
        this.item = item
        if (item !is ApartmentListModel.Apartment) return

        with(itemView) {
            description_button?.setOnClickListener {
                onDescriptionClicked(item.number)
            }
            appartament_number?.text = item.number.toString()

            yes_button_main?.setOnLongClickListener {
                onLongStateChanged(item.number, 1)
                true
            }
            yes_button_main?.setOnClickListener {
                item.state = item.state xor 1
                if (item.state and 4 > 0) {
                    item.state = item.state xor 4
                }
                onStateChanged(item.number, item.state)
            }

            not_regular_button_main?.setOnLongClickListener {
                onLongStateChanged(item.number, 2)
                true
            }
            not_regular_button_main?.setOnClickListener {
                item.state = item.state xor 2
                onStateChanged(item.number, item.state)
            }

            no_button_main?.setOnLongClickListener {
                onLongStateChanged(item.number, 4)
                true
            }
            no_button_main?.setOnClickListener {
                item.state = item.state xor 4
                if (item.state and 1 > 0) {
                    item.state = item.state xor 1
                }
                onStateChanged(item.number, item.state)
            }

            broken_button_main?.setOnLongClickListener {
                onLongStateChanged(item.number, 8)
                true
            }
            broken_button_main?.setOnClickListener {
                item.state = item.state xor 8
                onStateChanged(item.number, item.state)
            }

            yes_button_main?.setSelectButtonActive(item.state and 1 > 0)
            not_regular_button_main?.setSelectButtonActive(item.state and 2 > 0)
            no_button_main?.setSelectButtonActive(item.state and 4 > 0)
            broken_button_main?.setSelectButtonActive(item.state and 8 > 0)
        }
    }
}