package ru.relabs.kurjercontroller.presentation.yandexMap

import android.graphics.Color
import android.widget.Button
import androidx.core.graphics.ColorUtils
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.*
import com.yandex.mapkit.mapview.MapView
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.models.Address
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskId
import ru.relabs.kurjercontroller.presentation.base.recycler.DelegateAdapter
import ru.relabs.kurjercontroller.presentation.base.tea.renderT
import ru.relabs.kurjercontroller.presentation.yandexMap.iconProviders.ColoredIconProvider
import ru.relabs.kurjercontroller.presentation.yandexMap.iconProviders.DeliverymanIconProvider
import ru.relabs.kurjercontroller.presentation.yandexMap.models.MapObjectData
import ru.relabs.kurjercontroller.utils.extensions.visible

/**
 * Created by Daniil Kurchanov on 06.04.2020.
 */
object YandexMapRenders {

    fun renderAddresses(mapview: MapView, onAddressClicked: (Address) -> Unit): YandexMapRender = renderT(
        { it.addresses },
        {
            mapview.map.mapObjects.forEach {
                if (it.userData is MapObjectData.TaskItem) {
                    mapview.map.mapObjects.remove(it)
                }
            }
            it.forEach {
                val address = it.address
                if (address.lat != 0.0 && address.long != 0.0) {
                    val point = Point(address.lat, address.long)

                    mapview.map.mapObjects
                        .addPlacemark(point, ColoredIconProvider(mapview.context, it.color))
                        .apply {
                            userData = MapObjectData.TaskItem(address)
                            addTapListener { obj, _ ->
                                (obj.userData as? MapObjectData.TaskItem)?.let {
                                    onAddressClicked(it.address)
                                }
                                return@addTapListener true
                            }
                        }


                    mapview.map.mapObjects.addCircle(
                        Circle(point, 50f), it.outlineColor, 2f, ColorUtils.setAlphaComponent(it.color, 80)
                    ).apply {
                        userData = MapObjectData.TaskItem(address)
                    }
                }
            }
        }
    )

    fun renderStorages(mapview: MapView): YandexMapRender = renderT(
        { it.storages },
        {
            mapview.map.mapObjects.forEach {
                if (it.userData is MapObjectData.Storage) {
                    mapview.map.mapObjects.remove(it)
                }
            }

            it.forEach {
                mapview.map.mapObjects.addPlacemark(
                    Point(it.lat.toDouble(), it.long.toDouble()),
                    ColoredIconProvider(mapview.context, Color.BLACK)
                ).apply {
                    userData = MapObjectData.Storage
                }

                mapview.map.mapObjects.addCircle(
                    Circle(Point(it.lat.toDouble(), it.long.toDouble()), 20f),
                    Color.BLACK,
                    2f,
                    ColorUtils.setAlphaComponent(Color.BLACK, 80)
                ).apply {
                    userData = MapObjectData.Storage
                }
            }
        }
    )

    fun renderDeliverymans(mapview: MapView): YandexMapRender = renderT(
        { it.deliverymans },
        {
            mapview.map.mapObjects.forEach {
                if (it.userData is MapObjectData.DeliveryMan) {
                    mapview.map.mapObjects.remove(it)
                }
            }

            it.forEach { d ->
                mapview.map.mapObjects.addPlacemark(
                    Point(d.lat.toDouble(), d.long.toDouble()),
                    DeliverymanIconProvider(
                        mapview.context,
                        d.name
                    )
                ).apply {
                    userData = MapObjectData.DeliveryMan
                }
            }
        }
    )

    private data class RenderControlsData(
        val isDeliverymansLoading: Boolean,
        val selectedLayer: MapLayer,
        val tasks: List<Task>,
        val taskLoadings: Map<TaskId, Boolean>
    )

    fun renderControls(adapter: DelegateAdapter<YandexMapListItem>): YandexMapRender = renderT(
        { RenderControlsData(it.deliverymansLoading, it.selectedLayer, it.tasks, it.taskLoadings) },
        { (deliverymansLoading, selectedLayer, tasks, taskLoadings) ->
            adapter.items.clear()
            adapter.items.addAll(
                listOfNotNull(
                    YandexMapListItem.MyPosition(false),
                    YandexMapListItem.LoadDeliverymans(deliverymansLoading, false),
                    YandexMapListItem.CommonLayer(selectedLayer == MapLayer.Common).takeIf { tasks.size > 1 },
                    YandexMapListItem.PredefinedAddressesLayer(selectedLayer == MapLayer.Predefined).takeIf { tasks.size > 1 }
                ) + tasks
                    .map {
                        YandexMapListItem.TaskLayer(
                            it,
                            taskLoadings.getOrElse(it.id) { false },
                            selectedLayer is MapLayer.TaskLayer && selectedLayer.task.id == it.id
                        )
                    }
                    .takeIf { tasks.size > 1 }
                    .orEmpty()
            )
            adapter.notifyDataSetChanged()
        }
    )

    fun renderAddNewAddressesButton(btn: Button, map: MapView): YandexMapRender = renderT(
        { Triple(it.newTaskItems, it.selectedLayer, it.cameraPosition to it.cameraZoom) },
        { (newTaskItems, selectedLayer, _) ->
            if (selectedLayer is MapLayer.TaskLayer) {
                val visibleRegion = map.map.visibleRegion

                val selectedNewTaskItems = newTaskItems.filter {
                    it.taskId == selectedLayer.task.id && isAddressInVisibleRegion(it.address, visibleRegion)
                }

                if (selectedNewTaskItems.isNotEmpty()) {
                    btn.text = btn.resources.getString(
                        R.string.yandex_map_add_button,
                        selectedNewTaskItems.size.toString()
                    )
                }
            } else {
                btn.visible = false
            }
        }
    )

    fun isAddressInVisibleRegion(address: Address, region: VisibleRegion): Boolean {
        return address.lat > region.bottomLeft.latitude && address.long < region.topRight.longitude &&
                address.long > region.bottomLeft.longitude && address.long < region.topRight.longitude
    }

    fun renderCamera(mapview: MapView): YandexMapRender = renderT(
        { Triple(it.cameraPosition, it.cameraZoom, it.cameraUpdateRequired) },
        { (position, zoom, required) ->
            if (required) {
                position?.let {
                    mapview.map.move(CameraPosition(Point(position.first, position.second), zoom, 0f, 0f))
                }
            }
        }
    )
}

inline fun MapObjectCollection.forEach(crossinline block: (MapObject) -> Unit) {
    this.traverse(object : MapObjectVisitor {
        override fun onPolygonVisited(p0: PolygonMapObject) = block(p0)
        override fun onCircleVisited(p0: CircleMapObject) = block(p0)
        override fun onPolylineVisited(p0: PolylineMapObject) = block(p0)
        override fun onColoredPolylineVisited(p0: ColoredPolylineMapObject) = block(p0)
        override fun onPlacemarkVisited(p0: PlacemarkMapObject) = block(p0)
        override fun onCollectionVisitEnd(p0: MapObjectCollection) {}
        override fun onCollectionVisitStart(p0: MapObjectCollection): Boolean = true
    })
}