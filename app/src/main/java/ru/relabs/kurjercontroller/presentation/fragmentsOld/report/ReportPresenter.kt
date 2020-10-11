package ru.relabs.kurjercontroller.presentation.fragmentsOld.report

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_report.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.fileHelpers.ImageUtils
import ru.relabs.kurjercontroller.utils.CancelableScope
import ru.relabs.kurjercontroller.data.database.entities.EntranceResultEntity
import ru.relabs.kurjercontroller.fileHelpers.PathHelper
import ru.relabs.kurjercontroller.logError
import ru.relabs.kurjercontroller.domain.models.Entrance
import ru.relabs.kurjercontroller.domain.models.EntrancePhotoModel
import ru.relabs.kurjercontroller.presentation.splash.showError
import ru.relabs.kurjercontroller.utils.extensions.setSelectButtonActive
import ru.relabs.kurjercontroller.presentation.fragmentsOld.report.models.ApartmentListModel
import ru.relabs.kurjercontroller.presentation.fragmentsOld.report.models.ReportPhotosListModel
import java.io.File
import java.util.*

/**
 * Created by ProOrange on 18.03.2019.
 */

const val REQUEST_PHOTO = 1

class ReportPresenter(val fragment: ReportFragment) {
    var photoUUID: UUID? = null
    var photoMultiMode: Boolean = false
    var photoEntrance: Boolean = false

    fun onBlankMultiPhotoClicked() {
        requestPhoto()
    }

    fun onBlankPhotoClicked(isEntrancePhoto: Boolean) {
        requestPhoto(false, isEntrancePhoto)
    }

    private fun requestPhoto(multiPhoto: Boolean = true, entrancePhoto: Boolean = false) {
        val uuid = UUID.randomUUID()
        photoUUID = uuid
        photoMultiMode = multiPhoto
        photoEntrance = entrancePhoto
        val photoFile = PathHelper.getEntrancePhotoFile(fragment.taskItem, fragment.entrance, uuid)

        val intent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent?.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))

        fragment.context?.packageManager?.let { packageManager ->
            intent?.let { intent ->
                if (intent.resolveActivity(packageManager) != null) {
                    fragment.startActivityForResult(intent, REQUEST_PHOTO)
                }
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == REQUEST_PHOTO) {
            if (resultCode != Activity.RESULT_OK) {
                if (resultCode != Activity.RESULT_CANCELED) {
                    fragment.context?.showError("Не удалось сделать фото.")
                }
                return false
            }
            val photoFile = PathHelper.getEntrancePhotoFile(
                fragment.taskItem,
                fragment.entrance,
                photoUUID ?: UUID.randomUUID()
            )
            if (photoFile.exists()) {
                saveNewPhoto(photoFile.absolutePath)
                return true
            }

            val bmpData = data?.extras?.get("data") as? Bitmap
            if (bmpData == null) {
                fragment.context?.showError("Не удалось сделать фото.")
                return false
            }

            val photo = saveNewPhoto(bmpData)

            if (photo == null) {
                fragment.context?.showError("Не удалось сделать фото.")
                return false
            }

            return true
        }
        return false
    }

    private fun saveNewPhoto(path: String): File? {
        try {
            val bmp = BitmapFactory.decodeFile(path)
            return saveNewPhoto(bmp)
        } catch (e: Throwable) {
            fragment.context?.showError("Не удалось сохранить фотографию. Недостаточно памяти. Попробуйте сделать снимок еще раз. Если проблема повторится перезагрузите телефон.")
            e.printStackTrace()
        }

        return null
    }

    private fun saveNewPhoto(bmp: Bitmap?): File? {
        val photoFile = PathHelper.getEntrancePhotoFile(
            fragment.taskItem,
            fragment.entrance,
            photoUUID ?: UUID.randomUUID()
        )
        if (bmp != null) {
            val photo: Bitmap
            try {
                photo = ImageUtils.resizeBitmap(bmp, 1024f, 768f)
                bmp.recycle()
            } catch (e: Throwable) {
                fragment.context?.showError("Не удалось сохранить фотографию. Недостаточно памяти. Попробуйте сделать снимок еще раз. Если проблема повторится перезагрузите телефон.")
                e.printStackTrace()
                bmp.recycle()
                return null
            }

            try {
                ImageUtils.saveImage(photo, photoFile, fragment.context?.contentResolver)
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }

            photo.recycle()
        }
        bgScope.launch(Dispatchers.Main) {
            val currentGPS = application().currentLocation
            val photoModel = EntrancePhotoModel(
                0,
                photoUUID.toString(),
                fragment.taskItem,
                fragment.entrance,
                currentGPS,
                null,
                photoEntrance
            )

            val id = application().tasksRepository.savePhoto(photoModel)

            if (photoEntrance) {
                fragment.allTaskItems.forEach { taskItem ->
                    taskItem.entrances.firstOrNull {
                        it.number == fragment.entrance.number
                                && taskItem != fragment.taskItem
                                && it.state != Entrance.CLOSED
                    }?.let {
                        application().tasksRepository.savePhoto(
                            photoModel.copy(
                                taskItem = taskItem,
                                entrance = it,
                                realPath = photoModel.URI.path
                            )
                        )
                    }
                }
            }
            val savedPhoto = photoModel.copy(id = id.toInt())

            fragment.photosAdapter.data.add(ReportPhotosListModel.TaskItemPhoto(savedPhoto))
            fragment.photosAdapter.notifyItemRangeChanged(fragment.photosAdapter.data.size - 1, 2)

            if (photoMultiMode) {
                requestPhoto()
            }

            onPhotoCreated()
        }

        return photoFile
    }

    private fun onPhotoCreated() {
        fragment.updateEditable()
    }


    fun onDescriptionChanged() {
        val description = fragment.user_explanation_input?.text.toString()
        bgScope.launch {
            application().tasksRepository.insertEntranceResult(
                fragment.taskItem,
                fragment.entrance,
                description = description
            )
        }
    }

    fun onCodeChanged() {
        val code = fragment.entrance_code?.text.toString()
        bgScope.launch {
            fragment.allTaskItems.forEach {
                application().tasksRepository.insertEntranceResult(
                    it,
                    fragment.entrance,
                    code = code
                )
            }
        }
    }

    fun onApartmentIntervalChanged() {
        val from = try {
            fragment.appartaments_from?.text.toString().toInt()
        } catch (e: Exception) {
            return
        }
        val to = try {
            fragment.appartaments_to?.text.toString().toInt()
        } catch (e: Exception) {
            return
        }

        if (from < 1) {
            fragment.appartaments_from?.setText(
                (fragment.saved?.apartmentFrom ?: fragment.entrance.startApartments).toString()
            )
            return
        }
        if (from > to) {
            fragment.appartaments_from?.setText(
                (fragment.saved?.apartmentFrom ?: fragment.entrance.startApartments).toString()
            )
            return
        }
        if (to < from) {
            fragment.appartaments_to?.setText(
                (fragment.saved?.apartmentTo ?: fragment.entrance.endApartments).toString()
            )
            return
        }

        if (fragment.saved == null) {
            fragment.saved = EntranceResultEntity(
                id = 0,
                taskId = fragment.taskItem.taskId,
                taskItemId = fragment.taskItem.id,
                entranceNumber = fragment.entrance.number,
                apartmentTo = to,
                apartmentFrom = from,
                euroKey = null,
                mailboxType = null,
                key = null,
                hasLookupPost = null,
                isDeliveryWrong = null,
                floors = null,
                description = null,
                code = null,
                entranceClosed = null
            )
        } else {
            fragment.saved?.apartmentFrom = from
            fragment.saved?.apartmentTo = to
        }

        fragment.fillApartmentList()

        changeApartmentInterval(fragment.entrance, from, to)
    }

    private fun changeApartmentInterval(entrance: Entrance, from: Int, to: Int) {
        Log.d("Test Apps", "$entrance changed")
        var toApartment = to
        var fromApartment = from
        if (fromApartment > toApartment) {
            toApartment = fromApartment + 1
        }
        if (toApartment < fromApartment) {
            fromApartment = toApartment - 1
        }

        fragment.taskItem.entrances.indexOf(entrance).takeIf { it > 0 }?.let { entranceIdx ->
            fragment.taskItem.entrances[entranceIdx] = fragment.taskItem.entrances[entranceIdx].copy(
                startApartments = fromApartment,
                endApartments = toApartment
            )
        }

        bgScope.launch {
            fragment.allTaskItems.forEach {
                if(it.entrances.firstOrNull { it.number == entrance.number }?.state != Entrance.CLOSED){
                    application().tasksRepository.insertEntranceResult(
                        it,
                        entrance,
                        apartmentFrom = fromApartment,
                        apartmentTo = toApartment
                    )
                }
            }

            if (entrance.number != fragment.entrance.number) {
                fragment.callback?.onEntranceChanged(entrance)
            }
        }
    }

    fun onRemovePhotoClicked(holder: RecyclerView.ViewHolder) {
        val position = holder.adapterPosition
        if (position >= fragment.photosAdapter.data.size || position < 0) {
            fragment.context?.showError("Невозможно удалить фото.")
            return
        }
        val entrancePhotoModel =
            (fragment.photosAdapter.data[holder.adapterPosition] as ReportPhotosListModel.TaskItemPhoto).photo

        fragment.photosAdapter.data.removeAt(holder.adapterPosition)
        fragment.photosAdapter.notifyItemRemoved(holder.adapterPosition)

        bgScope.launch {
            application().tasksRepository.removePhoto(entrancePhotoModel)
            withContext(Dispatchers.Main){
                fragment.updateEditable()
            }
        }
    }

    fun onFloorsChanged() {
        val floors = try {
            fragment.floors?.text.toString().toInt()
        } catch (e: java.lang.Exception) {
            return
        }

        bgScope.launch {
            fragment.allTaskItems.forEach {
                application().tasksRepository.insertEntranceResult(
                    it,
                    fragment.entrance,
                    floors = floors
                )
            }
        }
    }

    fun onApartmentButtonGroupChanged() {

        val app =
            fragment.apartmentAdapter.data.firstOrNull { it is ApartmentListModel.Apartment } as? ApartmentListModel.Apartment
        app ?: return

        val buttonGroup = if (app.buttonGroup == 0) 1 else 0

        try {
            val data = fragment.apartmentAdapter.data.toList()
            data.forEachIndexed { index, apartmentListModel ->
                if (apartmentListModel !is ApartmentListModel.Apartment) return@forEachIndexed

                apartmentListModel.buttonGroup = buttonGroup
            }
        } catch (e: java.lang.Exception) {
            e.logError()
            return
        }

        fragment.apartmentListRemoveEntrance()
        fragment.apartmentListRemoveLookout()
        if (buttonGroup == 1) {
            fragment.apartmentListAddEntrance()
        } else if (buttonGroup == 0) {
            if (fragment.hasLookout) {
                fragment.apartmentListAddLookout()
            }
        }
        fragment.updateApartmentListBackground(buttonGroup)
        fragment.apartmentAdapter.notifyDataSetChanged()

        bgScope.launch {
            val data = fragment.apartmentAdapter.data.toList()

            application().tasksRepository.saveApartmentResults(
                fragment.taskItem,
                fragment.entrance,
                data.mapNotNull { it as? ApartmentListModel.Apartment })

            application().tasksRepository.updateTaskItemButtonGroup(
                fragment.taskItem,
                buttonGroup
            )

            fragment.taskItem.entrances.forEach {
                if (it != fragment.entrance)
                    fragment.callback?.onEntranceChanged(it)
            }
        }

        fragment.updateApartmentListTypeButton()
    }

    fun onApartmentButtonStateChanged(apartment: Int, newState: Int) {
        val index = fragment.apartmentAdapter.data.indexOfFirst {
            (it as? ApartmentListModel.Apartment)?.number == apartment
        }
        val item = fragment.apartmentAdapter.data[index] as ApartmentListModel.Apartment
        val newItem = item.copy(state = newState)
        fragment.apartmentAdapter.data[index] = newItem
        fragment.apartmentAdapter.notifyItemChanged(index)

        bgScope.launch {
            application().tasksRepository.saveApartmentResult(
                fragment.taskItem,
                fragment.entrance,
                newItem
            )
        }
    }

    fun onEntranceButtonStateChanged(newState: Int) {
        val index = fragment.apartmentAdapter.data.indexOfFirst {
            it is ApartmentListModel.Entrance
        }
        val item = fragment.apartmentAdapter.data[index] as ApartmentListModel.Entrance
        val newItem = item.copy(state = newState)
        fragment.apartmentAdapter.data[index] = newItem
        fragment.apartmentAdapter.notifyItemChanged(index)

        bgScope.launch {
            application().tasksRepository.saveApartmentResult(
                fragment.taskItem,
                fragment.entrance,
                ApartmentListModel.Apartment(-1, 1, newItem.state)
            )
        }
    }

    fun onLookoutButtonStateChanged(newState: Int) {
        val index = fragment.apartmentAdapter.data.indexOfFirst {
            it is ApartmentListModel.Lookout
        }
        val item = fragment.apartmentAdapter.data[index] as ApartmentListModel.Lookout
        val newItem = item.copy(state = newState)
        fragment.apartmentAdapter.data[index] = newItem
        fragment.apartmentAdapter.notifyItemChanged(index)

        bgScope.launch {
            application().tasksRepository.saveApartmentResult(
                fragment.taskItem,
                fragment.entrance,
                ApartmentListModel.Apartment(-2, 0, newItem.state)
            )
        }
    }

    fun onDeliveryWrongChanged() {
        fragment.deliveryWrong = !fragment.deliveryWrong
        fragment.layout_error_button?.setSelectButtonActive(fragment.deliveryWrong)
        bgScope.launch {
            application().tasksRepository.insertEntranceResult(
                fragment.taskItem,
                fragment.entrance,
                isDeliveryWrong = fragment.deliveryWrong
            )
        }
    }

    fun onLookoutChanged() {
        fragment.hasLookout = !fragment.hasLookout
        fragment.lookout?.setSelectButtonActive(fragment.hasLookout)

        bgScope.launch {
            fragment.allTaskItems.forEach {
                application().tasksRepository.insertEntranceResult(
                    it,
                    fragment.entrance,
                    hasLookupPost = fragment.hasLookout
                )
            }

            val shouldAddLookout =
                (fragment.apartmentAdapter.data.first { it is ApartmentListModel.Apartment } as? ApartmentListModel.Apartment)?.buttonGroup == 0
            if (fragment.hasLookout && shouldAddLookout) {
                fragment.apartmentListAddLookout()
            } else {
                fragment.apartmentListRemoveLookout()
            }

            withContext(Dispatchers.Main) {
                fragment.apartmentAdapter.notifyDataSetChanged()
            }
        }
    }

    fun onEntranceKeyChanged(key: String) {
        bgScope.launch {
            fragment.allTaskItems.forEach {
                application().tasksRepository.insertEntranceResult(it, fragment.entrance, key = key)
            }
        }
    }

    fun onEntranceEuroKeyChanged(key: String) {
        bgScope.launch {
            fragment.allTaskItems.forEach {
                application().tasksRepository.insertEntranceResult(
                    it,
                    fragment.entrance,
                    euroKey = key
                )
            }
        }
    }

    fun onEntranceMailboxTypeChanged() {
        fragment.mailboxType = if (fragment.mailboxType == 1) 2 else 1
        fragment.updateMailboxTypeText()
        bgScope.launch {
            fragment.allTaskItems.forEach {
                application().tasksRepository.insertEntranceResult(
                    it, fragment.entrance, mailboxType = fragment.mailboxType
                )
            }
        }
    }

    fun onAllApartmentsButtonStateChanged(apartmentNumber: Int, change: Int) {
        val originalApartment = fragment.apartmentAdapter.data.firstOrNull {
            (it as? ApartmentListModel.Apartment)?.number == apartmentNumber
        } as? ApartmentListModel.Apartment ?: return

        val targetState = (originalApartment.state xor change) and change

        fragment.apartmentAdapter.data
            .filter { it is ApartmentListModel.Apartment }
            .forEach {
                val apartment = (it as ApartmentListModel.Apartment)
                if (apartment.state and change != targetState) {
                    apartment.state = apartment.state xor change
                    when (change) {
                        1 -> if (apartment.state and 4 > 0) apartment.state = apartment.state xor 4
                        4 -> if (apartment.state and 1 > 0) apartment.state = apartment.state xor 1
                        16 -> if (apartment.state and 32 > 0) apartment.state =
                            apartment.state xor 32
                        32 -> if (apartment.state and 16 > 0) apartment.state =
                            apartment.state xor 16
                    }
                }
            }

        fragment.apartmentAdapter.notifyDataSetChanged()

        bgScope.launch {
            fragment.apartmentAdapter.data
                .filter { it is ApartmentListModel.Apartment }
                .forEach {
                    application().tasksRepository.saveApartmentResult(
                        fragment.taskItem,
                        fragment.entrance,
                        it as ApartmentListModel.Apartment
                    )
                }
        }
    }

    fun onIsEntranceClosedChanged() {
        fragment.entranceClosed = !fragment.entranceClosed
        fragment.updateEntranceClosed()

        bgScope.launch {
            fragment.allTaskItems.forEach {
                application().tasksRepository.insertEntranceResult(
                    it, fragment.entrance, entranceClosed = fragment.entranceClosed
                )
            }
        }
    }

    fun onApartmentDescriptionClicked(apartment: Int) {
        val index = fragment.apartmentAdapter.data.indexOfFirst {
            (it as? ApartmentListModel.Apartment)?.number == apartment
        }
        val item = fragment.apartmentAdapter.data[index] as ApartmentListModel.Apartment

        val input = EditText(fragment.context).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            setText(item.description)
        }

        AlertDialog.Builder(fragment.context)
            .setTitle("Описание")
            .setView(input)
            .setPositiveButton("Ок") { _, _ ->
                bgScope.launch {
                    item.description = input.text.toString()
                    withContext(Dispatchers.Main) {
                        fragment.apartmentAdapter.notifyItemChanged(index)
                    }
                    application().tasksRepository.saveApartmentResult(
                        fragment.taskItem,
                        fragment.entrance,
                        item
                    )
                }
            }
            .setNegativeButton("Отмена") { _, _ -> }
            .show()
    }

    val bgScope = CancelableScope(Dispatchers.Default)

}