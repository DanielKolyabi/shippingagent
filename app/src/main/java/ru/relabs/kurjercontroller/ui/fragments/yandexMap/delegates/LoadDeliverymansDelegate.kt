package ru.relabs.kurjercontroller.ui.fragments.yandexMap.delegates

import android.view.LayoutInflater
import android.view.ViewGroup
import ru.relabs.kurjercontroller.ui.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.ui.delegateAdapter.IAdapterDelegate
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.ui.fragments.yandexMap.models.YandexMapModel
import ru.relabs.kurjercontroller.ui.fragments.yandexMap.holders.LoadDeliverymansHolder

class LoadDeliverymansDelegate(
    val onClicked: (YandexMapModel) -> Unit
) : IAdapterDelegate<YandexMapModel> {

    override fun isForViewType(data: List<YandexMapModel>, position: Int): Boolean {
        return data[position] is YandexMapModel.LoadDeliverymans
    }

    override fun onBindViewHolder(holder: BaseViewHolder<YandexMapModel>, data: List<YandexMapModel>, position: Int) {
        holder.onBindViewHolder(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<YandexMapModel> {
        return LoadDeliverymansHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.holder_load_deliverymans, parent, false)
        ) { onClicked(it) }
    }
}