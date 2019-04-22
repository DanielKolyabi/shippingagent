package ru.relabs.kurjercontroller.ui.fragments.report

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_report.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.relabs.kurjer.ui.delegateAdapter.DelegateAdapter
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.models.EntranceModel
import ru.relabs.kurjercontroller.models.TaskItemModel
import ru.relabs.kurjercontroller.models.TaskModel
import ru.relabs.kurjercontroller.ui.extensions.setSelectButtonActive
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

    var deliveryWrong: Boolean = false
    var hasLookup: Boolean = false
    var mailboxType: Int = 1
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
            arrayListOf("загрузка")
        )
        euroKeyAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            arrayListOf("нет")
        )

        //TODO: If apartments interval changed - refresh apartments list
        apartmentAdapter.addDelegate(
            ApartmentDelegate(
                { apartment, buttonGroup ->
                    presenter.onApartmentButtonGroupChanged(apartment, buttonGroup)
                },
                { apartment, newState ->
                    presenter.onApartmentButtonStateChanged(apartment, newState)
                }
            )
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
        if (entrance.state == EntranceModel.CLOSED) {
            setControlsLocked(true)
        } else {
            setControlsLocked(false)
        }
    }

    private fun setControlsLocked(locked: Boolean) {
        appartaments_from.isEnabled = !locked
        appartaments_to.isEnabled = !locked
        entrance_euro_key.isEnabled = !locked
        entrance_key.isEnabled = !locked
        floors.isEnabled = !locked
        entrance_code.isEnabled = !locked
        layout_error_button.isEnabled = !locked
        lookout.isEnabled = !locked
        close_button.isEnabled = !locked
        user_explanation_input.isEnabled = !locked
    }

    fun updateEditable() {
        val isEmpty = photosAdapter.data.none { it is ReportPhotosListModel.TaskItemPhoto }
//        floors.isEnabled = photosAdapter.data.isNotEmpty()
        appartaments_from.isEnabled = !isEmpty
        appartaments_to.isEnabled = !isEmpty
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!presenter.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun updateMailboxTypeText(){
        mailbox_type?.text = if(mailboxType == 1) {"Щель"} else{"Евро"}
    }

    private fun fillData() {
        appartaments_from?.setText(entrance.startApartments.toString())
        appartaments_to?.setText(entrance.endApartments.toString())
        entrance_code?.setText(entrance.code)

        mailboxType = entrance.mailboxType
        updateMailboxTypeText()

        entrance_key.adapter = keyAdapter
        entrance_euro_key.adapter = euroKeyAdapter

        appartaments_list.layoutManager = LinearLayoutManager(context)
        appartaments_list.adapter = apartmentAdapter

        photos_list.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        photos_list.isNestedScrollingEnabled = true
        photos_list.adapter = photosAdapter

        floors?.setText(entrance.floors.toString())

        presenter.bgScope.launch(Dispatchers.Main) {
            val saved = application().tasksRepository.loadEntranceResult(taskItem, entrance) ?: return@launch
            val availableKeys = application().tasksRepository.getAvailableEntranceKeys()

            keyAdapter?.clear()
            keyAdapter?.addAll(availableKeys)
            if (entrance.key.isNotBlank()) {
                val key = saved.key ?: entrance.key
                val pos = keyAdapter?.getPosition(key)
                if (pos != null && pos >= 0) {
                    entrance_key?.setSelection(pos)
                }
                withContext(Dispatchers.Main){
                    keyAdapter?.notifyDataSetChanged()
                }
            }


            if (saved.apartmentFrom != null) appartaments_from?.setText(saved.apartmentFrom.toString())
            if (saved.apartmentTo != null) appartaments_to?.setText(saved.apartmentTo.toString())
            if (saved.code != null) entrance_code?.setText(saved.code.toString())
            if (saved.description != null) user_explanation_input?.setText(saved.description)
            if (saved.floors != null) floors?.setText(saved.floors.toString())
            if (saved.hasLookupPost != null) {
                hasLookup = saved.hasLookupPost
                lookout?.setSelectButtonActive(saved.hasLookupPost)
            }
            if (saved.isDeliveryWrong != null) {
                deliveryWrong = saved.isDeliveryWrong
                layout_error_button?.setSelectButtonActive(saved.isDeliveryWrong)
            }
            if(saved.mailboxType != null){
                mailboxType = saved.mailboxType
                updateMailboxTypeText()
            }
        }

        fillPhotosList()
        fillApartmentList()
    }

    private fun fillPhotosList() {
        photosAdapter.data.add(ReportPhotosListModel.BlankMultiPhoto)
        photosAdapter.data.add(ReportPhotosListModel.BlankPhoto)
        presenter.bgScope.launch(Dispatchers.Main) {
            val photos = application().tasksRepository.loadEntrancePhotos(taskItem, entrance)
            photosAdapter.data.addAll(photos.map {
                ReportPhotosListModel.TaskItemPhoto(it)
            })
            photosAdapter.notifyItemRangeChanged(2, photos.size)
            updateEditable()
        }
        photosAdapter.notifyDataSetChanged()
    }

    private fun fillApartmentList() {
        //TODO: Merge if interval changed
        apartmentAdapter.data.clear()
        apartmentAdapter.data.addAll(
            (entrance.startApartments..entrance.endApartments).map {
                ApartmentListModel.Apartment(it, buttonGroup = 0, state = 0)
            }
        )
        presenter.bgScope.launch {
            val saves = application().tasksRepository.loadEntranceApartments(taskItem, entrance)
            saves.forEach { save ->
                val idx = apartmentAdapter.data.indexOfFirst {
                    (it as? ApartmentListModel.Apartment)?.number == save.number
                }
                if (idx < 0) return@forEach
                apartmentAdapter.data[idx] = save
            }
        }
        apartmentAdapter.notifyDataSetChanged()
    }

    private fun bindControl() {
        mailbox_type?.setOnClickListener {
            presenter.onEntranceMailboxTypeChanged()
        }

        close_button?.setOnClickListener {
            callback?.onEntranceClosed(task, taskItem, entrance)
        }

        entrance_key?.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                keyAdapter?.getItem(pos)?.let{
                    presenter.onEntranceKeyChanged(it)
                }
            }
        }

        val listClickInterceptor = object : RecyclerView.OnItemTouchListener {
            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean =
                entrance.state == EntranceModel.CLOSED
        }

        photos_list.addOnItemTouchListener(listClickInterceptor)
        appartaments_list.addOnItemTouchListener(listClickInterceptor)

        user_explanation_input?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                presenter.onDescriptionChanged()
            }
        })
        entrance_code?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                presenter.onCodeChanged()
            }
        })
        appartaments_from?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                presenter.onApartmentIntervalChanged()
            }
        })
        appartaments_to?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                presenter.onApartmentIntervalChanged()
            }
        })
        floors?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                presenter.onFloorsChanged()
            }
        })
        layout_error_button?.setOnClickListener {
            presenter.onDeliveryWrongChanged()
        }
        lookout?.setOnClickListener {
            presenter.onLookupChanged()
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