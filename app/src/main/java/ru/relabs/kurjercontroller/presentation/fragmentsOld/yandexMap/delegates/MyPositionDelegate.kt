package ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.delegates

import android.view.ViewGroup
import android.widget.Button
import ru.relabs.kurjercontroller.presentation.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.presentation.delegateAdapter.IAdapterDelegate
import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.models.YandexMapModel
import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.holders.ButtonHolder

class MyPositionDelegate(
    val onClicked: (YandexMapModel) -> Unit
) : IAdapterDelegate<YandexMapModel> {

    override fun isForViewType(data: List<YandexMapModel>, position: Int): Boolean {
        return data[position] is YandexMapModel.MyPosition
    }

    override fun onBindViewHolder(holder: BaseViewHolder<YandexMapModel>, data: List<YandexMapModel>, position: Int) {
        holder.onBindViewHolder(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<YandexMapModel> {
        return ButtonHolder(
            Button(parent.context).apply {
                text = "Где я?"
                isAllCaps = false
            }
        ) { onClicked(it) }
    }
}