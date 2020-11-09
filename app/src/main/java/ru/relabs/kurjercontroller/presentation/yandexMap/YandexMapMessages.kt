package ru.relabs.kurjercontroller.presentation.yandexMap

import ru.relabs.kurjercontroller.presentation.base.tea.msgEffects
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffect
import ru.relabs.kurjercontroller.presentation.base.tea.msgState

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */

object YandexMapMessages {
    fun msgInit(): YandexMapMessage = msgEffects(
        { it },
        { listOf(YandexMapEffects.effectInit()) }
    )
}