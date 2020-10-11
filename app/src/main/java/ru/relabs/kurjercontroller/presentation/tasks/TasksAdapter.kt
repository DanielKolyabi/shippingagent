package ru.relabs.kurjercontroller.presentation.tasks

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import kotlinx.android.synthetic.main.holder_search.view.*
import kotlinx.android.synthetic.main.holder_task.view.*
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskState
import ru.relabs.kurjercontroller.presentation.base.recycler.IAdapterDelegate
import ru.relabs.kurjercontroller.presentation.base.recycler.delegateDefine
import ru.relabs.kurjercontroller.presentation.base.recycler.holderDefine
import ru.relabs.kurjercontroller.utils.extensions.hideKeyboard
import ru.relabs.kurjercontroller.utils.extensions.visible

object TasksAdapter {

    fun headerAdapter(): IAdapterDelegate<TasksItem> = delegateDefine(
        { it is TasksItem.Header },
        { p ->
            holderDefine(
                p,
                R.layout.holder_tasks_header,
                { it as TasksItem.Header }) { (title) ->
                with(itemView) {
                    tv_title.text = title
                }
            }
        }
    )

    fun taskAdapter(
        onSelectedClicked: (task: Task) -> Unit,
        onTaskClicked: (task: Task) -> Unit
    ): IAdapterDelegate<TasksItem> = delegateDefine(
        { it is TasksItem.TaskItem },
        { p ->
            holderDefine(
                p,
                R.layout.holder_task,
                { it as TasksItem.TaskItem }) { (task, isTasksWithSameAddressPresented, isSelected) ->
                with(itemView) {
                    tv_title.text = task.name
                    when (isSelected) {
                        true -> iv_selected.setImageResource(R.drawable.ic_chain_enabled)
                        false -> iv_selected.setImageResource(R.drawable.ic_chain_disabled)
                    }
                    iv_active.visible = task.state.state != TaskState.CREATED
                    when (task.state.byOtherUser) {
                        true -> iv_active.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN)
                        false -> iv_active.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
                    }
                    iv_selected.setOnClickListener {
                        onSelectedClicked(task)
                    }
                    setOnClickListener {
                        onTaskClicked(task)
                    }
                    when (isTasksWithSameAddressPresented) {
                        true -> setBackgroundColor(Color.GRAY)
                        else -> setBackgroundColor(Color.TRANSPARENT)
                    }
                }
            }
        }
    )


    fun loaderAdapter(): IAdapterDelegate<TasksItem> = delegateDefine(
        { it is TasksItem.Loader },
        { p ->
            holderDefine(p, R.layout.holder_loader, { it as TasksItem.Loader }) { (title) ->
                with(itemView) {
                    tv_title.visible = title.isNotEmpty()
                    tv_title.text = title
                }
            }
        }
    )

    fun blankAdapter(): IAdapterDelegate<TasksItem> = delegateDefine(
        { it is TasksItem.Blank },
        { p ->
            holderDefine(p, R.layout.holder_empty, { it as TasksItem.Blank }) {}
        }
    )

    fun searchAdapter(onSearch: (String) -> Unit): IAdapterDelegate<TasksItem> = delegateDefine(
        { it is TasksItem.Search },
        { p ->
            holderDefine(p, R.layout.holder_search, { it as TasksItem.Search }) { (filter) ->
                itemView.et_search.setText(filter)
                itemView.iv_clear.visible = filter.isNotBlank()

                itemView.et_search.addTextChangedListener {
                    val text = (it?.toString() ?: "")
                    itemView.iv_clear.visible = text.isNotBlank()
                    onSearch(text)
                }
                if (itemView.et_search.text.isNotEmpty()) {
                    itemView.et_search.requestFocus()
                }
                itemView.et_search.setOnEditorActionListener { _, actionId, event ->
                    if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        actionId == EditorInfo.IME_ACTION_NEXT ||
                        event != null &&
                        event.action == KeyEvent.ACTION_DOWN &&
                        event.keyCode == KeyEvent.KEYCODE_ENTER
                    ) {
                        itemView.hideKeyboard(itemView.context)
                        true
                    }
                    false
                }
                itemView.iv_clear.setOnClickListener {
                    itemView.et_search.setText("")
                    itemView.hideKeyboard(itemView.context)
                }
            }
        }
    )
}