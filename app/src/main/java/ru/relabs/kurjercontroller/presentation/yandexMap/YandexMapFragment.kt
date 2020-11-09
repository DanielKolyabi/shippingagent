package ru.relabs.kurjercontroller.presentation.yandexMap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.presentation.base.fragment.BaseFragment
import ru.relabs.kurjercontroller.presentation.base.tea.debugCollector
import ru.relabs.kurjercontroller.presentation.base.tea.defaultController
import ru.relabs.kurjercontroller.presentation.base.tea.rendersCollector
import ru.relabs.kurjercontroller.presentation.base.tea.sendMessage
import ru.relabs.kurjercontroller.utils.debug


/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */

class YandexMapFragment : BaseFragment() {

    private val controller = defaultController(YandexMapState(), YandexMapContext())
    private var renderJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        controller.start(YandexMapMessages.msgInit())
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.stop()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_yandex_map2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindControls(view)

        renderJob = uiScope.launch {
            val renders = emptyList<YandexMapRender>()
            launch { controller.stateFlow().collect(rendersCollector(renders)) }
            launch { controller.stateFlow().collect(debugCollector { debug(it) }) }
        }
        controller.context.errorContext.attach(view)
    }

    private fun bindControls(
        view: View
    ) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        renderJob?.cancel()
        controller.context.errorContext.detach()
    }

    override fun interceptBackPressed(): Boolean {
        return false
    }

    companion object {
        fun newInstance() = YandexMapFragment()
    }
}