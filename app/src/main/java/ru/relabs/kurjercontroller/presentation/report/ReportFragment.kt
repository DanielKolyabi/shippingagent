package ru.relabs.kurjercontroller.presentation.report

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.fragment_report.view.*
import kotlinx.android.synthetic.main.include_hint_container.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.models.ApartmentNumber
import ru.relabs.kurjercontroller.domain.models.Entrance
import ru.relabs.kurjercontroller.domain.models.EntranceNumber
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.presentation.base.TextChangeListener
import ru.relabs.kurjercontroller.presentation.base.fragment.BaseFragment
import ru.relabs.kurjercontroller.presentation.base.recycler.DelegateAdapter
import ru.relabs.kurjercontroller.presentation.base.tea.debugCollector
import ru.relabs.kurjercontroller.presentation.base.tea.defaultController
import ru.relabs.kurjercontroller.presentation.base.tea.rendersCollector
import ru.relabs.kurjercontroller.presentation.base.tea.sendMessage
import ru.relabs.kurjercontroller.presentation.helpers.HintHelper
import ru.relabs.kurjercontroller.presentation.reportPager.TaskItemWithTaskIds
import ru.relabs.kurjercontroller.utils.CustomLog
import ru.relabs.kurjercontroller.utils.debug
import ru.relabs.kurjercontroller.utils.extensions.showDialog
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */

class ReportFragment : BaseFragment() {
    private var nextPhotoData: ReportPhotoData? = null

    private val controller = defaultController(ReportState(), ReportContext())
    private var renderJob: Job? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(SAVED_NEXT_PHOTO_DATA_KEY, nextPhotoData)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        nextPhotoData = nextPhotoData ?: savedInstanceState?.getParcelable(SAVED_NEXT_PHOTO_DATA_KEY)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val taskItem = arguments?.getParcelable<TaskItem>(ARG_TASK_ITEM)
        val allTaskItems = arguments?.getParcelableArrayList<TaskItemWithTaskIds>(ARG_TASK_ITEMS_ALL)
        val entrance = arguments?.getParcelable<Entrance>(ARG_ENTRANCES)
        if (taskItem == null || entrance == null || allTaskItems == null) {
            FirebaseCrashlytics.getInstance().log("ti: ${taskItem} \n e: $entrance \n ati: $allTaskItems")
            return
        }
        controller.start(ReportMessages.msgInit(taskItem, entrance, allTaskItems.toList()))
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.stop()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val hintHelper = HintHelper(view.hint_container, "", false, requireActivity())
        view.hint_container.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.hint_container?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                hintHelper.maxHeight = view.appartaments_list.height + view.hint_container.height
            }
        })

        val entranceKeysAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, mutableListOf())
        view.entrance_key.adapter = entranceKeysAdapter

        val entranceEuroKeysAdapter =
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, mutableListOf())
        view.entrance_euro_key.adapter = entranceEuroKeysAdapter

        val photosAdapter = DelegateAdapter(
            ReportAdapter.photoSingle {
                uiScope.sendMessage(controller, ReportMessages.msgPhotoClicked(false, false))
            },
            ReportAdapter.photoMultiple {
                uiScope.sendMessage(controller, ReportMessages.msgPhotoClicked(true, false))
            },
            ReportAdapter.photo {
                uiScope.sendMessage(controller, ReportMessages.msgRemovePhotoClicked(it))
            }
        )
        val listInterceptor = ListClickInterceptor()
        view.photos_list.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
        view.photos_list.adapter = photosAdapter
        view.photos_list.addOnItemTouchListener(listInterceptor)

        view.entrance_code.transformationMethod = null

        val apartmentsAdapter = DelegateAdapter(
            ReportAdapter.apartmentDivider(),
            ReportAdapter.apartmentMain(
                { number, state -> uiScope.sendMessage(controller, ReportMessages.msgApartmentStateChanged(number, state)) },
                { number, state -> uiScope.sendMessage(controller, ReportMessages.msgAllApartmentStateChanged(number, state)) },
                { number -> uiScope.sendMessage(controller, ReportMessages.msgApartmentDescriptionClicked(number)) }
            ),
            ReportAdapter.apartmentAdditional(
                { number, state -> uiScope.sendMessage(controller, ReportMessages.msgApartmentStateChanged(number, state)) },
                { number, state -> uiScope.sendMessage(controller, ReportMessages.msgAllApartmentStateChanged(number, state)) },
                { number -> uiScope.sendMessage(controller, ReportMessages.msgApartmentDescriptionClicked(number)) }
            ),
            ReportAdapter.entrance { state ->
                uiScope.sendMessage(
                    controller,
                    ReportMessages.msgApartmentStateChanged(ApartmentNumber(-1), state)
                )
            },
            ReportAdapter.lookout { state ->
                uiScope.sendMessage(
                    controller,
                    ReportMessages.msgApartmentStateChanged(ApartmentNumber(-2), state)
                )
            }
        )
        view.appartaments_list.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
        view.appartaments_list.adapter = apartmentsAdapter
        view.appartaments_list.addOnItemTouchListener(listInterceptor)

        val apartmentsFromTextListener = TextChangeListener {
            if (view.appartaments_from.hasFocus()) {
                uiScope.sendMessage(controller, ReportMessages.msgApartmentsFromChanged(it.toIntOrNull()))
            }
        }
        view.appartaments_from.addTextChangedListener(apartmentsFromTextListener)
        val apartmentsToTextListener = TextChangeListener {
            if (view.appartaments_to.hasFocus()) {
                uiScope.sendMessage(controller, ReportMessages.msgApartmentsToChanged(it.toIntOrNull()))
            }
        }
        view.appartaments_to.addTextChangedListener(apartmentsToTextListener)
        val codeTextListener = TextChangeListener {
            if (view.entrance_code.hasFocus()) {
                uiScope.sendMessage(controller, ReportMessages.msgCodeChanged(it))
            }
        }
        view.entrance_code.addTextChangedListener(codeTextListener)
        val floorsTextListener = TextChangeListener {
            if (view.floors.hasFocus()) {
                it.toIntOrNull()?.let { uiScope.sendMessage(controller, ReportMessages.msgFloorsChanged(it)) }
            }
        }
        view.floors.addTextChangedListener(floorsTextListener)

        bindControls(view)

        renderJob = uiScope.launch {
            val renders = listOf(
                ReportRenders.renderLoading(view.loading),
                ReportRenders.renderEntranceKeys(view.entrance_key, entranceKeysAdapter),
                ReportRenders.renderEntranceEuroKeys(view.entrance_euro_key, entranceEuroKeysAdapter),
                ReportRenders.renderPhotos(photosAdapter),
                ReportRenders.renderNotes(view.hint_text),
                ReportRenders.renderApartmentsInterval(
                    view.appartaments_from,
                    apartmentsFromTextListener,
                    view.appartaments_to,
                    apartmentsToTextListener
                ),
                ReportRenders.renderApartments(apartmentsAdapter),
                ReportRenders.renderApartmentsListBackground(view.list_background),
                ReportRenders.renderApartmentListTypeButton(view.list_type_button),
                ReportRenders.renderEntranceCode(view.entrance_code, codeTextListener),
                ReportRenders.renderDescription(view.user_explanation_input),
                ReportRenders.renderLayoutError(view.layout_error_button),
                ReportRenders.renderLookout(view.lookout),
                ReportRenders.renderMailboxType(view.mailbox_euro, view.mailbox_gap),
                ReportRenders.renderEntranceClosed(view.entrance_closed),
                ReportRenders.renderFloors(view.floors, floorsTextListener),
                ReportRenders.renderLockInputOverlay(view.lock_input_overlay),
                ReportRenders.renderDisableControlsForClosed(
                    listOf(
                        view.appartaments_from,
                        view.appartaments_to,
                        view.floors,
                        view.entrance_code,
                        view.entrance_key,
                        view.entrance_euro_key,
                        view.layout_error_button,
                        view.lookout,
                        view.mailbox_gap,
                        view.mailbox_euro,
                        view.user_explanation_input,
                        view.list_type_button
                    )
                ),
                ReportRenders.renderDisableControlsForManualClosed(
                    listOf(
                        view.appartaments_from,
                        view.appartaments_to,
                        view.entrance_key,
                        view.entrance_euro_key,
                        view.layout_error_button,
                        view.lookout,
                        view.mailbox_gap,
                        view.mailbox_euro
                    )
                ),
                ReportRenders.renderIsClosed(view.entrance_closed),
                ReportRenders.renderIsClosed(view.close_button),
                ReportRenders.renderEuroKeyEnabled(view.entrance_euro_key)
            )
            launch { controller.stateFlow().collect(rendersCollector(renders)) }
            launch { controller.stateFlow().collect(debugCollector { debug(it) }) }
        }
        controller.context.errorContext.attach(view)

        controller.context.showError = ::showFatalError
        controller.context.showErrorMessage = ::showErrorMessage
        controller.context.showCloseEntranceDialog = ::showCloseEntranceDialog
        controller.context.requestPhoto = ::requestPhoto
        controller.context.showDescriptionInputDialog = ::showDescriptionInputDialog
        controller.context.contentResolver = { requireContext().contentResolver }
    }

    private fun showDescriptionInputDialog(apartmentNumber: ApartmentNumber, description: String, isEditable: Boolean) {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            isEnabled = isEditable
            setText(description)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Описание")
            .setView(input)
            .setPositiveButton("Ок") { _, _ ->
                uiScope.sendMessage(
                    controller,
                    ReportMessages.msgApartmentDescriptionChanged(apartmentNumber, input.text.toString())
                )
            }
            .setNegativeButton("Отмена") { _, _ -> }
            .show()
    }

    private suspend fun showErrorMessage(messageId: Int) = withContext(Dispatchers.Main) {
        showDialog(
            getString(messageId),
            R.string.ok to {}
        )

        Unit
    }

    private suspend fun showCloseEntranceDialog() = withContext(Dispatchers.Main) {
        showDialog(
            getString(R.string.close_entrance_dialog_message),
            R.string.ok to {
                uiScope.sendMessage(controller, ReportMessages.msgCloseEntrance())
            },
            R.string.no to {}
        )

        Unit
    }

    private suspend fun showFatalError(code: String, isFatal: Boolean) = withContext(Dispatchers.Main) {
        FirebaseCrashlytics.getInstance().log("fatal error $isFatal $code")
        showDialog(
            getString(R.string.unknown_runtime_error_code, code),
            R.string.ok to {
                if (isFatal) {
                    CustomLog.writeToFile("Navigate back from report, error ${code}")
                    uiScope.sendMessage(controller, ReportMessages.msgNavigateBack())
                }
            }
        )

        Unit
    }

    private fun requestPhoto(
        entrance: EntranceNumber,
        multiplePhoto: Boolean,
        targetFile: File,
        uuid: UUID,
        isEntrancePhoto: Boolean
    ) {
        val photoUri = FileProvider.getUriForFile(
            requireContext(),
            "ru.relabs.kurjercontroller.file_provider",
            targetFile
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            nextPhotoData = ReportPhotoData(entrance, multiplePhoto, photoUri, targetFile, uuid, isEntrancePhoto)
            startActivityForResult(intent, REQUEST_PHOTO_CODE)
        } else {
            uiScope.sendMessage(controller, ReportMessages.msgPhotoError(1))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_PHOTO_CODE) return
        val photoData = nextPhotoData
        nextPhotoData = null
        if (resultCode != Activity.RESULT_OK && resultCode != Activity.RESULT_CANCELED) {
            uiScope.sendMessage(controller, ReportMessages.msgPhotoError(2))
            return
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            return
        }

        if (photoData == null) {
            uiScope.sendMessage(controller, ReportMessages.msgPhotoError(3))
            return
        }

        val uri = data?.data ?: photoData.photoUri
        if (requireContext().contentResolver.getType(uri) == null) {
            uiScope.sendMessage(controller, ReportMessages.msgPhotoError(4))
            return
        }

        uiScope.sendMessage(
            controller,
            ReportMessages.msgPhotoCaptured(
                photoData.entrance,
                photoData.multiplePhoto,
                photoData.photoUri,
                photoData.targetFile,
                photoData.uuid,
                photoData.isEntrancePhoto
            )
        )
    }

    private fun bindControls(view: View) {
        view.entrance_key.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                (parent?.getItemAtPosition(position) as? String)?.let {
                    uiScope.sendMessage(controller, ReportMessages.msgKeySelected(it))
                }
            }
        }
        view.entrance_euro_key.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                (parent?.getItemAtPosition(position) as? String)?.let {
                    uiScope.sendMessage(controller, ReportMessages.msgEuroKeySelected(it))
                }
            }
        }
        view.layout_error_button.setOnClickListener {
            uiScope.sendMessage(controller, ReportMessages.msgLayoutErrorChanged())
        }
        view.lookout.setOnClickListener {
            uiScope.sendMessage(controller, ReportMessages.msgLookoutChanged())
        }
        view.mailbox_euro.setOnClickListener {
            uiScope.sendMessage(controller, ReportMessages.msgMailboxTypeChanged(1))
        }
        view.mailbox_gap.setOnClickListener {
            uiScope.sendMessage(controller, ReportMessages.msgMailboxTypeChanged(2))
        }
        view.entrance_closed.setOnClickListener {
            uiScope.sendMessage(controller, ReportMessages.msgEntranceClosedClicked())
        }
        view.list_type_button.setOnClickListener {
            uiScope.sendMessage(controller, ReportMessages.msgListTypeChanged())
        }
        view.lock_input_overlay.setOnClickListener {
            uiScope.sendMessage(controller, ReportMessages.msgPhotoClicked(false, true))
        }
        view.user_explanation_input.setOnClickListener {
            uiScope.sendMessage(controller, ReportMessages.msgApartmentDescriptionClicked(ApartmentNumber(-1)))
        }
        view.close_button.setOnClickListener {
            uiScope.sendMessage(controller, ReportMessages.msgCloseEntranceClick())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        renderJob?.cancel()
        controller.context.errorContext.detach()
        controller.context.showError = { _, _ -> }
        controller.context.showCloseEntranceDialog = {}
        controller.context.showErrorMessage = {}
        controller.context.requestPhoto = { _, _, _, _, _ -> }
        controller.context.showDescriptionInputDialog = { _, _, _ -> }
        controller.context.contentResolver = { null }
    }

    override fun interceptBackPressed(): Boolean {
        return false
    }

    companion object {
        const val ARG_TASK_ITEM = "taskItem"
        const val ARG_TASK_ITEMS_ALL = "allTaskItems"
        const val ARG_ENTRANCES = "entrances"
        const val REQUEST_PHOTO_CODE = 501
        const val SAVED_NEXT_PHOTO_DATA_KEY = "photoData"

        fun newInstance(taskItem: TaskItem, entrance: Entrance, otherTaskItemWithTaskIds: List<TaskItemWithTaskIds>) =
            ReportFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TASK_ITEM, taskItem)
                    putParcelable(ARG_ENTRANCES, entrance)
                    putParcelableArrayList(ARG_TASK_ITEMS_ALL, ArrayList(otherTaskItemWithTaskIds))
                }
            }
    }

    @Parcelize
    private data class ReportPhotoData(
        val entrance: EntranceNumber,
        val multiplePhoto: Boolean,
        val photoUri: Uri,
        val targetFile: File,
        val uuid: UUID,
        val isEntrancePhoto: Boolean
    ) : Parcelable
}