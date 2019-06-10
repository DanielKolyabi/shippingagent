package ru.relabs.kurjercontroller.ui.fragments.yandexMap

import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.yandex.mapkit.map.VisibleRegion
import kotlinx.android.synthetic.main.fragment_yandex_map.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.models.AddressModel
import ru.relabs.kurjercontroller.models.TaskItemModel
import ru.relabs.kurjercontroller.models.TaskModel
import ru.relabs.kurjercontroller.ui.extensions.setVisible
import ru.relabs.kurjercontroller.ui.fragments.yandexMap.base.BaseYandexMapFragment

class TasksYandexMapFragment : BaseYandexMapFragment() {

    override fun shouldSaveCameraPosition(): Boolean {
        return true
    }


    var onNewTaskItemsAdded: (() -> Unit)? = null

    var taskIds: List<Int> = listOf()
    var tasks: List<TaskModel> = listOf()
    val newTaskItems: MutableList<TaskItemModel> = mutableListOf()

    var showedAddresses: List<AddressWithColor> = listOf()

    override val presenter = TasksYandexMapPresenter(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        add_button?.setOnClickListener {
            val selectedLayer = getSelectedLayer() as? YandexMapModel.TaskLayer ?: return@setOnClickListener
            val visibleRegion = mapview?.map?.visibleRegion ?: return@setOnClickListener

            val selectedNewTaskItems = newTaskItems.filter {
                it.taskId == selectedLayer.task.id && isAddressInVisibleRegion(
                    it.address,
                    visibleRegion
                )
            }
            if (selectedNewTaskItems.isNotEmpty()) {
                onNewTaskItemsAdded?.invoke()
            }
            val selectedIds = selectedNewTaskItems.map { it.address.idnd }
            add_button?.isEnabled = false

            newTaskItems.removeAll {
                selectedIds.contains(it.address.idnd)
            }

            presenter.addNewTaskItems(selectedLayer, selectedNewTaskItems)
        }

        presenter.loadTasks()
        mapview.map.addCameraListener { map, _, _, b ->
            if (!b) return@addCameraListener

            updateAddButton()
        }
    }

    fun updateAddButton() {
        val selectedLayer = getSelectedLayer() as? YandexMapModel.TaskLayer ?: return

        val visibleRegion = mapview?.map?.visibleRegion ?: return

        val selectedNewTaskItems = newTaskItems.filter {
            it.taskId == selectedLayer.task.id && isAddressInVisibleRegion(
                it.address,
                visibleRegion
            )
        }

        if (selectedNewTaskItems.isNotEmpty()) {
            add_button?.setVisible(true)
            add_button?.text = resources.getString(R.string.yandex_map_add_button, selectedNewTaskItems.size.toString())
        } else {
            add_button?.setVisible(false)
        }
    }

    fun isAddressInVisibleRegion(address: AddressModel, region: VisibleRegion): Boolean {
        return address.lat > region.bottomLeft.latitude && address.long < region.topRight.longitude &&
                address.long > region.bottomLeft.longitude && address.long < region.topRight.longitude
    }

    override fun onControlListPopulation() {
        adapter.data.add(YandexMapModel.MyPosition)
        adapter.data.add(YandexMapModel.CommonLayer)
        adapter.data.add(YandexMapModel.PredefinedAddressesLayer)
        tasks.filter { it.filtered }.forEach {
            adapter.data.add(YandexMapModel.TaskLayer(it))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            taskIds = it.getIntegerArrayList("task_ids")?.toList() ?: listOf()
        }
    }

    fun getSelectedLayer(): YandexMapModel? {
        return adapter.data.firstOrNull { it.selected }
    }

    fun showAllTasks() {
        clearMap()

        val addresses = tasks
            .flatMap {
                it.taskItems
            }.map {
                AddressWithColor(it.address, Color.CYAN)
            }

        val newAddresses = newTaskItems.map { AddressWithColor(it.address, Color.rgb(0, 100, 0)) }

        showedAddresses = addresses + newAddresses
        showedAddresses.forEach(::showAddress)
    }

    fun showTask(task: TaskModel) {
        clearMap()
        val addresses = task.taskItems.map {
            AddressWithColor(it.address, it.placemarkColor)
        }
        val newAddresses = newTaskItems
            .filter { it.taskId == task.id }
            .map {
                AddressWithColor(it.address, Color.rgb(0, 100, 0))
            }

        showedAddresses = addresses + newAddresses
        showedAddresses.forEach(::showAddress)
    }

    fun focusShowedAddresses() {

        makeFocus(showedAddresses.map { it.address })
    }

    suspend fun setTaskLayerLoading(task: TaskModel, loading: Boolean) = withContext(Dispatchers.Main) {
        val layer =
            adapter.data.firstOrNull { it is YandexMapModel.TaskLayer && it.task.id == task.id } ?: return@withContext
        (layer as YandexMapModel.TaskLayer).loading = loading
        adapter.notifyDataSetChanged()
    }

    fun showPredefinedAddresses() {
        clearMap()

        tasks
            .filter {
                !it.filtered
            }
            .flatMap {
                it.taskItems
            }.map {
                AddressWithColor(it.address, it.placemarkColor)
            }.forEach(::showAddress)

    }

    companion object {

        @JvmStatic
        fun newInstance(tasks: List<TaskModel>) =
            TasksYandexMapFragment().apply {
                arguments = Bundle().apply {
                    putIntegerArrayList(
                        "task_ids",
                        ArrayList(tasks.map { it.id })
                    )
                }
            }
    }
}