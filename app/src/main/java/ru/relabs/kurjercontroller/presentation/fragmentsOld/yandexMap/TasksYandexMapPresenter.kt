//package ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap
//
//import kotlinx.android.synthetic.main.fragment_yandex_map.*
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import org.koin.core.KoinComponent
//import org.koin.core.inject
//import ru.relabs.kurjercontroller.data.models.FiltersRequest
//import ru.relabs.kurjercontroller.domain.models.Task
//import ru.relabs.kurjercontroller.domain.models.TaskId
//import ru.relabs.kurjercontroller.domain.models.TaskItem
//import ru.relabs.kurjercontroller.domain.repositories.DatabaseRepository
//import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.base.BaseYandexMapPresenter
//import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.models.YandexMapModel
//import ru.relabs.kurjercontroller.utils.Left
//import ru.relabs.kurjercontroller.utils.Right
//import ru.relabs.kurjercontroller.utils.extensions.setVisible
//import ru.relabs.kurjercontroller.utils.log
//
//class TasksYandexMapPresenter(override val fragment: TasksYandexMapFragment) : BaseYandexMapPresenter(fragment), KoinComponent {
//    val databaseRepository: DatabaseRepository by inject()
//
//    override fun getDeliverymanIDs(): List<Int> {
//        return fragment.tasks
//            .flatMap {
//                it.taskItems
//            }.map {
//                it.deliverymanId
//            }.distinct()
//    }
//
//    override fun onPredefinedAddressesLayerSelected() {
//        fragment.showPredefinedAddresses()
//        fragment.add_button?.setVisible(false)
//    }
//
//    override fun onTaskLayerSelected(taskModel: Task) {
//        fragment.showTask(taskModel)
//        fragment.updateAddButton()
//    }
//
//    override fun onCommonLayerSelected() {
//        fragment.showAllTasks()
//        fragment.add_button?.setVisible(false)
//    }
//
//    fun loadTasks() {
//        bgScope.launch {
//
//            fragment.tasks = fragment.taskIds.mapNotNull {
//                databaseRepository.getTask(TaskId(it))
//            }
//
//            loadNewTaskItems(fragment.tasks)
//
//            withContext(Dispatchers.Main) {
//                fragment.populateControlList()
//                fragment.focusShowedAddresses()
//            }
//        }
//    }
//
//    fun loadNewTaskItems(tasks: List<Task>) = bgScope.launch(Dispatchers.IO) {
//        tasks.filter { it.filtered }.forEach { task ->
//            fragment.setTaskLayerLoading(task, true)
//
//            val currentAddresses = task.taskItems.map { it.address.id }
//
//            when (val r = controlRepository.getFilteredTaskItems(task.taskFilters.all, task.withPlanned)) {
//                is Right -> fragment.newTaskItems.addAll(
//                    r.value
//                        .items
//                        .filter { !currentAddresses.contains(it.address.id) }
//                        .map { it.copy(taskId = task.id) }
//                )
//                is Left -> {
//                    r.value.log()
//                    return@forEach
//                }
//            }
//        }
//        withContext(Dispatchers.Main) {
//            when (val selectedLayer = fragment.getSelectedLayer()) {
//                is YandexMapModel.CommonLayer -> fragment.showAllTasks()
//                is YandexMapModel.TaskLayer -> fragment.showTask(selectedLayer.task)
//            }
//        }
//    }
//
//    fun addNewTaskItems(selectedLayer: YandexMapModel.TaskLayer, newTaskItems: List<TaskItem>) =
//        bgScope.launch(Dispatchers.IO) {
////            selectedLayer.task.taskItems.addAll(newTaskItems)
//
//            newTaskItems.forEach {
////                databaseRepository.saveTaskItem(it)
//            }
//
//            withContext(Dispatchers.Main) {
//                fragment.add_button?.isEnabled = true
//                fragment.updateAddButton()
//                fragment.showTask(selectedLayer.task)
//            }
//        }
//
//    fun updateCurrentLayer() {
//        return when (val layer = fragment.getSelectedLayer()) {
//            YandexMapModel.CommonLayer -> onCommonLayerSelected()
//            YandexMapModel.PredefinedAddressesLayer -> onPredefinedAddressesLayerSelected()
//            is YandexMapModel.TaskLayer -> onTaskLayerSelected(layer.task)
//            else -> {
//            }
//        }
//    }
//}
