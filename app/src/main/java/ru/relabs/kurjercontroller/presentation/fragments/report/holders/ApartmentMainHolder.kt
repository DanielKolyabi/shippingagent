package ru.relabs.kurjercontroller.presentation.fragments.report.holders

import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import kotlinx.android.synthetic.main.holder_report_appartament.view.*
import kotlinx.android.synthetic.main.holder_report_appartament_button_group_main.view.*
import ru.relabs.kurjercontroller.presentation.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.utils.extensions.setSelectButtonActive
import ru.relabs.kurjercontroller.presentation.fragments.report.models.ApartmentListModel


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
            if(item.colored){
                setBackgroundColor(Color.parseColor("#77ff0000"))
            }else{
                background = null
            }
            if(item.required){
                appartament_number?.setTextColor(Color.parseColor("#0000ff"))
                appartament_number?.setTypeface(null, Typeface.BOLD)
            }else{
                appartament_number?.setTextColor(Color.parseColor("#000000"))
                appartament_number?.setTypeface(null, Typeface.NORMAL)
            }
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