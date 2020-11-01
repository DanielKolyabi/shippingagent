package ru.relabs.kurjercontroller.presentation.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_report.view.*
import kotlinx.android.synthetic.main.fragment_tasks.view.loading
import kotlinx.android.synthetic.main.include_hint_container.view.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.models.Entrance
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.presentation.base.TextChangeListener
import ru.relabs.kurjercontroller.presentation.base.fragment.BaseFragment
import ru.relabs.kurjercontroller.presentation.base.recycler.DelegateAdapter
import ru.relabs.kurjercontroller.presentation.base.tea.debugCollector
import ru.relabs.kurjercontroller.presentation.base.tea.defaultController
import ru.relabs.kurjercontroller.presentation.base.tea.rendersCollector
import ru.relabs.kurjercontroller.presentation.base.tea.sendMessage
import ru.relabs.kurjercontroller.presentation.helpers.HintHelper
import ru.relabs.kurjercontroller.utils.debug


/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */

class ReportFragment : BaseFragment() {

    private val controller = defaultController(ReportState(), ReportContext())
    private var renderJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val task = arguments?.getParcelable<Task>(ARG_TASK)
        val taskItem = arguments?.getParcelable<TaskItem>(ARG_TASK_ITEM)
        val entrance = arguments?.getParcelable<Entrance>(ARG_ENTRANCES)
        if(task == null || taskItem == null || entrance == null){
            //TODO: Show error
            return
        }
        controller.start(ReportMessages.msgInit(task, taskItem, entrance))
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

        val entranceKeysAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, emptyList())
        view.entrance_key.adapter = entranceKeysAdapter

        val entranceEuroKeysAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, emptyList())
        view.entrance_euro_key.adapter = entranceEuroKeysAdapter

        val photosAdapter = DelegateAdapter(
            ReportAdapter.photoSingle {
                uiScope.sendMessage(controller, ReportMessages.msgPhotoClicked(null, false))
            },
            ReportAdapter.photo {
                uiScope.sendMessage(controller, ReportMessages.msgRemovePhotoClicked(it))
            }
        )
        val listInterceptor = ListClickInterceptor()
        view.photos_list.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
        view.photos_list.adapter = photosAdapter
        view.photos_list.addOnItemTouchListener(listInterceptor)

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
            )
        )
        view.appartaments_list.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
        view.appartaments_list.adapter = apartmentsAdapter
        view.appartaments_list.addOnItemTouchListener(listInterceptor)


        val apartmentsFromTextListener = TextChangeListener {
            uiScope.sendMessage(controller, ReportMessages.msgApartmentsFromChanged(it.toIntOrNull()))
        }
        view.appartaments_from.addTextChangedListener(apartmentsFromTextListener)
        val apartmentsToTextListener = TextChangeListener {
            uiScope.sendMessage(controller, ReportMessages.msgApartmentsToChanged(it.toIntOrNull()))
        }
        view.appartaments_to.addTextChangedListener(apartmentsToTextListener)
        val codeTextListener = TextChangeListener {
            uiScope.sendMessage(controller, ReportMessages.msgCodeChanged(it))
        }
        view.entrance_code.addTextChangedListener(codeTextListener)

        bindControls(view)

        renderJob = uiScope.launch {
            val renders = listOf(
                ReportRenders.renderLoading(view.loading),
                ReportRenders.renderEntranceKeys(view.entrance_key, entranceKeysAdapter),
                ReportRenders.renderEntranceEuroKeys(view.entrance_euro_key, entranceEuroKeysAdapter),
                ReportRenders.renderPhotos(photosAdapter),
                ReportRenders.renderNotes(view.hint_text),
                ReportRenders.renderApartmentsInterval(view.appartaments_from, view.appartaments_to),
                ReportRenders.renderApartments(apartmentsAdapter),
                ReportRenders.renderApartmentsListBackground(view.list_background),
                ReportRenders.renderApartmentListTypeButton(view.list_type_button),
                ReportRenders.renderEntranceCode(view.entrance_code, codeTextListener),
                ReportRenders.renderDescription(view.user_explanation_input),
                ReportRenders.renderLayoutError(view.layout_error_button),
                ReportRenders.renderLookout(view.lookout),
                ReportRenders.renderMailboxType(view.mailbox_euro, view.mailbox_gap),
                ReportRenders.renderEntranceClosed(view.entrance_closed)
            )
            launch { controller.stateFlow().collect(rendersCollector(renders)) }
            launch { controller.stateFlow().collect(debugCollector { debug(it) }) }
        }
        controller.context.errorContext.attach(view)
    }

    private fun bindControls(
        view: View
    ) {
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        renderJob?.cancel()
        controller.context.errorContext.detach()
    }

    override fun interceptBackPressed(): Boolean {
        return false
    }

    companion object {
        const val ARG_TASK = "task"
        const val ARG_TASK_ITEM = "taskItem"
        const val ARG_ENTRANCES = "entrances"

        fun newInstance(task: Task, taskItem: TaskItem, entrances: Entrance) = ReportFragment().apply{
            arguments = Bundle().apply{
                putParcelable(ARG_TASK, task)
                putParcelable(ARG_TASK_ITEM, taskItem)
                putParcelable(ARG_ENTRANCES, entrance)
            }
        }
    }
}