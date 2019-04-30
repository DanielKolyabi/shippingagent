package ru.relabs.kurjercontroller.ui.fragments.taskInfo


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_taskinfo.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.relabs.kurjer.ui.delegateAdapter.DelegateAdapter
import ru.relabs.kurjercontroller.CustomLog
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.activity
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.models.AddressModel
import ru.relabs.kurjercontroller.models.TaskModel
import ru.relabs.kurjercontroller.ui.activities.ErrorButtonsListener
import ru.relabs.kurjercontroller.ui.activities.showError
import ru.relabs.kurjercontroller.ui.fragments.taskInfo.delegates.*
import ru.relabs.kurjercontroller.ui.helpers.TaskAddressSorter

class TaskInfoFragment : Fragment() {

    lateinit var task: TaskModel

    var targetAddress: AddressModel? = null
    val presenter = TaskInfoPresenter(this)
    val adapter = DelegateAdapter<TaskInfoModel>().apply {
        addDelegate(InfoAddressesHeaderDelegate())
        addDelegate(InfoInfoDelegate())
        addDelegate(InfoAddressItemDelegate { presenter.onInfoClicked(it) })
        addDelegate(InfoFilterItemDelegate())
        addDelegate(InfoFiltersHeaderDelegate())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        task_items_list?.layoutManager = LinearLayoutManager(context)
        task_items_list?.adapter = adapter

        examine_button?.isEnabled =
            !(task.androidState == TaskModel.EXAMINED || task.androidState == TaskModel.STARTED || task.androidState == TaskModel.COMPLETED)

        bindControls()
        if(adapter.data.isEmpty()) {
            populateList(task)
        }

        targetAddress?.let {
            //HACK. List won't scroll without timeout ¯\_(ツ)_/¯
            presenter.bgScope.launch {
                delay(250)
                withContext(Dispatchers.Main){
                    scrollToAddress(it)
                }
            }
        }
    }

    private fun scrollToAddress(address: AddressModel) {
        val idx = adapter.data.indexOfFirst {
            (it as? TaskInfoModel.TaskItem)?.taskItem?.address?.idnd == address.idnd
        }
        if (idx < 0) {
            return
        }

        task_items_list?.smoothScrollToPosition(idx)
    }

    private fun bindControls() {
        examine_button?.setOnClickListener {
            presenter.onExamineClicked()
        }

        show_map_button?.setOnClickListener {
            presenter.onShowMapClicked()
        }
    }

    private fun populateList(task: TaskModel) {
        if (task.taskFilters?.all?.isNotEmpty() == true) {
            populateListFilters(task)
        } else {
            populateListAddresses(task)
        }
    }

    private fun populateListAddresses(task: TaskModel) {
        adapter.data.clear()
        adapter.data.add(TaskInfoModel.Task(task))
        adapter.data.add(TaskInfoModel.DetailsAddressTableHeader)
        adapter.data.addAll(TaskAddressSorter.sortInfoTaskItemsAlphabetic(task.taskItems).map {
            TaskInfoModel.TaskItem(it)
        })
        adapter.notifyDataSetChanged()
    }

    private fun populateListFilters(task: TaskModel) {
        adapter.data.clear()
        adapter.data.add(TaskInfoModel.Task(task))
        adapter.data.add(TaskInfoModel.DetailsFiltersTableHeader)
        if (task.taskFilters != null) {
            adapter.data.addAll(task.taskFilters.publishers.map {
                TaskInfoModel.FilterItem("Издатель", it)
            })
            adapter.data.addAll(task.taskFilters.brigades.map {
                TaskInfoModel.FilterItem("Бригада", it)
            })
            adapter.data.addAll(task.taskFilters.users.map {
                TaskInfoModel.FilterItem("Распространитель", it)
            })
            adapter.data.addAll(task.taskFilters.districts.map {
                TaskInfoModel.FilterItem("Округ", it)
            })
            adapter.data.addAll(task.taskFilters.regions.map {
                TaskInfoModel.FilterItem("Район", it)
            })
        }
        adapter.notifyDataSetChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_taskinfo, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val tempTask: TaskModel? = it.getParcelable("task")
            if (tempTask == null) {
                CustomLog.writeToFile("null task in TaskInfoFragment")
                activity()?.showError("Произошла ошибка", object : ErrorButtonsListener {
                    override fun positiveListener() {
                        application().router.exit()
                    }
                })
                return
            }

            task = tempTask
        }
    }

    override fun onDestroy() {
        presenter.bgScope.terminate()
        super.onDestroy()
    }

    companion object {
        @JvmStatic
        fun newInstance(task: TaskModel) =
            TaskInfoFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("task", task)
                }
            }
    }
}
