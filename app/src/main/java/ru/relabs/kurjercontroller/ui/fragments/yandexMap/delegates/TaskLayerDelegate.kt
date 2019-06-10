package ru.relabs.kurjercontroller.ui.fragments.yandexMap.delegates

import android.view.ViewGroup
import android.widget.Button
import ru.relabs.kurjer.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjer.ui.delegateAdapter.IAdapterDelegate
import ru.relabs.kurjercontroller.ui.fragments.yandexMap.YandexMapModel
import ru.relabs.kurjercontroller.ui.fragments.yandexMap.holders.TaskButtonHolder

class TaskLayerDelegate(
    val onClicked: (YandexMapModel) -> Unit
) : IAdapterDelegate<YandexMapModel> {

    override fun isForViewType(data: List<YandexMapModel>, position: Int): Boolean {
        return data[position] is YandexMapModel.TaskLayer
    }

    override fun onBindViewHolder(holder: BaseViewHolder<YandexMapModel>, data: List<YandexMapModel>, position: Int) {
        holder.onBindViewHolder(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<YandexMapModel> {
        return TaskButtonHolder(
            Button(parent.context).apply {
                text = "Слой: ---"
                isAllCaps = false
            }
        ) {
            onClicked(it)
        }
    }
}