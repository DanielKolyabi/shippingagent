package ru.relabs.kurjercontroller.ui.fragments.yandexMap.holders

import android.view.View
import android.widget.Button
import ru.relabs.kurjer.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.ui.extensions.setSelectButtonActive
import ru.relabs.kurjercontroller.ui.fragments.yandexMap.YandexMapModel

/**
 * Created by ProOrange on 06.06.2019.
 */
class ButtonHolder(
    val view: View,
    val onClicked: (YandexMapModel) -> Unit
) : BaseViewHolder<YandexMapModel>(view) {
    override fun onBindViewHolder(item: YandexMapModel) {
        (view as Button).setSelectButtonActive(item.selected)

        view.setOnClickListener {
            onClicked(item)
        }
    }
}