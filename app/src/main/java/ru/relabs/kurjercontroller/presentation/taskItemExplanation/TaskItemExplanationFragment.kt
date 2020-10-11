package ru.relabs.kurjercontroller.presentation.taskItemExplanation

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_task_item_explanation.*
import ru.relabs.kurjercontroller.utils.CustomLog
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.utils.extensions.showDialog

/**
 * Created by ProOrange on 18.03.2019.
 */
class TaskItemExplanationFragment : Fragment() {

    private var taskItem: TaskItem? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (taskItem?.notes ?: emptyList()).forEachIndexed { num, text ->
            setInfoText(num, text)
        }
    }

    private fun setInfoText(fieldNum: Int, text: String) {
        val views = listOf(note3_text, note2_text, note1_text)
        if (fieldNum < 0 || fieldNum > 2) {
            return
        }

        views[fieldNum].text = Html.fromHtml(text)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_task_item_explanation, container, false)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val tempTaskItem: TaskItem? = it.getParcelable("taskItem")
            if (tempTaskItem == null) {
                CustomLog.writeToFile("null taskItem in TaskInfoFragment")
                showDialog(resources.getString(R.string.fatal_error_title, "tief:100"))
                return
            }

            taskItem = tempTaskItem
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(taskItem: TaskItem) =
            TaskItemExplanationFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("taskItem", taskItem)
                }
            }
    }
}