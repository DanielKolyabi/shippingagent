package ru.relabs.kurjercontroller.ui.fragments.report

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_report.*
import ru.relabs.kurjer.ui.delegateAdapter.DelegateAdapter
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.models.EntranceModel
import ru.relabs.kurjercontroller.models.TaskItemModel
import ru.relabs.kurjercontroller.models.TaskModel
import ru.relabs.kurjercontroller.ui.fragments.report.delegates.ApartmentDelegate
import ru.relabs.kurjercontroller.ui.fragments.report.delegates.ReportBlankMultiPhotoDelegate
import ru.relabs.kurjercontroller.ui.fragments.report.delegates.ReportBlankPhotoDelegate
import ru.relabs.kurjercontroller.ui.fragments.report.delegates.ReportPhotoDelegate
import ru.relabs.kurjercontroller.ui.fragments.report.models.ApartmentListModel
import ru.relabs.kurjercontroller.ui.fragments.report.models.ReportPhotosListModel

/**
 * Created by ProOrange on 15.04.2019.
 */
class ReportFragment : Fragment() {

    var apartmentAdapter = DelegateAdapter<ApartmentListModel>()
    val photosAdapter = DelegateAdapter<ReportPhotosListModel>()

    var keyAdapter: ArrayAdapter<String>? = null
    var euroKeyAdapter: ArrayAdapter<String>? = null
    var callback: Callback? = null
    val presenter = ReportPresenter(this)
    lateinit var task: TaskModel
    lateinit var taskItem: TaskItemModel
    lateinit var entrance: EntranceModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        keyAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            entrance.availableKeys.ifEmpty { listOf("нет") })
        euroKeyAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            entrance.availableEuroKeys.ifEmpty { listOf("нет") })

        //TODO: If apartments interval changed - refresh apartments list
        apartmentAdapter.addDelegate(
            ApartmentDelegate { apartment, buttonGroup ->
                presenter.onApartmentButtonGroupChanged(apartment, buttonGroup)
            }
        )
        photosAdapter.addDelegate(ReportPhotoDelegate { holder ->
            presenter.onRemovePhotoClicked(holder)
        })
        photosAdapter.addDelegate(ReportBlankPhotoDelegate { holder ->
            presenter.onBlankPhotoClicked()
        })
        photosAdapter.addDelegate(ReportBlankMultiPhotoDelegate { holder ->
            presenter.onBlankMultiPhotoClicked()
        })

        fillData()
        bindControl()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!presenter.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun fillData() {
        appartaments_from?.setText(entrance.startApartments.toString())
        appartaments_to?.setText(entrance.endApartments.toString())
        entrance_code?.setText(entrance.code)
        entrance_key.adapter = keyAdapter
        entrance_euro_key.adapter = euroKeyAdapter

        appartaments_list.layoutManager = LinearLayoutManager(context)
        appartaments_list.adapter = apartmentAdapter

        photos_list.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        photos_list.isNestedScrollingEnabled = true
        photos_list.adapter = photosAdapter

        fillPhotosList()
        fillApartmentList()
    }

    private fun fillPhotosList() {
        photosAdapter.data.add(ReportPhotosListModel.BlankMultiPhoto)
        photosAdapter.data.add(ReportPhotosListModel.BlankPhoto)
        //TODO: Load saved photos
        photosAdapter.notifyDataSetChanged()
    }

    private fun fillApartmentList() {
        //TODO: Merge if interval changed
        apartmentAdapter.data.clear()
        apartmentAdapter.data.addAll(
            (entrance.startApartments..entrance.endApartments).map {
                ApartmentListModel.Apartment(it, buttonGroup = 0)
            }
        )
        apartmentAdapter.notifyDataSetChanged()
    }

    private fun bindControl() {
        close_button?.setOnClickListener {
            callback?.onEntranceClosed(task, taskItem, entrance)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_report, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            task = it.getParcelable("task")
            taskItem = it.getParcelable("task_item")
            entrance = it.getParcelable("entrance")
        }
    }

    override fun onDestroy() {
        presenter.bgScope.terminate()
        super.onDestroy()
    }

    companion object {
        @JvmStatic
        fun newInstance(task: TaskModel, taskItem: TaskItemModel, entrance: EntranceModel) =
            ReportFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("task", task)
                    putParcelable("task_item", taskItem)
                    putParcelable("entrance", entrance)
                }
            }
    }

    interface Callback {
        fun onEntranceClosed(task: TaskModel, taskItem: TaskItemModel, entrance: EntranceModel)
    }
}