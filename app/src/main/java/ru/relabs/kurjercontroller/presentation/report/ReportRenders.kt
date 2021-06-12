package ru.relabs.kurjercontroller.presentation.report

import android.text.Html
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.models.ApartmentNumber
import ru.relabs.kurjercontroller.domain.models.ApartmentResult
import ru.relabs.kurjercontroller.domain.models.EntranceState
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.presentation.base.DefaultListDiffCallback
import ru.relabs.kurjercontroller.presentation.base.recycler.DelegateAdapter
import ru.relabs.kurjercontroller.presentation.base.tea.renderT
import ru.relabs.kurjercontroller.utils.debug
import ru.relabs.kurjercontroller.utils.extensions.renderText
import ru.relabs.kurjercontroller.utils.extensions.setSelectButtonActive
import ru.relabs.kurjercontroller.utils.extensions.visible

/**
 * Created by Daniil Kurchanov on 06.04.2020.
 */
object ReportRenders {
    fun renderLoading(view: View, gpsLoadingLabel: View): ReportRender = renderT(
        { (it.loaders > 0) to it.isGPSLoading },
        { (loading, isGPSLoading) ->
            view.visible = loading
            gpsLoadingLabel.visible = isGPSLoading && loading
        }
    )

    fun renderEntranceKeys(spinner: Spinner, adapter: ArrayAdapter<String>): ReportRender = renderT(
        {
            (it.entranceKeys.takeIf { it.isNotEmpty() }
                ?: listOf(adapter.context.getString(R.string.loading))) to it.selectedKey
        },
        { (keys, selectedKey) ->
            adapter.clear()
            adapter.addAll(keys)
            adapter.notifyDataSetChanged()

            spinner.setSelection(keys.indexOf(selectedKey).takeIf { it != -1 } ?: 0)
        }
    )

    fun renderEntranceEuroKeys(spinner: Spinner, adapter: ArrayAdapter<String>): ReportRender = renderT(
        {
            (it.entranceEuroKeys.takeIf { it.isNotEmpty() }
                ?: listOf(adapter.context.getString(R.string.loading))) to it.selectedEuroKey
        },
        { (keys, selectedKey) ->
            adapter.clear()
            adapter.addAll(keys)
            adapter.notifyDataSetChanged()

            spinner.setSelection(keys.indexOf(selectedKey).takeIf { it != -1 } ?: 0)
        }
    )

    fun renderPhotos(adapter: DelegateAdapter<ReportPhotoItem>): ReportRender = renderT(
        { it.selectedEntrancePhotos },
        { photos ->
            val newItems =
                listOf(ReportPhotoItem.Multiple, ReportPhotoItem.Single) + photos.map { ReportPhotoItem.Photo(it.photo, it.uri) }

            val diff = DiffUtil.calculateDiff(DefaultListDiffCallback(adapter.items, newItems))

            adapter.items.clear()
            adapter.items.addAll(newItems)
            diff.dispatchUpdatesTo(adapter)
        }
    )

    fun renderNotes(hintText: TextView): ReportRender = renderT(
        { it.taskItem?.notes.orEmpty() to it.entrance?.code.orEmpty() },
        { (notes, code) ->
            hintText.text = Html.fromHtml("<b>Код: ${code}</b><br/>" + (3 downTo 1).joinToString("<br/>") {
                "<b>Пр. $it</b><br/>" + notes.getOrElse(it - 1) { "" }
            })

        }
    )

    fun renderApartmentsInterval(
        apartmentsFrom: EditText,
        fromWatcher: TextWatcher,
        apartmentsTo: EditText,
        toWatcher: TextWatcher
    ): ReportRender = renderT(
        { (it.saved?.apartmentFrom ?: it.entrance?.startApartments) to (it.saved?.apartmentTo ?: it.entrance?.endApartments) },
        { (from, to) ->
            apartmentsFrom.renderText(from?.toString() ?: "", fromWatcher)
            apartmentsTo.renderText(to?.toString() ?: "", toWatcher)
        }
    )

    private data class RenderApartmentsData(
        val apartmentsInterval: Pair<Int?, Int?>,
        val taskItem: TaskItem?,
        val savedApartments: List<ApartmentResult>,
        val hasLookout: Boolean
    )

    fun renderApartments(apartmentsAdapter: DelegateAdapter<ReportApartmentItem>): ReportRender = renderT(
        {
            RenderApartmentsData(
                (it.saved?.apartmentFrom ?: it.entrance?.startApartments) to (it.saved?.apartmentTo
                    ?: it.entrance?.endApartments),
                it.taskItem,
                it.savedApartments,
                it.saved?.hasLookupPost ?: it.entrance?.hasLookout ?: false
            )
        },
        { (apartmentInterval, taskItem, savedApartments, hasLookout) ->
            val (startAps, endAps) = apartmentInterval
            if (startAps != null && endAps != null && taskItem != null) {
                val requiredApartments = taskItem.getRequiredApartments()
                val apartments = (startAps..endAps)
                    .map { apartment -> ApartmentNumber(apartment) to requiredApartments.firstOrNull { it.number == apartment } }
                    .map { (apartmentNumber, requiredData) ->
                        val saved = savedApartments.firstOrNull { it.apartmentNumber == apartmentNumber }
                        ReportApartmentItem.Apartment(
                            number = apartmentNumber,
                            buttonGroup = saved?.buttonGroup
                                ?: savedApartments.firstOrNull { it.apartmentNumber.number > 0 }?.buttonGroup
                                ?: taskItem.defaultReportType,
                            state = saved?.buttonState ?: 0,
                            colored = requiredData?.colored ?: false,
                            required = requiredData != null
                        )
                    }

                val requiredApartmentsElements = apartments.filter { it.required }
                val notRequiredApartmentElements = apartments.filter { !it.required }
                val buttonGroup = apartments.firstOrNull()?.buttonGroup ?: ReportApartmentButtonsMode.Main
                val headerModeItem = if (buttonGroup == ReportApartmentButtonsMode.Main && hasLookout) {
                    ReportApartmentItem.Lookout(savedApartments.firstOrNull { it.apartmentNumber.number == -2 }?.buttonState ?: 0)
                } else if (buttonGroup == ReportApartmentButtonsMode.Additional) {
                    ReportApartmentItem.Entrance(
                        savedApartments.firstOrNull { it.apartmentNumber.number == -1 }?.buttonState ?: 0
                    )
                } else {
                    null
                }
                debug("$buttonGroup $hasLookout")
                val items =
                    listOfNotNull(headerModeItem) +
                            requiredApartmentsElements +
                            listOfNotNull(ReportApartmentItem.Divider.takeIf { requiredApartments.isNotEmpty() }) +
                            notRequiredApartmentElements

                debug("$items")
                val diff = DiffUtil.calculateDiff(DefaultListDiffCallback(apartmentsAdapter.items, items))

                apartmentsAdapter.items.clear()
                apartmentsAdapter.items.addAll(items)
                diff.dispatchUpdatesTo(apartmentsAdapter)

            } else {
                apartmentsAdapter.items.clear()
                apartmentsAdapter.notifyDataSetChanged()
            }
        }
    )

    fun renderApartmentsListBackground(view: ImageView): ReportRender = renderT(
        {
            it.savedApartments.firstOrNull { it.apartmentNumber.number > 0 }?.buttonGroup
                ?: it.taskItem?.defaultReportType
                ?: ReportApartmentButtonsMode.Main
        },
        {
            view.background = when (it) {
                ReportApartmentButtonsMode.Main -> view.resources.getDrawable(R.drawable.apartment_list_main_bg, null)
                ReportApartmentButtonsMode.Additional -> null
            }
        }
    )

    fun renderApartmentListTypeButton(view: Button): ReportRender = renderT(
        {
            it.savedApartments.firstOrNull { it.apartmentNumber.number > 0 }?.buttonGroup
                ?: it.taskItem?.defaultReportType
                ?: ReportApartmentButtonsMode.Main
        },
        {
            view.text = when (it) {
                ReportApartmentButtonsMode.Main -> view.resources.getString(R.string.button_group_main)
                ReportApartmentButtonsMode.Additional -> view.resources.getString(R.string.button_group_addition)
            }
            view.setSelectButtonActive(it == ReportApartmentButtonsMode.Additional)
        }
    )

    fun renderEntranceCode(view: EditText, watcher: TextWatcher): ReportRender = renderT(
        { it.saved?.code ?: "" },
        { view.renderText(it, watcher) }
    )

    fun renderDescription(view: TextView): ReportRender = renderT(
        { it.saved?.description ?: "" },
        { view.text = it }
    )

    fun renderMailboxType(mailboxEuro: Button, mailboxGap: Button): ReportRender = renderT(
        { it.saved?.mailboxType ?: it.entrance?.mailboxType ?: 1 },
        {
            mailboxEuro.setSelectButtonActive(it == 1)
            mailboxGap.setSelectButtonActive(it == 2)
        }
    )

    fun renderLookout(lookout: Button): ReportRender = renderT(
        { it.saved?.hasLookupPost ?: it.entrance?.hasLookout ?: false },
        { lookout.setSelectButtonActive(it) }
    )

    fun renderLayoutError(button: Button): ReportRender = renderT(
        { it.saved?.isDeliveryWrong ?: false },
        { button.setSelectButtonActive(it) }
    )

    fun renderEntranceClosed(button: Button): ReportRender = renderT(
        { (it.entrance?.state == EntranceState.CLOSED) || (it.saved?.entranceClosed ?: false) },
        { button.setSelectButtonActive(it) }
    )

    fun renderFloors(view: EditText, watcher: TextWatcher): ReportRender = renderT(
        { it.saved?.floors?.toString() ?: it.entrance?.floors?.toString() ?: "" },
        { view.renderText(it, watcher) }
    )

    fun renderLockInputOverlay(view: View): ReportRender = renderT(
        { it.selectedEntrancePhotos.none { it.photo.isEntrancePhoto } },
        { view.visible = it }
    )

    fun renderDisableControlsForClosed(views: List<View>): ReportRender = renderT(
        { it.entrance?.state == EntranceState.CREATED },
        { enabled -> views.forEach { it.isEnabled = enabled } }
    )

    fun renderDisableControlsForManualClosed(views: List<View>): ReportRender = renderT(
        { it.saved?.entranceClosed == true },
        { disabled -> views.forEach { it.isEnabled = !disabled } }
    )

    fun renderIsClosed(view: View): ReportRender = renderT(
        { it.entrance?.state == EntranceState.CREATED },
        { view.isEnabled = it }
    )

    fun renderEuroKeyEnabled(view: Spinner): ReportRender = renderT(
        {
            Pair(
                it.saved?.mailboxType ?: it.entrance?.mailboxType,
                it.entrance?.state == EntranceState.CLOSED || it.saved?.entranceClosed == true
            )
        },
        { (mailboxType, isEntranceClosed) ->
            view.isEnabled = mailboxType == 1 && !isEntranceClosed
        }
    )
}