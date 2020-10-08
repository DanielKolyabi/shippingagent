package ru.relabs.kurjercontroller.ui.fragments.yandexMap.holders

import android.view.View
import kotlinx.android.synthetic.main.holder_load_deliverymans.view.*
import ru.relabs.kurjercontroller.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.utils.extensions.setVisible
import ru.relabs.kurjercontroller.ui.fragments.yandexMap.models.YandexMapModel

/**
 * Created by ProOrange on 06.06.2019.
 */
class LoadDeliverymansHolder(
    val view: View,
    val onClicked: (YandexMapModel) -> Unit
) : BaseViewHolder<YandexMapModel>(view) {
    override fun onBindViewHolder(item: YandexMapModel) {
        if (item !is YandexMapModel.LoadDeliverymans) return

        view.loading.setVisible(item.loading)

        view.icon.setOnClickListener {
            if (!item.loading) {

                onClicked(item)
            }
        }
    }
}
