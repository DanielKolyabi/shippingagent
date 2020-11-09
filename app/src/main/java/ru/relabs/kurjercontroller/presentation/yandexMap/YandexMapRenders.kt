package ru.relabs.kurjercontroller.presentation.yandexMap

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.*
import com.yandex.mapkit.mapview.MapView
import ru.relabs.kurjercontroller.domain.models.Address
import ru.relabs.kurjercontroller.presentation.base.recycler.DelegateAdapter
import ru.relabs.kurjercontroller.presentation.base.tea.renderT
import ru.relabs.kurjercontroller.presentation.yandexMap.iconProviders.ColoredIconProvider
import ru.relabs.kurjercontroller.presentation.yandexMap.iconProviders.DeliverymanIconProvider

/**
 * Created by Daniil Kurchanov on 06.04.2020.
 */
object YandexMapRenders {

    fun renderAddresses(mapview: MapView, onAddressClicked: (Address) -> Unit): YandexMapRender = renderT(
        { it.addresses },
        {
            mapview.map.mapObjects.forEach {
                if (it.userData as? MapObjectType == MapObjectType.Address) {
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
                            userData = MapObjectType.Address
                            addTapListener { _, _ ->
                                onAddressClicked(address)
                                return@addTapListener true
                            }
                        }


                    mapview.map.mapObjects.addCircle(
                        Circle(point, 50f), it.outlineColor, 2f, ColorUtils.setAlphaComponent(it.color, 80)
                    ).apply {
                        userData = MapObjectType.Address
                    }
                }
            }
        }
    )

    fun renderStorages(mapview: MapView): YandexMapRender = renderT(
        { it.storages },
        {
            mapview.map.mapObjects.forEach {
                if (it.userData as? MapObjectType == MapObjectType.Storage) {
                    mapview.map.mapObjects.remove(it)
                }
            }

            it.forEach {
                mapview.map.mapObjects.addPlacemark(
                    Point(it.lat.toDouble(), it.long.toDouble()),
                    ColoredIconProvider(mapview.context, Color.BLACK)
                ).apply {
                    userData = MapObjectType.Storage
                }

                mapview.map.mapObjects.addCircle(
                    Circle(Point(it.lat.toDouble(), it.long.toDouble()), 20f),
                    Color.BLACK,
                    2f,
                    ColorUtils.setAlphaComponent(Color.BLACK, 80)
                ).apply {
                    userData = MapObjectType.Storage
                }
            }
        }
    )

    fun renderDeliverymans(mapview: MapView): YandexMapRender = renderT(
        { it.deliverymans },
        {
            mapview.map.mapObjects.forEach {
                if (it.userData as? MapObjectType == MapObjectType.DeliveryMan) {
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
                    userData = MapObjectType.DeliveryMan
                }
            }
        }
    )

    fun renderControls(adapter: DelegateAdapter<YandexMapListItem>): YandexMapRender = renderT(
        { it.deliverymansLoading },
        {
            adapter.items.clear()
            adapter.items.addAll(
                listOfNotNull(
                    YandexMapListItem.MyPosition(false),
                    YandexMapListItem.LoadDeliverymans(it, false)
                )
            )
            adapter.notifyDataSetChanged()
        }
    )

    private enum class MapObjectType {
        Address, Storage, DeliveryMan
    }
}

inline fun MapObjectCollection.forEach(crossinline block: (MapObject) -> Unit) {
    this.traverse(object : MapObjectVisitor {
        override fun onPolygonVisited(p0: PolygonMapObject) = block(p0)
        override fun onCircleVisited(p0: CircleMapObject) = block(p0)
        override fun onPolylineVisited(p0: PolylineMapObject) = block(p0)
        override fun onColoredPolylineVisited(p0: ColoredPolylineMapObject) = block(p0)
        override fun onPlacemarkVisited(p0: PlacemarkMapObject) = block(p0)
        override fun onCollectionVisitEnd(p0: MapObjectCollection) {}
        override fun onCollectionVisitStart(p0: MapObjectCollection): Boolean = false
    })
}