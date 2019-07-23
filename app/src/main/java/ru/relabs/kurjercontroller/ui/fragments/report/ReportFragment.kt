package ru.relabs.kurjercontroller.ui.fragments.report

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_report.*
import kotlinx.android.synthetic.main.include_hint_container.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.relabs.kurjer.ui.delegateAdapter.DelegateAdapter
import ru.relabs.kurjercontroller.BuildConfig
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.database.entities.EntranceResultEntity
import ru.relabs.kurjercontroller.models.EntranceModel
import ru.relabs.kurjercontroller.models.TaskItemModel
import ru.relabs.kurjercontroller.models.TaskModel
import ru.relabs.kurjercontroller.ui.activities.ErrorButtonsListener
import ru.relabs.kurjercontroller.ui.activities.showError
import ru.relabs.kurjercontroller.ui.extensions.setSelectButtonActive
import ru.relabs.kurjercontroller.ui.fragments.report.delegates.*
import ru.relabs.kurjercontroller.ui.fragments.report.models.ApartmentListModel
import ru.relabs.kurjercontroller.ui.fragments.report.models.ReportPhotosListModel
import ru.relabs.kurjercontroller.ui.helpers.HintHelper

/**
 * Created by ProOrange on 15.04.2019.
 */
class ReportFragment : Fragment() {

    var deliveryWrong: Boolean = false
    var hasLookout: Boolean = false
    var entranceClosed: Boolean = false
    var mailboxType: Int = 1
    var apartmentAdapter = DelegateAdapter<ApartmentListModel>()
    val photosAdapter = DelegateAdapter<ReportPhotosListModel>()

    private lateinit var hintHelper: HintHelper
    var keyAdapter: ArrayAdapter<String>? = null
    var euroKeyAdapter: ArrayAdapter<String>? = null
    var callback: Callback? = null
    val presenter = ReportPresenter(this)
    lateinit var task: TaskModel
    lateinit var taskItem: TaskItemModel
    lateinit var entrance: EntranceModel
    var allTaskItems: List<TaskItemModel> = listOf()
    var saved: EntranceResultEntity? = null

    var defaultButtonBackground: Drawable? = null

    fun updateHintHelperMaximumHeight() {

        hintHelper?.maxHeight = (appartaments_list?.height ?: 0) + (hint_container?.height ?: 0)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        defaultButtonBackground = mailbox_euro?.background

        hintHelper = HintHelper(
            hint_container,
            "",
            false,
            activity?.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
        )
        showHintText(taskItem.notes)
        updateApartmentListBackground(0)

        hint_container.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {

            override fun onGlobalLayout() {
                hint_container?.viewTreeObserver?.removeOnGlobalLayoutListener(this)

                updateHintHelperMaximumHeight()
            }
        })

        allTaskItems = callback?.getAllTaskItems() ?: listOf(taskItem)

        keyAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            arrayListOf("загрузка")
        )
        euroKeyAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            arrayListOf("загрузка")
        )

        bindDelegates()
        bindControl()
        refreshData()

        if (entrance.state == EntranceModel.CLOSED) {
            setControlsLocked(true)
        } else {
            setControlsLocked(false)
        }
    }

    private fun isClosed() = entrance.state == EntranceModel.CLOSED || entranceClosed
    private fun isPhotoAvailable() = entrance.state == EntranceModel.CREATED

    private fun bindDelegates() {

        apartmentAdapter.addDelegate(
            ApartmentMainDelegate(
                { apartment, newState ->
                    if (isClosed()) return@ApartmentMainDelegate
                    presenter.onApartmentButtonStateChanged(apartment, newState)
                },
                { apartment, change ->
                    if (isClosed()) return@ApartmentMainDelegate
                    presenter.onAllApartmentsButtonStateChanged(apartment, change)
                },
                { apartment ->
                    if (isClosed()) return@ApartmentMainDelegate
                    presenter.onApartmentDescriptionClicked(apartment)
                }
            )
        )
        apartmentAdapter.addDelegate(
            ApartmentAdditionDelegate(
                { apartment, newState ->
                    if (isClosed()) return@ApartmentAdditionDelegate
                    presenter.onApartmentButtonStateChanged(apartment, newState)
                },
                { apartment, change ->
                    if (isClosed()) return@ApartmentAdditionDelegate
                    presenter.onAllApartmentsButtonStateChanged(apartment, change)
                },
                { apartment ->
                    if (isClosed()) return@ApartmentAdditionDelegate
                    presenter.onApartmentDescriptionClicked(apartment)
                }
            )
        )
        apartmentAdapter.addDelegate(
            EntranceDelegate { newState ->
                if (isClosed()) return@EntranceDelegate
                presenter.onEntranceButtonStateChanged(newState)
            }
        )
        apartmentAdapter.addDelegate(
            LookoutDelegate { newState ->
                if (isClosed()) return@LookoutDelegate
                presenter.onLookoutButtonStateChanged(newState)
            }
        )
        photosAdapter.addDelegate(ReportPhotoDelegate { holder ->
            if (!isPhotoAvailable()) return@ReportPhotoDelegate
            presenter.onRemovePhotoClicked(holder)
        })
        photosAdapter.addDelegate(ReportBlankPhotoDelegate {
            if (!isPhotoAvailable()) return@ReportBlankPhotoDelegate
            presenter.onBlankPhotoClicked()
        })
        photosAdapter.addDelegate(ReportBlankMultiPhotoDelegate {
            if (!isPhotoAvailable()) return@ReportBlankMultiPhotoDelegate
            presenter.onBlankMultiPhotoClicked()
        })
    }

    fun showHintText(notes: List<String>) {
        val text = Html.fromHtml("<b>Код: ${entrance.code}</b><br/>" + (3 downTo 1).map {
            "<b>Пр. $it</b><br/>" + notes.getOrElse(it - 1) { "" }
        }.joinToString("<br/>"))
        hint_text.text = text
    }

    private fun refreshData() {
        presenter.bgScope.launch {
            saved = application().tasksRepository.loadEntranceResult(taskItem, entrance)
            fillData()
        }
    }

    fun onChanged(entrance: EntranceModel) {
        if (entrance.number != this.entrance.number) {
            return
        }
        refreshData()
    }

    fun setControlsLockedDueEntranceClosed(locked: Boolean){
        entrance_euro_key?.isEnabled = !locked
        entrance_key?.isEnabled = !locked
        layout_error_button?.isEnabled = !locked
        lookout?.isEnabled = !locked
        close_button?.isEnabled = !locked
        mailbox_euro?.isEnabled = !locked
        mailbox_gap?.isEnabled = !locked

        updateCloseButtonActive()
        updateIsEntranceClosedButton()
    }

    fun setControlsLocked(locked: Boolean) {
        appartaments_from?.isEnabled = !locked
        appartaments_to?.isEnabled = !locked
        entrance_euro_key?.isEnabled = !locked
        entrance_key?.isEnabled = !locked
        floors?.isEnabled = !locked
        entrance_code?.isEnabled = !locked
        layout_error_button?.isEnabled = !locked
        lookout?.isEnabled = !locked
        close_button?.isEnabled = !locked
        user_explanation_input?.isEnabled = !locked
        mailbox_euro?.isEnabled = !locked
        mailbox_gap?.isEnabled = !locked

        updateCloseButtonActive()
        updateIsEntranceClosedButton()
    }

    fun updateIsEntranceClosedButton() {
        entrance_closed?.isEnabled = entrance.state == EntranceModel.CREATED
    }

    fun updateCloseButtonActive() {
        close_button?.isEnabled = entrance.state == EntranceModel.CREATED
    }

    fun updateEditable() {
        val isEmpty = photosAdapter.data.none { it is ReportPhotosListModel.TaskItemPhoto }
        appartaments_from?.isEnabled = !isEmpty
        appartaments_to?.isEnabled = !isEmpty
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!presenter.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun updateEuroKeysLocked() {
        entrance_euro_key.isEnabled = mailboxType == 1
    }

    fun updateMailboxTypeText() {
        updateEuroKeysLocked()
        mailbox_euro?.setSelectButtonActive(mailboxType == 1)
        mailbox_gap?.setSelectButtonActive(mailboxType == 2)
    }

    private suspend fun fillData() = withContext(Dispatchers.Main) {
        appartaments_from?.setText(entrance.startApartments.toString())
        appartaments_to?.setText(entrance.endApartments.toString())

        mailboxType = entrance.mailboxType
        updateMailboxTypeText()

        entrance_key?.adapter = keyAdapter
        entrance_euro_key?.adapter = euroKeyAdapter

        appartaments_list?.layoutManager = LinearLayoutManager(context)
        appartaments_list?.adapter = apartmentAdapter

        photos_list?.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        photos_list?.isNestedScrollingEnabled = true
        photos_list?.adapter = photosAdapter

        floors?.setText(entrance.floors.toString())

        if (entrance.hasLookout) {
            hasLookout = entrance.hasLookout
        }

        presenter.bgScope.launch(Dispatchers.Main) {
            loadKeys(saved)
        }

        saved?.let { saved ->
            if (saved.apartmentFrom != null) appartaments_from?.setText(saved.apartmentFrom.toString())
            if (saved.apartmentTo != null) appartaments_to?.setText(saved.apartmentTo.toString())
            if (saved.code != null) entrance_code?.setText(saved.code.toString())
            if (saved.description != null) user_explanation_input?.setText(saved.description)
            if (saved.floors != null) floors?.setText(saved.floors.toString())
            if (saved.hasLookupPost != null) {
                hasLookout = saved.hasLookupPost
                lookout?.setSelectButtonActive(saved.hasLookupPost)
            }
            if (saved.isDeliveryWrong != null) {
                deliveryWrong = saved.isDeliveryWrong
                layout_error_button?.setSelectButtonActive(saved.isDeliveryWrong)
            }
            if (saved.mailboxType != null) {
                mailboxType = saved.mailboxType
                updateMailboxTypeText()
            }
            if (saved.entranceClosed != null) {
                entranceClosed = saved.entranceClosed
                updateEntranceClosed()
            }
        }

        fillPhotosList()
        fillApartmentList()
    }

    private suspend fun loadKeys(saved: EntranceResultEntity?) {
        val availableKeys = application().tasksRepository.getAvailableEntranceKeys()
        val availableEuroKeys = application().tasksRepository.getAvailableEntranceEuroKeys()

        keyAdapter?.clear()
        keyAdapter?.addAll(availableKeys)

        euroKeyAdapter?.clear()
        euroKeyAdapter?.addAll(availableEuroKeys)

        if (entrance.key.isNotBlank() || saved?.key?.isNotBlank() == true) {
            val key = saved?.key ?: entrance.key
            val pos = keyAdapter?.getPosition(key)
            if (pos != null && pos >= 0) {
                entrance_key?.setSelection(pos)
            }
        } else {
            val pos = keyAdapter?.getPosition("Нет")
            if (pos != null && pos >= 0) {
                entrance_key?.setSelection(pos)
            }
        }

        if (entrance.euroKey.isNotBlank() || saved?.euroKey?.isNotBlank() == true) {
            val key = saved?.euroKey ?: entrance.euroKey
            val pos = euroKeyAdapter?.getPosition(key)
            if (pos != null && pos >= 0) {
                entrance_euro_key?.setSelection(pos)
            }
        } else {
            val pos = keyAdapter?.getPosition("Нет")
            if (pos != null && pos >= 0) {
                entrance_key?.setSelection(pos)
            }
        }

        presenter.onEntranceKeyChanged(entrance_key.selectedItem?.toString() ?: "ошибка")
        presenter.onEntranceEuroKeyChanged(entrance_euro_key.selectedItem?.toString() ?: "ошибка")

        withContext(Dispatchers.Main) {
            keyAdapter?.notifyDataSetChanged()
            euroKeyAdapter?.notifyDataSetChanged()
        }
    }

    private fun fillPhotosList() {
        photosAdapter.data.clear()
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

    fun apartmentListAddEntrance() =
        presenter.bgScope.launch(Dispatchers.IO) {
            val saved = application().tasksRepository.loadEntranceApartment(taskItem, entrance, -1)

            apartmentAdapter.data.removeAll {
                it is ApartmentListModel.Entrance
            }
            apartmentAdapter.data.add(0, ApartmentListModel.Entrance(saved?.state ?: 0))
        }


    fun apartmentListRemoveEntrance() {
        apartmentAdapter.data.removeAll {
            it is ApartmentListModel.Entrance
        }
    }

    fun apartmentListAddLookout() =
        presenter.bgScope.launch(Dispatchers.IO) {
            val saved = application().tasksRepository.loadEntranceApartment(taskItem, entrance, -2)

            apartmentAdapter.data.removeAll {
                it is ApartmentListModel.Lookout
            }
            apartmentAdapter.data.add(0, ApartmentListModel.Lookout(saved?.state ?: 0))
        }

    fun apartmentListRemoveLookout() {
        apartmentAdapter.data.removeAll {
            it is ApartmentListModel.Lookout
        }
    }

    fun fillApartmentList() {
        apartmentAdapter.data.clear()
        apartmentAdapter.data.addAll(
            ((saved?.apartmentFrom ?: entrance.startApartments)..(saved?.apartmentTo ?: entrance.endApartments)).map {
                ApartmentListModel.Apartment(it, buttonGroup = taskItem.defaultReportType, state = 0)
            }
        )
        apartmentAdapter.notifyDataSetChanged()
        presenter.bgScope.launch {
            val saves = application().tasksRepository.loadEntranceApartments(taskItem, entrance)
            saves.forEach { save ->
                val idx = apartmentAdapter.data.indexOfFirst {
                    (it as? ApartmentListModel.Apartment)?.number == save.number
                }
                if (idx < 0) return@forEach
                apartmentAdapter.data[idx] = save
            }

            val buttonGroup =
                (apartmentAdapter.data.firstOrNull { it is ApartmentListModel.Apartment } as? ApartmentListModel.Apartment)?.buttonGroup
            if (buttonGroup == 0) {
                apartmentListRemoveEntrance()

                if (hasLookout) {
                    apartmentListAddLookout()
                } else {
                    apartmentListRemoveLookout()
                }
            } else {
                apartmentListAddEntrance()
                if (hasLookout) {
                    apartmentListAddLookout()
                } else {
                    apartmentListRemoveLookout()
                }
            }

            appartaments_list?.post {

                apartmentAdapter.notifyDataSetChanged()
                updateApartmentListTypeButton()
            }
        }
    }

    private fun bindControl() {
        entrance_closed?.setOnClickListener {
            presenter.onIsEntranceClosedChanged()
        }

        mailbox_euro?.setOnClickListener {
            presenter.onEntranceMailboxTypeChanged()
        }
        mailbox_gap?.setOnClickListener {
            presenter.onEntranceMailboxTypeChanged()
        }
        list_type_button?.setOnClickListener {
            presenter.onApartmentButtonGroupChanged()
        }
        close_button?.setOnClickListener {
            context?.showError("Вы действительно хотите закрыть подъезд?", object : ErrorButtonsListener {
                override fun positiveListener() {
                    callback?.onEntranceClosed(task, taskItem, entrance)
                }
            }, "Да", "Нет", true)
        }

        entrance_key?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                keyAdapter?.getItem(pos)?.let {
                    presenter.onEntranceKeyChanged(it)
                }
            }
        }

        entrance_euro_key?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                euroKeyAdapter?.getItem(pos)?.let {
                    presenter.onEntranceEuroKeyChanged(it)
                }
            }
        }

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

        appartaments_from?.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_NEXT ||
                event != null &&
                event.action == KeyEvent.ACTION_DOWN &&
                event.keyCode == KeyEvent.KEYCODE_ENTER
            ) {

                presenter.onApartmentIntervalChanged()
                (context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(
                    appartaments_from?.windowToken,
                    0
                )
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        appartaments_from?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                appartaments_from?.setText((saved?.apartmentFrom ?: entrance.startApartments).toString())
            }else{
                hintHelper.setHintExpanded(false)
            }
        }

        appartaments_to?.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_NEXT ||
                event != null &&
                event.action == KeyEvent.ACTION_DOWN &&
                event.keyCode == KeyEvent.KEYCODE_ENTER
            ) {

                presenter.onApartmentIntervalChanged()
                (context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(
                    appartaments_to?.windowToken,
                    0
                )
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        appartaments_to?.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                appartaments_to?.setText((saved?.apartmentTo ?: entrance.endApartments).toString())
            }else{
                hintHelper.setHintExpanded(false)
            }
        }

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
            presenter.onLookoutChanged()
        }

        appartaments_to?.setOnClickListener {
            hintHelper.setHintExpanded(false)
        }
        appartaments_from?.setOnClickListener {
            hintHelper.setHintExpanded(false)
        }
        floors?.setOnClickListener {
            hintHelper.setHintExpanded(false)
        }
        floors?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                hintHelper.setHintExpanded(false)
            }
        }

        entrance_code?.setOnClickListener {
            hintHelper.setHintExpanded(false)
        }
        entrance_code?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                hintHelper.setHintExpanded(false)
            }
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

    fun updateEntranceClosed() {
        entrance_closed?.setSelectButtonActive(entranceClosed)
        setControlsLockedDueEntranceClosed(entranceClosed)
    }

    fun updateApartmentListBackground(buttonGroup: Int) {
        list_background.setImageDrawable(
            if (buttonGroup == 0) resources.getDrawable(
                R.drawable.apartment_list_main_bg,
                null
            ) else null
        )
    }

    fun updateApartmentListTypeButton() {
        val app = apartmentAdapter.data.firstOrNull{ it is ApartmentListModel.Apartment } as? ApartmentListModel.Apartment
        app ?: return
        if(app.buttonGroup == 0){
            list_type_button.text = "Опрос"
            list_type_button.setSelectButtonActive(false)
        }else{
            list_type_button.text = "БезОп"
            list_type_button.setSelectButtonActive(true)
        }
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
        fun getAllTaskItems(): List<TaskItemModel>
        fun onEntranceChanged(entrance: EntranceModel)
    }
}