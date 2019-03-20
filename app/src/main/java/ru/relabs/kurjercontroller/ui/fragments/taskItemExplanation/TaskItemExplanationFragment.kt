package ru.relabs.kurjercontroller.ui.fragments.taskItemExplanation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.relabs.kurjercontroller.R

/**
 * Created by ProOrange on 18.03.2019.
 */
class TaskItemExplanationFragment : Fragment() {

    val presenter = TaskItemExplanationPresenter(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_task_item_explanation, container, false)
    }
}
