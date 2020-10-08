package ru.relabs.kurjercontroller.presentation.fragments.addressList.holders

import android.view.View
import kotlinx.android.synthetic.main.holder_addr_list_sorting.view.*
import ru.relabs.kurjercontroller.presentation.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.presentation.fragments.addressList.AddressListModel
import ru.relabs.kurjercontroller.presentation.helpers.TaskAddressSorter

/**
 * Created by ProOrange on 29.08.2018.
 */
class AddressListSortingHolder(itemView: View, val onSortingChanged: (sortingMethod: Int) -> Unit) :
    BaseViewHolder<AddressListModel>(itemView) {
    override fun onBindViewHolder(item: AddressListModel) {
        if (item !is AddressListModel.SortingItem) return
        itemView.sort_alphabetic.setOnClickListener {
            onSortingChanged(TaskAddressSorter.ALPHABETIC)
            setEnabledSorting(TaskAddressSorter.ALPHABETIC)
        }
        itemView.sort_standart.setOnClickListener {
            onSortingChanged(TaskAddressSorter.STANDART)
            setEnabledSorting(TaskAddressSorter.STANDART)
        }

        itemView.sort_standart.text = itemView.resources.getString(
            if (item.isTaskFiltered) {
                R.string.sort_by_time_button
            } else {
                R.string.sort_standart_button
            }
        )

        setEnabledSorting(item.selectedSortType) //default
    }

    fun setEnabledSorting(sortingMethod: Int) {
        if (sortingMethod == TaskAddressSorter.STANDART) {
            itemView.sort_standart.setBackgroundColor(itemView.resources.getColor(R.color.colorAccent))
            itemView.sort_alphabetic.setBackgroundColor(itemView.resources.getColor(R.color.button_material_light))
        }
        if (sortingMethod == TaskAddressSorter.ALPHABETIC) {
            itemView.sort_alphabetic.setBackgroundColor(itemView.resources.getColor(R.color.colorAccent))
            itemView.sort_standart.setBackgroundColor(itemView.resources.getColor(R.color.button_material_light))
        }
    }
}