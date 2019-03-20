package ru.relabs.kurjercontroller.ui.fragments.entrancesList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_entrances_list.*
import ru.relabs.kurjer.ui.delegateAdapter.DelegateAdapter
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.ui.fragments.entrancesList.delegates.EntranceDelegate

/**
 * Created by ProOrange on 18.03.2019.
 */
class EntrancesListFragment : Fragment() {

    val presenter = EntrancesListPresenter(this)
    val adapter = DelegateAdapter<EntrancesListModel>().apply {
        addDelegate(EntranceDelegate { presenter.onEntranceClicked(it) })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        entrances_list?.layoutManager = LinearLayoutManager(context)
        entrances_list?.adapter = adapter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_entrances_list, container, false)
    }
}