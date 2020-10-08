package ru.relabs.kurjercontroller.ui.fragments.yandexMap

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.yandex.mapkit.map.VisibleRegion
import kotlinx.android.synthetic.main.fragment_yandex_map.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.models.AddressModel
import ru.relabs.kurjercontroller.domain.models.TaskItemModel
import ru.relabs.kurjercontroller.domain.models.TaskModel
import ru.relabs.kurjercontroller.utils.extensions.placemarkColor
import ru.relabs.kurjercontroller.utils.extensions.setVisible
import ru.relabs.kurjercontroller.ui.fragments.yandexMap.base.BaseYandexMapFragment
import ru.relabs.kurjercontroller.ui.fragments.yandexMap.base.WRONG_METHOD_OUTLINE_COLOR
import ru.relabs.kurjercontroller.ui.fragments.yandexMap.models.YandexMapModel

class TasksYandexMapFragment : BaseYandexMapFragment() {


    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return

            val updatedId = intent.getIntExtra("task_item_id_closed_by_deliveryman", -1)
            val updatedDateTime = intent.getLongExtra("task_item_date_closed_by_deliveryman", -1)
            if (updatedId > 0 && updatedDateTime > 0) {
                tasks.forEach {
                    it.taskItems.forEach {
                        if (it.id == updatedId) {
                            it.isNew = true
                            it.closeTime = DateTime(updatedDateTime * 1000)
                        }
                    }
                }

                presenter.bgScope.launch(Dispatchers.Main) {
                    presenter.updateCurrentLayer()
                }

                return
            }
        }
    }
    private val intentFilter = IntentFilter("NOW")


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
            val selectedLayer =
                getSelectedLayer() as? YandexMapModel.TaskLayer ?: return@setOnClickListener
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
            add_button?.text = resources.getString(
                R.string.yandex_map_add_button,
                selectedNewTaskItems.size.toString()
            )
        } else {
            add_button?.setVisible(false)
        }
    }

    fun isAddressInVisibleRegion(address: AddressModel, region: VisibleRegion): Boolean {
        return address.lat > region.bottomLeft.latitude && address.long < region.topRight.longitude &&
                address.long > region.bottomLeft.longitude && address.long < region.topRight.longitude
    }

    override fun onControlListPopulation() {
        if (tasks.size > 1) {
            adapter.data.add(YandexMapModel.CommonLayer)
            adapter.data.add(YandexMapModel.PredefinedAddressesLayer)
            tasks.filter { it.filtered }.forEach {
                adapter.data.add(YandexMapModel.TaskLayer(it))
            }
        } else {
            tasks.firstOrNull()?.let { showTask(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            taskIds = it.getIntegerArrayList("task_ids")?.toList() ?: listOf()
        }
        activity?.registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.unregisterReceiver(broadcastReceiver)
    }

    fun getSelectedLayer(): YandexMapModel? {
        if (tasks.size == 1) {
            return YandexMapModel.TaskLayer(tasks.first())
        }
        return adapter.data.firstOrNull { it.selected }
    }

    fun showAllTasks() {
        clearMap()

        val items = tasks
            .flatMap {
                it.taskItems
            }
        val addresses = items.map { it.address }.distinctBy { it.idnd }
        val coloredAddresses = items
            .groupBy {
                it.address.idnd
            }
            .map {
                AddressWithColor(addresses.first { addr -> addr.idnd == it.key }, Color.CYAN)
            }

        val newAddresses = newTaskItems.map { AddressWithColor(it.address, Color.rgb(0, 100, 0)) }

        showedAddresses = coloredAddresses + newAddresses
        showedAddresses.forEach(::showAddress)
    }

    fun showTask(task: TaskModel) {
        clearMap()
        val addresses = task.taskItems.map { it.address }.distinctBy { it.idnd }
        val coloredAddresses = task.taskItems
            .groupBy { it.address.idnd }
            .map {
                val placemarkColor = it.value.placemarkColor()
                AddressWithColor(
                    addresses.first { addr -> addr.idnd == it.key },
                    placemarkColor,
                    if (it.value.any {  it.wrongMethod && !it.isClosed  }) WRONG_METHOD_OUTLINE_COLOR else placemarkColor
                )
            }

        val newAddresses = newTaskItems
            .filter { it.taskId == task.id }
            .map {
                AddressWithColor(it.address, Color.rgb(0, 100, 0))
            }

        showedAddresses = coloredAddresses + newAddresses
        showedAddresses.forEach(::showAddress)

        task.storages.forEach {
            showStorage(it.lat.toDouble(), it.long.toDouble())
        }
    }

    fun focusShowedAddresses() {

        makeFocus(showedAddresses.map { it.address })
    }

    suspend fun setTaskLayerLoading(task: TaskModel, loading: Boolean) =
        withContext(Dispatchers.Main) {
            val layer =
                adapter.data.firstOrNull { it is YandexMapModel.TaskLayer && it.task.id == task.id }
                    ?: return@withContext
            (layer as YandexMapModel.TaskLayer).loading = loading
            adapter.notifyDataSetChanged()
        }

    fun showPredefinedAddresses() {
        clearMap()

        val items = tasks
            .filter {
                !it.filtered
            }
            .flatMap {
                it.taskItems
            }
        val addresses = items.map { it.address }.distinctBy { it.idnd }

        items.groupBy {
            it.address.idnd
        }.map {
            val placemarkColor = it.value.placemarkColor()
            AddressWithColor(
                addresses.first { addr -> addr.idnd == it.key },
                placemarkColor,
                if (it.value.any { it.wrongMethod && !it.isClosed }) WRONG_METHOD_OUTLINE_COLOR else placemarkColor
            )
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