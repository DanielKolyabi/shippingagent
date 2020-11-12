package ru.relabs.kurjercontroller.presentation.addresses

import android.graphics.Color
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.core.graphics.ColorUtils
import androidx.core.widget.addTextChangedListener
import kotlinx.android.synthetic.main.holder_address_list_address.view.*
import kotlinx.android.synthetic.main.holder_address_list_other_addresses.view.*
import kotlinx.android.synthetic.main.holder_address_list_sorting.view.*
import kotlinx.android.synthetic.main.holder_address_list_task.view.*
import kotlinx.android.synthetic.main.holder_search.view.*
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.presentation.base.recycler.IAdapterDelegate
import ru.relabs.kurjercontroller.presentation.base.recycler.delegateDefine
import ru.relabs.kurjercontroller.presentation.base.recycler.delegateLoader
import ru.relabs.kurjercontroller.presentation.base.recycler.holderDefine
import ru.relabs.kurjercontroller.utils.extensions.dpToPx
import ru.relabs.kurjercontroller.utils.extensions.getColorCompat
import ru.relabs.kurjercontroller.utils.extensions.hideKeyboard
import ru.relabs.kurjercontroller.utils.extensions.visible

object AddressesAdapter {
    fun addressDelegate(
        onMapClicked: (addressTaskItems: List<TaskItem>) -> Unit
    ): IAdapterDelegate<AddressesItem> = delegateDefine(
        { it is AddressesItem.GroupHeader },
        { p ->
            holderDefine(p, R.layout.holder_address_list_address, { it as AddressesItem.GroupHeader }) { (items, showBypass) ->
                with(itemView) {
                    val address = items.firstOrNull()?.address?.name ?: resources.getString(R.string.address_unknown)
                    tv_address.text = address
                    when (items.all { it.isClosed }) {
                        true -> {
                            tv_address.setTextColor(Color.parseColor("#CCCCCC"))
                            iv_task_map.alpha = 0.4f
                            iv_task_map.isClickable = false
                        }
                        false -> {
                            tv_address.setTextColor(Color.parseColor("#000000"))
                            iv_task_map.alpha = 1f
                            iv_task_map.isClickable = true
                        }
                    }

                    iv_task_map.setOnClickListener { onMapClicked(items) }
                }
            }
        }
    )

    fun taskItemDelegate(
        onItemClicked: (taskItem: TaskItem, task: Task) -> Unit
    ): IAdapterDelegate<AddressesItem> = delegateDefine(
        { it is AddressesItem.AddressItem },
        { p ->
            holderDefine(p, R.layout.holder_address_list_task, { it as AddressesItem.AddressItem }) { (taskItem, task) ->
                itemView.setBackgroundColor(ColorUtils.setAlphaComponent(taskItem.placemarkColor, 60))
                with(itemView) {
                    itemView.iv_new_flare.visible = taskItem.isNew
                    itemView.btn_task.text = (if (task.filtered) "По фильтрам. " else "") +
                            (if (taskItem.buttonName.isNotEmpty()) taskItem.buttonName else taskItem.publisherName)

                    if (taskItem.isClosed) {
                        btn_task.setTextColor(Color.parseColor("#66000000"))
                    } else {
                        btn_task.setTextColor(Color.parseColor("#ff000000"))
                    }

                    if (taskItem.required) {
                        btn_task.setBackgroundColor(Color.argb(80, 255, 219, 139))
                    } else {
                        btn_task.setBackgroundColor(itemView.resources.getColorCompat(R.color.button_material_light))
                    }

                    btn_task.setOnClickListener { onItemClicked(taskItem, task) }
                }
            }
        }
    )

    fun sortingAdapter(onSortingSelected: (AddressesSortingMethod) -> Unit): IAdapterDelegate<AddressesItem> = delegateDefine(
        { it is AddressesItem.Sorting },
        { p ->
            holderDefine(p, R.layout.holder_address_list_sorting, { it as AddressesItem.Sorting }) { (sorting, isFiltered) ->
                when (sorting) {
                    AddressesSortingMethod.STANDARD -> {
                        itemView.btn_standart.setBackgroundColor(itemView.resources.getColorCompat(R.color.colorAccent))
                        itemView.btn_alphabetic.setBackgroundColor(itemView.resources.getColorCompat(R.color.button_material_light))
                    }
                    AddressesSortingMethod.ALPHABETIC -> {
                        itemView.btn_standart.setBackgroundColor(itemView.resources.getColorCompat(R.color.button_material_light))
                        itemView.btn_alphabetic.setBackgroundColor(itemView.resources.getColorCompat(R.color.colorAccent))
                    }
                    AddressesSortingMethod.CLOSE_TIME -> {
                        itemView.btn_standart.setBackgroundColor(itemView.resources.getColorCompat(R.color.colorAccent))
                        itemView.btn_alphabetic.setBackgroundColor(itemView.resources.getColorCompat(R.color.button_material_light))
                    }
                }

                itemView.btn_standart.text = when (isFiltered) {
                    true -> itemView.resources.getString(R.string.sort_by_time_button)
                    false -> itemView.resources.getString(R.string.sort_standart_button)
                }

                itemView.btn_standart.setOnClickListener {
                    when (isFiltered) {
                        false -> if (sorting != AddressesSortingMethod.STANDARD) {
                            onSortingSelected(AddressesSortingMethod.STANDARD)
                        }
                        true -> if (sorting != AddressesSortingMethod.CLOSE_TIME) {
                            onSortingSelected(AddressesSortingMethod.CLOSE_TIME)
                        }
                    }

                }
                itemView.btn_alphabetic.setOnClickListener {
                    if (sorting != AddressesSortingMethod.ALPHABETIC) {
                        onSortingSelected(AddressesSortingMethod.ALPHABETIC)
                    }
                }
            }
        }
    )

    fun loaderAdapter(): IAdapterDelegate<AddressesItem> =
        delegateLoader { it is AddressesItem.Loading }

    fun blankAdapter(): IAdapterDelegate<AddressesItem> = delegateDefine(
        { it is AddressesItem.Blank },
        { p ->
            holderDefine(p, R.layout.holder_empty, { it as AddressesItem.Blank }) { (closeable) ->
                val height = when(closeable){
                    true -> 112
                    false -> 56
                }
                itemView.layoutParams =
                    FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, itemView.context.dpToPx(height).toInt())
            }
        }
    )

    fun otherAddressesAdapter(onClick: () -> Unit): IAdapterDelegate<AddressesItem> = delegateDefine(
        { it is AddressesItem.OtherAddresses },
        { p ->
            holderDefine(p, R.layout.holder_address_list_other_addresses, { it as AddressesItem.OtherAddresses }) { (count) ->
                itemView.tv_more.text = itemView.resources.getString(R.string.addresses_more, count)
                itemView.setOnClickListener {
                    onClick()
                }
            }
        }
    )

    fun searchAdapter(onSearch: (String) -> Unit): IAdapterDelegate<AddressesItem> = delegateDefine(
        { it is AddressesItem.Search },
        { p ->
            holderDefine(p, R.layout.holder_search, { it as AddressesItem.Search }) { (filter) ->
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