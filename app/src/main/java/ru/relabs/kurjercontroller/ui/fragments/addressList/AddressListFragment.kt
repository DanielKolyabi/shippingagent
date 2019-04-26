package ru.relabs.kurjercontroller.ui.fragments.addressList

import android.content.Context
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.relabs.kurjer.ui.delegateAdapter.DelegateAdapter
import ru.relabs.kurjercontroller.BuildConfig
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.models.TaskModel
import ru.relabs.kurjercontroller.ui.extensions.setVisible
import ru.relabs.kurjercontroller.ui.fragments.ISearchableFragment
import ru.relabs.kurjercontroller.ui.fragments.addressList.delegates.AddressListAddressDelegate
import ru.relabs.kurjercontroller.ui.fragments.addressList.delegates.AddressListLoaderDelegate
import ru.relabs.kurjercontroller.ui.fragments.addressList.delegates.AddressListSortingDelegate
import ru.relabs.kurjercontroller.ui.fragments.addressList.delegates.AddressListTaskItemDelegate
import ru.relabs.kurjercontroller.ui.helpers.HintHelper

/**
 * Created by ProOrange on 18.03.2019.
 */
class AddressListFragment : Fragment(), ISearchableFragment {
    override fun onSearchItems(filter: String): List<String> {
        if (filter.contains(",")) {
            return adapter.data.asSequence()
                .filter { it is AddressListModel.Address }
                .filter {
                    (it as AddressListModel.Address).taskItems.first().address.name.contains(filter, true)
                }
                .map {
                    (it as AddressListModel.Address).taskItems.first().address.name
                }
                .toList()
        } else {
            return adapter.data.asSequence()
                .filter { it is AddressListModel.Address }
                .filter {
                    (it as AddressListModel.Address).taskItems.first().address.street.contains(filter, true)
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
            (it as? AddressListModel.Address)?.taskItems?.first()?.address?.name?.contains(item, true)
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
            activity!!.getSharedPreferences(
                BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE
            )
        )

        address_list?.layoutManager = LinearLayoutManager(context)
        address_list?.adapter = adapter

        map_button?.setOnClickListener {
            presenter.onMapClicked()
        }
        close_button?.setOnClickListener {
            presenter.onCloseTaskClicked()
        }
        updateCloseTaskButtonVisibility()

        if (adapter.data.isEmpty()) {
            presenter.preloadTasks()
        } else {
            presenter.bgScope.launch {
                presenter.applySorting()
                presenter.checkTasks()
            }
        }
    }


    fun updateCloseTaskButtonVisibility() {
        close_button.setVisible(tasks.size == 1)
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
    }

    override fun onDestroy() {
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
