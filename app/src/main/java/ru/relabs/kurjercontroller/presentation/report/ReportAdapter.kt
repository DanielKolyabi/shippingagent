package ru.relabs.kurjercontroller.presentation.report

import android.graphics.Color
import android.graphics.Typeface
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.holder_report_appartament.view.*
import kotlinx.android.synthetic.main.holder_report_appartament_button_group_addition.view.*
import kotlinx.android.synthetic.main.holder_report_appartament_button_group_main.view.*
import kotlinx.android.synthetic.main.holder_report_photo.view.*
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.models.ApartmentNumber
import ru.relabs.kurjercontroller.domain.models.EntrancePhoto
import ru.relabs.kurjercontroller.presentation.base.recycler.IAdapterDelegate
import ru.relabs.kurjercontroller.presentation.base.recycler.delegateDefine
import ru.relabs.kurjercontroller.presentation.base.recycler.holderDefine
import ru.relabs.kurjercontroller.utils.extensions.setSelectButtonActive
import ru.relabs.kurjercontroller.utils.extensions.setVisible

object ReportAdapter {

    fun photoSingle(onPhotoClicked: () -> Unit): IAdapterDelegate<ReportPhotoItem> = delegateDefine(
        { it is ReportPhotoItem.Single },
        { p ->
            holderDefine(p, R.layout.holder_report_photo_blank, { it as ReportPhotoItem.Single }) {
                itemView.setOnClickListener {
                    onPhotoClicked()
                }
            }
        }
    )

    fun photoMultiple(onPhotoClicked: () -> Unit): IAdapterDelegate<ReportPhotoItem> = delegateDefine(
        { it is ReportPhotoItem.Multiple },
        { p ->
            holderDefine(p, R.layout.holder_report_photo_blank_multi, { it as ReportPhotoItem.Multiple }) {
                itemView.setOnClickListener {
                    onPhotoClicked()
                }
            }
        }
    )

    fun photo(onRemoveClicked: (EntrancePhoto) -> Unit): IAdapterDelegate<ReportPhotoItem> = delegateDefine(
        { it is ReportPhotoItem.Photo },
        { p ->
            holderDefine(p, R.layout.holder_report_photo, { it as ReportPhotoItem.Photo }) { (photo, uri) ->
                itemView.remove.setOnClickListener {
                    onRemoveClicked(photo)
                }

                Glide.with(itemView)
                    .load(uri)
                    .into(itemView.photo)
            }
        }
    )

    fun apartmentDivider(): IAdapterDelegate<ReportApartmentItem> = delegateDefine(
        { it is ReportApartmentItem.Divider },
        { p ->
            holderDefine(p, R.layout.holder_divider, { it as ReportApartmentItem.Divider }) { }
        }
    )

    fun lookout(onStateChanged: (state: Int) -> Unit): IAdapterDelegate<ReportApartmentItem> = delegateDefine(
        { it is ReportApartmentItem.Lookout },
        { p ->
            holderDefine(p, R.layout.holder_report_appartament, { it as ReportApartmentItem.Lookout }) { (state) ->
                itemView.buttons_list?.setOnTouchListener { view, motionEvent ->
                    itemView.buttons_list?.scrollX = 0
                    return@setOnTouchListener true
                }

                itemView.description_button?.setVisible(false)
                itemView.appartament_number?.text = "Вахта"
                itemView.buttons_list?.adapter = ApartmentButtonsPagerAdapter(
                    itemView.context,
                    state,
                    { newState ->
                        onStateChanged(newState)
                    },
                    {},
                    false
                )
                itemView.buttons_list?.currentItem = 0
            }
        }
    )


    fun entrance(onStateChanged: (state: Int) -> Unit): IAdapterDelegate<ReportApartmentItem> = delegateDefine(
        { it is ReportApartmentItem.Entrance },
        { p ->
            holderDefine(p, R.layout.holder_report_appartament, { it as ReportApartmentItem.Entrance }) { (state) ->

                itemView.buttons_list?.setOnTouchListener { view, motionEvent ->
                    itemView.buttons_list?.scrollX = 0
                    return@setOnTouchListener true
                }

                itemView.description_button?.setVisible(false)
                itemView.appartament_number?.text = "Под."
                itemView.buttons_list?.adapter = ApartmentButtonsPagerAdapter(
                    itemView.context,
                    state,
                    { newState ->
                        onStateChanged(newState)
                    },
                    {},
                    false
                )
                itemView.buttons_list?.currentItem = 1
            }
        }
    )

    fun apartmentMain(
        onStateChanged: (apartmentNumber: ApartmentNumber, state: Int) -> Unit,
        onLongStateChanged: (apartmentNumber: ApartmentNumber, change: Int) -> Unit,
        onDescriptionClicked: (apartmentNumber: ApartmentNumber) -> Unit
    ): IAdapterDelegate<ReportApartmentItem> = delegateDefine(
        { it is ReportApartmentItem.Apartment && it.buttonGroup == ReportApartmentButtonsMode.Main },
        { p ->
            holderDefine(p, R.layout.holder_report_appartament_main, { it as ReportApartmentItem.Apartment }) { item ->
                with(itemView) {
                    if (item.colored) {
                        setBackgroundColor(Color.parseColor("#77ff0000"))
                    } else {
                        background = null
                    }
                    if (item.required) {
                        appartament_number?.setTextColor(Color.parseColor("#0000ff"))
                        appartament_number?.setTypeface(null, Typeface.BOLD)
                    } else {
                        appartament_number?.setTextColor(Color.parseColor("#000000"))
                        appartament_number?.setTypeface(null, Typeface.NORMAL)
                    }
                    description_button?.setOnClickListener {
                        onDescriptionClicked(item.number)
                    }
                    appartament_number?.text = item.number.number.toString()

                    yes_button_main?.setOnLongClickListener {
                        onLongStateChanged(item.number, 1)
                        true
                    }
                    yes_button_main?.setOnClickListener {
                        val newState = if (item.state and 4 > 0) {
                            item.state xor 4 xor 1
                        } else {
                            item.state xor 1
                        }
                        onStateChanged(item.number, newState)
                    }

                    not_regular_button_main?.setOnLongClickListener {
                        onLongStateChanged(item.number, 2)
                        true
                    }
                    not_regular_button_main?.setOnClickListener {
                        onStateChanged(item.number, item.state xor 2)
                    }

                    no_button_main?.setOnLongClickListener {
                        onLongStateChanged(item.number, 4)
                        true
                    }
                    no_button_main?.setOnClickListener {
                        val newState = if (item.state and 1 > 0) {
                            item.state xor 1 xor 4
                        }else{
                            item.state xor 4
                        }
                        onStateChanged(item.number, newState)
                    }

                    broken_button_main?.setOnLongClickListener {
                        onLongStateChanged(item.number, 8)
                        true
                    }
                    broken_button_main?.setOnClickListener {
                        onStateChanged(item.number, item.state xor 8)
                    }

                    yes_button_main?.setSelectButtonActive(item.state and 1 > 0)
                    not_regular_button_main?.setSelectButtonActive(item.state and 2 > 0)
                    no_button_main?.setSelectButtonActive(item.state and 4 > 0)
                    broken_button_main?.setSelectButtonActive(item.state and 8 > 0)
                }
            }
        }
    )

    fun apartmentAdditional(
        onStateChanged: (apartmentNumber: ApartmentNumber, state: Int) -> Unit,
        onLongStateChanged: (apartmentNumber: ApartmentNumber, change: Int) -> Unit,
        onDescriptionClicked: (apartmentNumber: ApartmentNumber) -> Unit
    ): IAdapterDelegate<ReportApartmentItem> = delegateDefine(
        { it is ReportApartmentItem.Apartment && it.buttonGroup == ReportApartmentButtonsMode.Additional },
        { p ->
            holderDefine(p, R.layout.holder_report_appartament_addition, { it as ReportApartmentItem.Apartment }) { item ->
                with(itemView) {
                    if (item.colored) {
                        setBackgroundColor(Color.parseColor("#77ff0000"))
                    } else {
                        background = null
                    }
                    if (item.required) {
                        appartament_number?.setTextColor(Color.parseColor("#0000ff"))
                        appartament_number?.setTypeface(null, Typeface.BOLD)
                    } else {
                        appartament_number?.setTextColor(Color.parseColor("#000000"))
                        appartament_number?.setTypeface(null, Typeface.NORMAL)
                    }

                    description_button?.setOnClickListener {
                        onDescriptionClicked(item.number)
                    }
                    appartament_number?.text = item.number.number.toString()

                    yes_button_addition?.setOnLongClickListener {
                        onLongStateChanged(item.number, 16)
                        true
                    }
                    yes_button_addition?.setOnClickListener {
                        val newState = if (item.state and 32 > 0) {
                            item.state xor 32 xor 16
                        }else{
                            item.state xor 16
                        }
                        onStateChanged(item.number, newState)
                    }

                    no_button_addition?.setOnLongClickListener {
                        onLongStateChanged(item.number, 32)
                        true
                    }
                    no_button_addition?.setOnClickListener {
                        val newState = if (item.state and 16 > 0) {
                            item.state xor 16 xor 32
                        }else{
                            item.state xor 32
                        }
                        onStateChanged(item.number, newState)
                    }

                    broken_button_addition?.setOnLongClickListener {
                        onLongStateChanged(item.number, 8)
                        true
                    }
                    broken_button_addition?.setOnClickListener {
                        onStateChanged(item.number, item.state xor 8)
                    }

                    broken_button_addition?.setSelectButtonActive(item.state and 8 > 0)
                    yes_button_addition?.setSelectButtonActive(item.state and 16 > 0)
                    no_button_addition?.setSelectButtonActive(item.state and 32 > 0)
                }
            }
        }
    )
}