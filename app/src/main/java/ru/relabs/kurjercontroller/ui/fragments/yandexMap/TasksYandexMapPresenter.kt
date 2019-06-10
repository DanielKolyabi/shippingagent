package ru.relabs.kurjercontroller.ui.fragments.yandexMap

import kotlinx.android.synthetic.main.fragment_yandex_map.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.logError
import ru.relabs.kurjercontroller.models.TaskItemModel
import ru.relabs.kurjercontroller.models.TaskModel
import ru.relabs.kurjercontroller.network.DeliveryServerAPI
import ru.relabs.kurjercontroller.network.models.FiltersRequest
import ru.relabs.kurjercontroller.ui.extensions.setVisible
import ru.relabs.kurjercontroller.ui.fragments.yandexMap.base.BaseYandexMapPresenter

class TasksYandexMapPresenter(override val fragment: TasksYandexMapFragment) : BaseYandexMapPresenter(fragment) {
    override fun onPredefinedAddressesLayerSelected() {
        fragment.showPredefinedAddresses()
        fragment.add_button?.setVisible(false)
    }

    override fun onTaskLayerSelected(taskModel: TaskModel) {
        fragment.showTask(taskModel)
        fragment.updateAddButton()
    }

    override fun onCommonLayerSelected() {
        fragment.showAllTasks()
        fragment.add_button?.setVisible(false)
    }

    fun loadTasks() {
        bgScope.launch {

            fragment.tasks = fragment.taskIds.mapNotNull {
                application().tasksRepository.getTask(it)
            }

            loadNewTaskItems(fragment.tasks)

            withContext(Dispatchers.Main) {
                fragment.populateControlList()
                fragment.focusShowedAddresses()
            }
        }
    }

    fun loadNewTaskItems(tasks: List<TaskModel>) = bgScope.launch(Dispatchers.IO) {
        val token = application().user.getUserCredentials()?.token ?: return@launch

        tasks.filter { it.filtered }.forEach { task ->
            fragment.setTaskLayerLoading(task, true)
            val response = try {
                DeliveryServerAPI.api.getFilteredTaskItems(
                    token,
                    FiltersRequest.fromFiltersList(task.taskFilters.all)
                ).await()
            } catch (e: Exception) {
                e.logError()
                return@forEach
            }

            val currentAddresses = task.taskItems.map { it.address.id }

            fragment.newTaskItems.addAll(
                response.items
                    .filter { !currentAddresses.contains(it.address.id) }
                    .map { it.copy(taskId = task.id).toModel() }
            )
            fragment.setTaskLayerLoading(task, false)
        }
        withContext(Dispatchers.Main) {
            when (val selectedLayer = fragment.getSelectedLayer()) {
                is YandexMapModel.CommonLayer -> fragment.showAllTasks()
                is YandexMapModel.TaskLayer -> fragment.showTask(selectedLayer.task)
            }
        }
    }

    fun addNewTaskItems(selectedLayer: YandexMapModel.TaskLayer, newTaskItems: List<TaskItemModel>) = bgScope.launch(Dispatchers.IO) {
        selectedLayer.task.taskItems.addAll(newTaskItems)

        newTaskItems.forEach {
            application().tasksRepository.saveTaskItem(it)
        }

        withContext(Dispatchers.Main) {
            fragment.add_button?.isEnabled = true
            fragment.updateAddButton()
            fragment.showTask(selectedLayer.task)
        }
    }
}
