package ru.relabs.kurjercontroller.ui.fragments.taskInfo


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_taskinfo.*
import ru.relabs.kurjer.ui.delegateAdapter.DelegateAdapter
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.ui.fragments.taskInfo.delegates.InfoHeaderDelegate
import ru.relabs.kurjercontroller.ui.fragments.taskInfo.delegates.InfoInfoDelegate
import ru.relabs.kurjercontroller.ui.fragments.taskInfo.delegates.InfoItemDelegate

class TaskInfoFragment : Fragment() {

    val presenter = TaskInfoPresenter(this)
    val adapter = DelegateAdapter<TaskInfoModel>().apply {
        addDelegate(InfoHeaderDelegate())
        addDelegate(InfoInfoDelegate())
        addDelegate(InfoItemDelegate { presenter.onInfoClicked(it) })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        task_items_list?.layoutManager = LinearLayoutManager(context)
        task_items_list?.adapter = adapter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_taskinfo, container, false)
    }
}
