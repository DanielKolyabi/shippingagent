package ru.relabs.kurjercontroller.presentation.yandexMap

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectTapListener
import kotlinx.android.synthetic.main.fragment_yandex_map.*
import kotlinx.android.synthetic.main.fragment_yandex_map.view.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.models.TaskId
import ru.relabs.kurjercontroller.domain.models.TaskStorage
import ru.relabs.kurjercontroller.presentation.base.fragment.BaseFragment
import ru.relabs.kurjercontroller.presentation.base.recycler.DelegateAdapter
import ru.relabs.kurjercontroller.presentation.base.tea.debugCollector
import ru.relabs.kurjercontroller.presentation.base.tea.defaultController
import ru.relabs.kurjercontroller.presentation.base.tea.rendersCollector
import ru.relabs.kurjercontroller.presentation.base.tea.sendMessage
import ru.relabs.kurjercontroller.presentation.yandexMap.models.AddressIdWithColor
import ru.relabs.kurjercontroller.presentation.yandexMap.models.IAddressClickedConsumer
import ru.relabs.kurjercontroller.presentation.yandexMap.models.INewItemsAddedConsumer
import ru.relabs.kurjercontroller.presentation.yandexMap.models.MapObjectData
import ru.relabs.kurjercontroller.utils.debug


/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */

class YandexMapFragment : BaseFragment() {

    private val controller = defaultController(YandexMapState(), YandexMapContext())
    private var renderJob: Job? = null

    private val clickCallback = MapObjectTapListener { obj, _ ->
        (obj.userData as? MapObjectData.TaskItem)?.let {
            uiScope.sendMessage(controller, YandexMapMessages.msgAddressClicked(it.address))
        }
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val addressIds = arguments?.getParcelableArrayList<AddressIdWithColor>(ARG_ADDRESSES)?.toList() ?: listOf()
        val deliverymanIds = arguments?.getIntArray(ARG_DELIVERYMANS)?.toList().orEmpty()
        val storages = arguments?.getParcelableArrayList<TaskStorage>(ARG_STORAGES)?.toList() ?: listOf()
        val tasks = arguments?.getParcelableArrayList<TaskId>(ARG_TASKS)?.toList() ?: listOf()

        controller.start(YandexMapMessages.msgInit(addressIds, deliverymanIds, storages, tasks))
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.context.moveCameraToUser = {}
        controller.stop()
    }

    override fun onPause() {
        super.onPause()
        uiScope.sendMessage(controller, YandexMapMessages.msgSaveCameraPosition())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_yandex_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MapKitFactory.initialize(this.context)

        val adapter = DelegateAdapter(
            YandexMapAdapter.myPositionAdapter {
                uiScope.sendMessage(controller, YandexMapMessages.msgMyLocationClicked())
            },
            YandexMapAdapter.deliverymansPositionAdapter {
                uiScope.sendMessage(controller, YandexMapMessages.msgDeliverymansLocationClicked())
            },
            YandexMapAdapter.commonLayerDelegate {
                uiScope.sendMessage(controller, YandexMapMessages.msgCommonLayerClicked())
            },
            YandexMapAdapter.predefinedLayerDelegate {
                uiScope.sendMessage(controller, YandexMapMessages.msgPredefinedLayerClicked())
            },
            YandexMapAdapter.taskLayerDelegate {
                uiScope.sendMessage(controller, YandexMapMessages.msgTaskLayerClicked(it))
            }
        )

        view.mapview.map.isRotateGesturesEnabled = false
        view.mapview.map.userLocationLayer.isEnabled = true

        view.controls_list.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.controls_list.adapter = adapter

        bindControls(view)

        renderJob = uiScope.launch {
            val renders = listOf(
                YandexMapRenders.renderAddresses(view.mapview, clickCallback),
                YandexMapRenders.renderStorages(view.mapview),
                YandexMapRenders.renderDeliverymans(view.mapview),
                YandexMapRenders.renderControls(adapter),
                YandexMapRenders.renderAddNewAddressesButton(view.add_button, view.mapview),
                YandexMapRenders.renderCamera(view.mapview)
            )
            launch { controller.stateFlow().collect(rendersCollector(renders)) }
            launch { controller.stateFlow().collect(debugCollector { debug(it) }) }
        }
        controller.context.errorContext.attach(view)
        controller.context.notifyAddressClicked = { (targetFragment as? IAddressClickedConsumer)?.onAddressClicked(it) }
        controller.context.notifyItemsAdded = { (targetFragment as? INewItemsAddedConsumer)?.onItemsAdded(it) }
        controller.context.moveCameraToUser = ::moveCameraToUser
    }

    private fun bindControls(view: View) {
        view.iv_menu.setOnClickListener {
            uiScope.sendMessage(controller, YandexMapMessages.msgNavigateBack())
        }
        view.add_button.setOnClickListener {
            uiScope.sendMessage(controller, YandexMapMessages.msgAddNewAddresses(view.mapview.map.visibleRegion))
        }
        view.mapview.map.addCameraListener { map, cameraPosition, cameraUpdateSource, finished ->
            if (finished) {
                uiScope.sendMessage(controller, YandexMapMessages.msgCameraChanged(cameraPosition))
            }
        }
    }

    private fun moveCameraToUser(location: Point) {
        mapview.map.move(
            mapview.map.userLocationLayer.cameraPosition() ?: CameraPosition(
                location,
                14f, 0f, 0f
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        renderJob?.cancel()
        controller.context.errorContext.detach()
    }

    override fun onStop() {
        super.onStop()
        MapKitFactory.getInstance().onStop()
        view?.mapview?.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        view?.mapview?.onStart()
    }

    override fun interceptBackPressed(): Boolean {
        return false
    }

    companion object {
        const val WRONG_METHOD_OUTLINE_COLOR = Color.BLACK

        const val ARG_ADDRESSES = "address_ids"
        const val ARG_STORAGES = "storages"
        const val ARG_DELIVERYMANS = "deliveryman_ids"
        const val ARG_TASKS = "tasks"

        fun <T> newInstance(
            tasks: List<TaskId>,
            addresses: List<AddressIdWithColor>,
            deliverymanIds: List<Int>,
            storages: List<TaskStorage>,
            targetFragment: T
        ) where T : Fragment, T : IAddressClickedConsumer, T : INewItemsAddedConsumer =
            YandexMapFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_TASKS, ArrayList(tasks.map { it }))
                    putParcelableArrayList(ARG_ADDRESSES, ArrayList(addresses))
                    putParcelableArrayList(ARG_STORAGES, ArrayList(storages))
                    putIntArray(ARG_DELIVERYMANS, deliverymanIds.toIntArray())
                }
                setTargetFragment(targetFragment, 0)
            }
    }
}