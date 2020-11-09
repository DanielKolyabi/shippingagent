package ru.relabs.kurjercontroller.presentation.yandexMap

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yandex.mapkit.geometry.Point
import kotlinx.coroutines.*
import ru.relabs.kurjercontroller.domain.models.Address
import ru.relabs.kurjercontroller.domain.models.AddressId
import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.models.DeliverymanPositionData
import ru.relabs.kurjercontroller.presentation.yandexMap.models.AddressIdWithColor
import ru.relabs.kurjercontroller.presentation.yandexMap.models.AddressWithColor
import ru.relabs.kurjercontroller.utils.Left
import ru.relabs.kurjercontroller.utils.Right
import java.util.*

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */
object YandexMapEffects {

    fun effectInit(addressIds: List<AddressIdWithColor>): YandexMapEffect = { c, s ->
        val addresses = addressIds.mapNotNull { awc ->
            c.databaseRepository.getAddress(AddressId(awc.id))?.let { a ->
                AddressWithColor(
                    a,
                    awc.color,
                    awc.outlineColor
                )
            }
        }.distinctBy {
            it.address.idnd
        }

        messages.send(YandexMapMessages.msgAddressesLoaded(addresses))
    }

    fun effectNotifyAddressClicked(address: Address): YandexMapEffect = { c, s ->
        withContext(Dispatchers.Main) {
            c.notifyAddressClicked(address)
            c.router.exit()
        }
    }

    fun effectMoveCameraToUser(): YandexMapEffect = { c, s ->
        val coordinates = c.locationProvider.lastReceivedLocation()
            ?: c.locationProvider.updatesChannel().let {
                val coord = it.receive()
                it.cancel()
                coord
            }

        withContext(Dispatchers.Main) {
            c.moveCameraToUser(Point(coordinates.latitude, coordinates.longitude))
        }
    }

    fun effectLoadDeliverymans(): YandexMapEffect = { c, s ->
        coroutineScope {
            val deliverymanPositions = s.deliverymanIds
                .map {
                    async(Dispatchers.IO) {
                        when (val r = c.controlRepository.getUserPosition(it)) {
                            is Left -> {
                                FirebaseCrashlytics.getInstance().recordException(r.value)
                                null
                            }
                            is Right -> it to r.value
                        }
                    }
                }
                .awaitAll()
                .mapNotNull { it }
                .map { pair ->
                    pair.second.mapNotNull {
                        if (Date().time - it.time.millis < 6 * 60 * 1000)
                            DeliverymanPositionData(pair.first, it.name, it.lat, it.long)
                        else
                            null

                    }
                }.flatten()

            messages.send(YandexMapMessages.msgDeliverymansLoaded(deliverymanPositions))
        }
    }

    fun effectNavigateBack(): YandexMapEffect = { c, s ->
        withContext(Dispatchers.Main) {
            c.router.exit()
        }
    }
}