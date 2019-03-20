package ru.relabs.kurjercontroller.ui.fragments.addressList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_address_list.*
import ru.relabs.kurjer.ui.delegateAdapter.DelegateAdapter
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.ui.fragments.addressList.delegates.AddressListAddressDelegate
import ru.relabs.kurjercontroller.ui.fragments.addressList.delegates.AddressListLoaderDelegate
import ru.relabs.kurjercontroller.ui.fragments.addressList.delegates.AddressListSortingDelegate
import ru.relabs.kurjercontroller.ui.fragments.addressList.delegates.AddressListTaskItemDelegate

/**
 * Created by ProOrange on 18.03.2019.
 */
class AddressListFragment : Fragment() {

    val presenter = AddressListPresenter(this)
    val adapter = DelegateAdapter<AddressListModel>().apply {
        addDelegate(AddressListAddressDelegate { presenter.onAddressMapClicked(it) })
        addDelegate(AddressListLoaderDelegate())
        addDelegate(AddressListSortingDelegate { presenter.onSortingChanged(it) })
        addDelegate(AddressListTaskItemDelegate { presenter.onTaskItemClicked(it) })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        address_list?.layoutManager = LinearLayoutManager(context)
        address_list?.adapter = adapter

        close_button?.setOnClickListener {
            presenter.onCloseTaskClicked()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_address_list, container, false)
    }
}
