package ru.relabs.kurjercontroller.presentation.fragments.yandexMap.holders

import android.graphics.Color
import android.view.View
import android.widget.Button
import ru.relabs.kurjercontroller.presentation.delegateAdapter.BaseViewHolder
import ru.relabs.kurjercontroller.utils.extensions.setSelectButtonActive
import ru.relabs.kurjercontroller.presentation.fragments.yandexMap.models.YandexMapModel

/**
 * Created by ProOrange on 06.06.2019.
 */
class TaskButtonHolder(
    val view: View,
    val onClicked: (YandexMapModel) -> Unit
) : BaseViewHolder<YandexMapModel>(view) {
    var defaultBackground = (view as Button).background

    override fun onBindViewHolder(item: YandexMapModel) {
        if (item !is YandexMapModel.TaskLayer) return

        if(item.loading){
            (view as Button).setBackgroundColor(Color.GREEN)
        }else{
            (view as Button).background = defaultBackground
        }

        view.setSelectButtonActive(item.selected)
        view.text = "Слой: ${item.task.name}"

        view.setOnClickListener {
            onClicked(item)
        }
    }
}