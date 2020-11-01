//package ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap
//
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import org.koin.core.KoinComponent
//import org.koin.core.inject
//import ru.relabs.kurjercontroller.domain.models.AddressId
//import ru.relabs.kurjercontroller.domain.models.Task
//import ru.relabs.kurjercontroller.domain.repositories.DatabaseRepository
//import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.base.BaseYandexMapPresenter
//
//class AddressYandexMapPresenter(override val fragment: AddressYandexMapFragment): BaseYandexMapPresenter(fragment), KoinComponent {
//
//    val databaseRepository: DatabaseRepository by inject()
//
//    override fun getDeliverymanIDs(): List<Int> {
//        return fragment.deliverymanIds
//    }
//
//    override fun onPredefinedAddressesLayerSelected() {}
//
//    override fun onTaskLayerSelected(taskModel: Task) {}
//
//    override fun onCommonLayerSelected() {}
//
//    fun loadAddresses() {
//        bgScope.launch {
//
//            fragment.addresses = fragment.addressIds.mapNotNull {
//                val address = databaseRepository.getAddress(AddressId(it.id)) ?: return@mapNotNull null
//                return@mapNotNull AddressWithColor(address, it.color, it.outlineColor)
//            }.distinctBy {
//                it.address.idnd
//            }
//
//            withContext(Dispatchers.Main) {
//                fragment.showAddresses()
//                fragment.makeFocus(fragment.addresses.map { it.address })
//            }
//        }
//    }
//}
