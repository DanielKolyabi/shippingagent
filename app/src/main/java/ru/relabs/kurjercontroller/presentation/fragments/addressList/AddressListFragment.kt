package ru.relabs.kurjercontroller.presentation.fragments.addressList

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_address_list.*
import kotlinx.android.synthetic.main.fragment_tasklist.*
import kotlinx.android.synthetic.main.include_hint_container.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.presentation.delegateAdapter.DelegateAdapter
import ru.relabs.kurjercontroller.BuildConfig
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.models.Address
import ru.relabs.kurjercontroller.domain.models.Entrance
import ru.relabs.kurjercontroller.domain.models.TaskModel
import ru.relabs.kurjercontroller.presentation.activities.ErrorButtonsListener
import ru.relabs.kurjercontroller.presentation.activities.showError
import ru.relabs.kurjercontroller.utils.extensions.setVisible
import ru.relabs.kurjercontroller.presentation.fragments.ISearchableFragment
import ru.relabs.kurjercontroller.presentation.fragments.addressList.delegates.AddressListAddressDelegate
import ru.relabs.kurjercontroller.presentation.fragments.addressList.delegates.AddressListLoaderDelegate
import ru.relabs.kurjercontroller.presentation.fragments.addressList.delegates.AddressListSortingDelegate
import ru.relabs.kurjercontroller.presentation.fragments.addressList.delegates.AddressListTaskItemDelegate
import ru.relabs.kurjercontroller.presentation.fragments.addressList.holders.AddressListAddressHolder
import ru.relabs.kurjercontroller.presentation.helpers.HintHelper

/**
 * Created by ProOrange on 18.03.2019.
 */
class AddressListFragment : Fragment(), ISearchableFragment {
    var targetAddress: Address? = null
    var shouldReloadData: Boolean = false

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

                presenter.bgScope.launch {
                    presenter.applySorting()
                }

                return
            }

            val taskId = intent.getIntExtra("task_closed", 0)
            val taskItemId = intent.getIntExtra("task_item_closed", 0)
            val entranceNumber = intent.getIntExtra("entrance_number_closed", 0)

            for (task in tasks) {
                if (task.id == taskId) {
                    for (taskItem in task.taskItems) {
                        if (taskItem.id == taskItemId) {
                            for (entrance in taskItem.entrances) {
                                if (entrance.number == entranceNumber) {
                                    entrance.state = Entrance.CLOSED
                                }
                            }
                        }
                    }
                }
            }

            presenter.bgScope.launch {
                presenter.applySorting()
            }
        }
    }
    private val intentFilter = IntentFilter("NOW")

    override fun onSearchItems(filter: String): List<String> {
        if (filter.contains(",")) {
            return adapter.data.asSequence()
                .filter { it is AddressListModel.Address }
                .filter {
                    (it as AddressListModel.Address).taskItems.first().address.name.contains(
                        filter,
                        true
                    )
                }
                .map {
                    (it as AddressListModel.Address).taskItems.first().address.name
                }
                .toList()
        } else {
            return adapter.data.asSequence()
                .filter { it is AddressListModel.Address }
                .filter {
                    (it as AddressListModel.Address).taskItems.first().address.street.contains(
                        filter,
                        true
                    )
                }
                .map {
                    (it as AddressListModel.Address).taskItems.first().address.street + ","
                }
                .distinct()
                .toList()
        }
    }

    override fun onItemSelected(item: String, searchView: AutoCompleteTextView) {

        val itemIndex = adapter.data.indexOfFirst {
            (it as? AddressListModel.Address)?.taskItems?.first()?.address?.name?.contains(
                item,
                true
            )
                ?: false
        }
        if (itemIndex < 0) {
            return
        }
        address_list?.smoothScrollToPosition(itemIndex)
    }


    private lateinit var hintHelper: HintHelper
    var taskIds: List<Int> = listOf()
    val tasks: MutableList<TaskModel> = mutableListOf()

    val presenter = AddressListPresenter(this)
    val adapter = DelegateAdapter<AddressListModel>().apply {
        addDelegate(AddressListAddressDelegate { presenter.onAddressMapClicked(it) })
        addDelegate(AddressListLoaderDelegate())
        addDelegate(AddressListSortingDelegate { presenter.onSortingChanged(it) })
        addDelegate(AddressListTaskItemDelegate { presenter.onTaskItemClicked(it) })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hintHelper = HintHelper(
            hint_container,
            resources.getString(R.string.address_list_hint_text),
            false,
            activity?.getSharedPreferences(
                BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE
            )
        )

        address_list?.layoutManager = LinearLayoutManager(context)
        address_list?.adapter = adapter

        map_button?.setOnClickListener {
            presenter.onMapClicked()
        }
        close_button?.setOnClickListener {
            context?.showError(
                "Вы действительно хотите закрыть задание?",
                object : ErrorButtonsListener {
                    override fun positiveListener() {
                        presenter.onCloseTaskClicked()
                    }
                },
                "Да",
                "Нет",
                true
            )
        }
        updateCloseTaskButtonVisibility()

        if (adapter.data.isEmpty() || shouldReloadData) {
            shouldReloadData = false
            presenter.preloadTasks()
        } else {
            presenter.bgScope.launch {
                presenter.preloadTasks(silent = true)
                presenter.checkTasks()
            }
//            presenter.bgScope.launch {
//                presenter.applySorting()
//                presenter.checkTasks()
//            }
        }

        targetAddress?.let {
            //HACK. List won't scroll without timeout ¯\_(ツ)_/¯
            presenter.bgScope.launch {
                delay(250)
                withContext(Dispatchers.Main) {
                    scrollToAddress(it)
                    targetAddress = null
                }
            }
        }
    }


    private fun scrollToAddress(address: Address) {
        val idx = adapter.data.indexOfFirst {
            (it as? AddressListModel.Address)?.taskItems?.firstOrNull()?.address?.idnd == address.idnd
        }
        if (idx < 0) {
            return
        }

        address_list?.scrollToPosition(idx)

        presenter.bgScope.launch {
            delay(500)
            address_list?.post {
                val holder =
                    address_list?.findViewHolderForAdapterPosition(idx) as? AddressListAddressHolder
                holder?.flashSelectedColor()
            }
        }
    }

    fun updateCloseTaskButtonVisibility() {
        close_button?.setVisible(tasks.size == 1)
    }

    suspend fun showLoading(visible: Boolean) = withContext(Dispatchers.Main) {
        if (visible) {
            adapter.data.add(0, AddressListModel.Loader)
            tasks_list?.isNestedScrollingEnabled = false
            adapter.notifyDataSetChanged()
        } else {
            adapter.data.removeAll {
                it is AddressListModel.Loader
            }
            tasks_list?.isNestedScrollingEnabled = false
            adapter.notifyDataSetChanged()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_address_list, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            taskIds = it.getIntArray("task_ids")?.toList() ?: listOf()
        }
        activity?.registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onDestroy() {
        activity?.unregisterReceiver(broadcastReceiver)
        presenter.bgScope.terminate()
        super.onDestroy()
    }

    companion object {
        @JvmStatic
        fun newInstance(taskIds: List<Int>) =
            AddressListFragment().apply {
                arguments = Bundle().apply {
                    putIntArray("task_ids", taskIds.toIntArray())
                }
            }
    }
}
