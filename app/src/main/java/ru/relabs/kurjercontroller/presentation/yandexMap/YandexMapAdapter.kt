package ru.relabs.kurjercontroller.presentation.yandexMap

import android.content.res.ColorStateList
import android.graphics.Color
import kotlinx.android.synthetic.main.holder_button.view.*
import kotlinx.android.synthetic.main.holder_load_deliverymans.view.*
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.presentation.base.recycler.IAdapterDelegate
import ru.relabs.kurjercontroller.presentation.base.recycler.delegateDefine
import ru.relabs.kurjercontroller.presentation.base.recycler.holderDefine
import ru.relabs.kurjercontroller.utils.extensions.setSelectButtonActive
import ru.relabs.kurjercontroller.utils.extensions.visible

object YandexMapAdapter {

    fun myPositionAdapter(onClick: () -> Unit): IAdapterDelegate<YandexMapListItem> = delegateDefine(
        { it is YandexMapListItem.MyPosition },
        { p ->
            holderDefine(p, R.layout.holder_button, { it as YandexMapListItem.MyPosition }) { item ->
                itemView.button.text = itemView.resources.getString(R.string.map_button_location)
                itemView.button.setSelectButtonActive(item.selected)
                itemView.button.setOnClickListener { onClick() }
            }
        }
    )

    fun deliverymansPositionAdapter(onClick: () -> Unit): IAdapterDelegate<YandexMapListItem> = delegateDefine(
        { it is YandexMapListItem.LoadDeliverymans },
        { p ->
            holderDefine(p, R.layout.holder_load_deliverymans, { it as YandexMapListItem.LoadDeliverymans }) { item ->
                itemView.loading.visible = item.loading
                itemView.setOnClickListener {
                    if (!item.loading) {
                        onClick()
                    }
                }
            }
        }
    )

    fun commonLayerDelegate(onClick: () -> Unit): IAdapterDelegate<YandexMapListItem> = delegateDefine(
        { it is YandexMapListItem.CommonLayer },
        { p ->
            holderDefine(p, R.layout.holder_button, { it as YandexMapListItem.CommonLayer }) { item ->
                itemView.button.text = itemView.resources.getString(R.string.map_button_layer_common)
                itemView.button.setSelectButtonActive(item.selected)
                itemView.button.setOnClickListener { onClick() }
            }
        }
    )

    fun predefinedLayerDelegate(onClick: () -> Unit): IAdapterDelegate<YandexMapListItem> = delegateDefine(
        { it is YandexMapListItem.PredefinedAddressesLayer },
        { p ->
            holderDefine(p, R.layout.holder_button, { it as YandexMapListItem.PredefinedAddressesLayer }) { item ->
                itemView.button.text = itemView.resources.getString(R.string.map_button_layer_predefined)
                itemView.button.setSelectButtonActive(item.selected)
                itemView.button.setOnClickListener { onClick() }
            }
        }
    )

    fun taskLayerDelegate(onClick: (Task) -> Unit): IAdapterDelegate<YandexMapListItem> = delegateDefine(
        { it is YandexMapListItem.TaskLayer },
        { p ->
            holderDefine(p, R.layout.holder_button, { it as YandexMapListItem.TaskLayer }) { item ->
                itemView.button.text = itemView.resources.getString(R.string.map_button_layer_task, item.task.name)

                if (item.loading) {
                    itemView.button.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                } else {
                    itemView.button.backgroundTintList = null
                }

                itemView.button.setSelectButtonActive(item.selected)

                itemView.setOnClickListener {
                    onClick(item.task)
                }
            }
        }
    )
}