package ru.relabs.kurjercontroller.presentation.yandexMap

import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.relabs.kurjercontroller.presentation.base.tea.*

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */

data class YandexMapState(
    val data: Any? = null
)

class YandexMapContext(val errorContext: ErrorContextImpl = ErrorContextImpl()) :
    ErrorContext by errorContext,
    RouterContext by RouterContextMainImpl(),
    KoinComponent {

}

typealias YandexMapMessage = ElmMessage<YandexMapContext, YandexMapState>
typealias YandexMapEffect = ElmEffect<YandexMapContext, YandexMapState>
typealias YandexMapRender = ElmRender<YandexMapState>