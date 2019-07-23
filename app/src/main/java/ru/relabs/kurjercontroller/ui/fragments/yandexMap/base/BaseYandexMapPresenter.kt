package ru.relabs.kurjercontroller.ui.fragments.yandexMap.base

import kotlinx.coroutines.*
import ru.relabs.kurjercontroller.CancelableScope
import ru.relabs.kurjercontroller.logError
import ru.relabs.kurjercontroller.models.TaskModel
import ru.relabs.kurjercontroller.network.DeliveryServerAPI
import ru.relabs.kurjercontroller.ui.fragments.yandexMap.models.DeliverymanPositionData
import java.util.*

abstract class BaseYandexMapPresenter(open val fragment: BaseYandexMapFragment) {
    val bgScope = CancelableScope(Dispatchers.Default)
    var deliverymanPositions = listOf<DeliverymanPositionData>()

    fun onMyPositionClicked() {
        fragment.moveCameraToUser()
    }

    abstract fun onTaskLayerSelected(taskModel: TaskModel)
    abstract fun onCommonLayerSelected()
    abstract fun onPredefinedAddressesLayerSelected()
    abstract fun getDeliverymanIDs(): List<Int>

    fun loadDeliverymanPositions() {
        fragment.setDeliverymanPositionsLoading(true)
        val deliverymanIDs = getDeliverymanIDs().distinct()
        bgScope.launch(Dispatchers.IO) {
            deliverymanPositions = deliverymanIDs.map {
                async {
                    try {
                        Pair(it, DeliveryServerAPI.api.requestUserPosition(it).await())
                    } catch (e: Exception) {
                        e.logError()
                        null
                    }
                }
            }
                .awaitAll()
                .mapNotNull { it }
                .map { pair ->
                    pair.second.locations.mapNotNull {
                        if (Date().time - it.location.time.time < 6 * 60 * 1000)
                            DeliverymanPositionData(pair.first, pair.second.name, it.location.lat, it.location.long)
                        else
                            null

                    }
                }.flatten()

            withContext(Dispatchers.Main) {
                fragment.updateDeliverymanPositions()
                fragment.setDeliverymanPositionsLoading(false)
            }
        }
    }
}
