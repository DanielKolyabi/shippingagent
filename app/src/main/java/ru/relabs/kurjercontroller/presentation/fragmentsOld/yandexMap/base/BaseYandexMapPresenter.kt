//package ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.base
//
//import com.google.firebase.crashlytics.FirebaseCrashlytics
//import kotlinx.coroutines.*
//import org.koin.core.KoinComponent
//import org.koin.core.inject
//import ru.relabs.kurjercontroller.domain.models.Task
//import ru.relabs.kurjercontroller.domain.repositories.ControlRepository
//import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.models.DeliverymanPositionData
//import ru.relabs.kurjercontroller.utils.CancelableScope
//import ru.relabs.kurjercontroller.utils.Left
//import ru.relabs.kurjercontroller.utils.Right
//import java.util.*
//
//abstract class BaseYandexMapPresenter(open val fragment: BaseYandexMapFragment) : KoinComponent {
//    val controlRepository: ControlRepository by inject()
//    val bgScope = CancelableScope(Dispatchers.Default)
//    var deliverymanPositions = listOf<DeliverymanPositionData>()
//
//    fun onMyPositionClicked() {
//        fragment.moveCameraToUser()
//    }
//
//    abstract fun onTaskLayerSelected(taskModel: Task)
//    abstract fun onCommonLayerSelected()
//    abstract fun onPredefinedAddressesLayerSelected()
//    abstract fun getDeliverymanIDs(): List<Int>
//
//    fun loadDeliverymanPositions() {
//        fragment.setDeliverymanPositionsLoading(true)
//        val deliverymanIDs = getDeliverymanIDs().distinct()
//        bgScope.launch(Dispatchers.IO) {
//            deliverymanPositions = deliverymanIDs
//                .map {
//                    async(Dispatchers.IO) {
//                        when (val r = controlRepository.getUserPosition(it)) {
//                            is Left -> {
//                                FirebaseCrashlytics.getInstance().recordException(r.value)
//                                null
//                            }
//                            is Right -> it to r.value
//                        }
//                    }
//                }
//                .awaitAll()
//                .mapNotNull { it }
//                .map { pair ->
//                    pair.second.mapNotNull {
//                        if (Date().time - it.time.millis < 6 * 60 * 1000)
//                            DeliverymanPositionData(pair.first, it.name, it.lat, it.long)
//                        else
//                            null
//
//                    }
//                }.flatten()
//
//            withContext(Dispatchers.Main) {
//                fragment.updateDeliverymanPositions()
//                fragment.setDeliverymanPositionsLoading(false)
//            }
//        }
//    }
//}
