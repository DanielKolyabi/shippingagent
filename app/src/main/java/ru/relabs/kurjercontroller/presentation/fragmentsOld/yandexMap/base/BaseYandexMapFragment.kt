package ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.base

import android.graphics.Color

//
//import android.graphics.Color
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.core.graphics.ColorUtils
//import androidx.fragment.app.Fragment
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.yandex.mapkit.MapKitFactory
//import com.yandex.mapkit.geometry.BoundingBox
//import com.yandex.mapkit.geometry.Circle
//import com.yandex.mapkit.geometry.Point
//import com.yandex.mapkit.map.CameraPosition
//import com.yandex.mapkit.map.MapObject
//import com.yandex.mapkit.user_location.UserLocationLayer
//import kotlinx.android.synthetic.main.fragment_yandex_map.*
//import kotlinx.coroutines.launch
//import ru.relabs.kurjercontroller.presentation.delegateAdapter.DelegateAdapter
//import ru.relabs.kurjercontroller.R
//import ru.relabs.kurjercontroller.application
//import ru.relabs.kurjercontroller.domain.models.Address
//import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.AddressWithColor
//import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.ColoredIconProvider
//import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.DeliverymanIconProvider
//import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.delegates.*
//import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.models.YandexMapModel
//
val WRONG_METHOD_OUTLINE_COLOR = Color.BLACK//Color.parseColor("#8B4513")
//
//abstract class BaseYandexMapFragment : Fragment() {
//    private lateinit var userLocationLayer: UserLocationLayer
//    abstract val presenter: BaseYandexMapPresenter
//    private var onClickCallback: Callback? = null
//    private val deliverymansIcons: MutableList<MapObject> = mutableListOf()
//
//    val adapter = DelegateAdapter<YandexMapModel>().apply {
//        addDelegate(MyPositionDelegate {
//            presenter.onMyPositionClicked()
//        })
//        addDelegate(CommonLayerDelegate {
//            setSelectedControlButton(it)
//            presenter.onCommonLayerSelected()
//        })
//        addDelegate(PredefinedAddressesLayerDelegate {
//            setSelectedControlButton(it)
//            presenter.onPredefinedAddressesLayerSelected()
//        })
//        addDelegate(TaskLayerDelegate {
//            setSelectedControlButton(it)
//            (it as? YandexMapModel.TaskLayer)?.let {
//                presenter.onTaskLayerSelected(it.task)
//            }
//        })
//        addDelegate(LoadDeliverymansDelegate {
//            presenter.loadDeliverymanPositions()
//        })
//    }
//
//    private fun setSelectedControlButton(selectedModel: YandexMapModel?) {
//        if (selectedModel is YandexMapModel.MyPosition) return
//        adapter.data.forEach {
//            it.selected = false
//        }
//        selectedModel?.selected = true
//
//        adapter.notifyDataSetChanged()
//    }
//
//    fun setOnClickCallback(callback: Callback) {
//        this.onClickCallback = callback
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_yandex_map, container, false)
//    }
//
//    fun showAddress(addressWithColor: AddressWithColor) {
//        val address = addressWithColor.address
//        if (address.lat != 0.0 && address.long != 0.0) {
//            val ctx = context ?: return
//            val point = Point(address.lat, address.long)
//
//            val color = addressWithColor.color
//            val outlineColor = addressWithColor.outlineColor
//
//            mapview.map.mapObjects
//                .addPlacemark(
//                    Point(address.lat, address.long),
//                    ColoredIconProvider(ctx, color)
//                )
//                .addTapListener { _, _ ->
//                    presenter.bgScope.launch {
//                        onClickCallback?.onAddressClicked(address)
//                    }
//                    application().router.exit()
//                    return@addTapListener true
//                }
//
//            mapview.map.mapObjects.addCircle(
//                Circle(point, 50f),
//                outlineColor,
//                2f,
//                ColorUtils.setAlphaComponent(color, 80)
//            )
//        }
//    }
//
//    fun showStorage(lat: Double, long: Double){
//        val ctx = context ?: return
//        val color = resources.getColor(R.color.black)
//
//        mapview.map.mapObjects
//            .addPlacemark(
//                Point(lat, long),
//                ColoredIconProvider(ctx, color)
//            )
//
//        mapview.map.mapObjects.addCircle(
//            Circle(Point(lat, long), 20f),
//            color,
//            2f,
//            ColorUtils.setAlphaComponent(color, 80)
//        )
//    }
//
//    fun clearMap() {
//        mapview.map.mapObjects.clear()
//    }
//
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        MapKitFactory.initialize(this.context)
//
//        val point = Point(application().currentLocation.lat, application().currentLocation.long)
//        mapview.map.isRotateGesturesEnabled = false
//        mapview.map.move(
//            CameraPosition(point, 14f, 0f, 0f)
//        )
//
//        userLocationLayer = mapview.map.userLocationLayer
//        userLocationLayer.isEnabled = true
//
//        controls_list?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
//        controls_list?.adapter = adapter
//
//        populateControlList()
//    }
//
//    fun updateDeliverymanPositions() {
//        deliverymansIcons.forEach {
//            try {
//                mapview.map.mapObjects.remove(it)
//            } catch (e: Exception) {
//            }
//        }
//        deliverymansIcons.clear()
//
//        context?.let { ctx ->
//
//            presenter.deliverymanPositions.forEach {
//                with(mapview.map.mapObjects) {
//                    deliverymansIcons.add(
//                        addPlacemark(
//                            Point(it.lat.toDouble(), it.long.toDouble()),
//                            DeliverymanIconProvider(ctx, it.name)
//                        )
//                    )
//                }
//            }
//        }
//    }
//
//    abstract fun onControlListPopulation()
//    fun populateControlList() {
//        adapter.data.clear()
//        adapter.data.add(YandexMapModel.MyPosition)
//        adapter.data.add(YandexMapModel.LoadDeliverymans(false))
//        onControlListPopulation()
//        adapter.notifyDataSetChanged()
//        setSelectedControlButton(YandexMapModel.CommonLayer)
//
//        adapter.data.firstOrNull { it !is YandexMapModel.MyPosition }?.let {
//            when (it) {
//                is YandexMapModel.TaskLayer -> {
//                    presenter.onTaskLayerSelected(it.task)
//                }
//                is YandexMapModel.CommonLayer -> {
//                    presenter.onCommonLayerSelected()
//                }
//            }
//
//            setSelectedControlButton(it)
//        }
//    }
//
//    override fun onStop() {
//        super.onStop()
//        presenter.bgScope.cancel()
//        MapKitFactory.getInstance().onStop()
//        mapview.onStop()
//    }
//
//    override fun onStart() {
//        super.onStart()
//        MapKitFactory.getInstance().onStart()
//        mapview.onStart()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        presenter.bgScope.terminate()
//    }
//
//    abstract fun shouldSaveCameraPosition(): Boolean
//
//    override fun onPause() {
//        super.onPause()
//        if (shouldSaveCameraPosition()) {
//            savedCameraPosition = mapview?.map?.cameraPosition
//        }
//    }
//
//    fun getCameraPosition(addresses: List<Address>): CameraPosition {
//        when {
//            addresses.isEmpty() -> {
//                return CameraPosition(
//                    Point(application().currentLocation.lat, application().currentLocation.long),
//                    14f, 0f, 0f
//                )
//            }
//            addresses.size == 1 -> {
//                val address = addresses.first()
//                return CameraPosition(
//                    Point(address.lat, address.long),
//                    14f, 0f, 0f
//                )
//            }
//            else -> {
//                val filtered = addresses.filter { it.lat != 0.0 && it.long != 0.0 }
//                val minLat = filtered.minBy { it.lat }?.lat
//                val maxLat = filtered.maxBy { it.lat }?.lat
//                val minLong = filtered.minBy { it.long }?.long
//                val maxLong = filtered.maxBy { it.long }?.long
//                if (minLat == null || maxLat == null || minLong == null || maxLong == null) {
//                    return getCameraPosition(listOfNotNull(addresses.firstOrNull()))
//
//                }
//                return mapview?.map?.cameraPosition(BoundingBox(Point(minLat, minLong), Point(maxLat, maxLong)))
//                    ?: getCameraPosition(listOfNotNull(addresses.firstOrNull()))
//            }
//        }
//    }
//
//    fun makeFocus(addresses: List<Address>) {
//
//        mapview?.map?.move(
//            savedCameraPosition
//                ?: getCameraPosition(addresses)
//        )
//        savedCameraPosition = null
//    }
//
//    fun moveCameraToUser() {
//        mapview.map.move(
//            userLocationLayer.cameraPosition() ?: CameraPosition(
//                Point(application().currentLocation.lat, application().currentLocation.long),
//                14f, 0f, 0f
//            )
//        )
//    }
//
//    fun setDeliverymanPositionsLoading(loading: Boolean) {
//        val idx = adapter.data.indexOfFirst { it is YandexMapModel.LoadDeliverymans }
//        if (idx < 0) {
//            return
//        }
//        (adapter.data[idx] as? YandexMapModel.LoadDeliverymans)?.loading = loading
//        adapter.notifyItemChanged(idx)
//    }
//
//    companion object {
//
//        var savedCameraPosition: CameraPosition? = null
//    }
//
//
//    interface Callback {
//        suspend fun onAddressClicked(address: Address)
//    }
//}
//
