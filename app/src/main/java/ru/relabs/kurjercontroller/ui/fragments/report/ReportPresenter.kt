package ru.relabs.kurjercontroller.ui.fragments.report

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_report.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.relabs.kurjer.files.ImageUtils
import ru.relabs.kurjercontroller.CancelableScope
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.database.entities.EntranceResultEntity
import ru.relabs.kurjercontroller.fileHelpers.PathHelper
import ru.relabs.kurjercontroller.models.EntranceModel
import ru.relabs.kurjercontroller.models.EntrancePhotoModel
import ru.relabs.kurjercontroller.ui.activities.showError
import ru.relabs.kurjercontroller.ui.extensions.setSelectButtonActive
import ru.relabs.kurjercontroller.ui.fragments.report.models.ApartmentListModel
import ru.relabs.kurjercontroller.ui.fragments.report.models.ReportPhotosListModel
import java.io.File
import java.util.*

/**
 * Created by ProOrange on 18.03.2019.
 */

const val REQUEST_PHOTO = 1

class ReportPresenter(val fragment: ReportFragment) {
    lateinit var photoUUID: UUID
    var photoMultiMode: Boolean = false

    fun onBlankMultiPhotoClicked() {
        requestPhoto()
    }

    fun onBlankPhotoClicked() {
        requestPhoto(false)
    }

    private fun requestPhoto(multiPhoto: Boolean = true) {
        photoUUID = UUID.randomUUID()
        photoMultiMode = multiPhoto
        val photoFile = PathHelper.getEntrancePhotoFile(fragment.taskItem, fragment.entrance, photoUUID)

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
            val photoFile = PathHelper.getEntrancePhotoFile(fragment.taskItem, fragment.entrance, photoUUID)
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
        val photoFile = PathHelper.getEntrancePhotoFile(fragment.taskItem, fragment.entrance, photoUUID)
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
            val photoModel =
                EntrancePhotoModel(0, photoUUID.toString(), fragment.taskItem, fragment.entrance, currentGPS)

            val id = application().tasksRepository.savePhoto(photoModel)
            val savedPhoto =
                EntrancePhotoModel(id.toInt(), photoUUID.toString(), fragment.taskItem, fragment.entrance, currentGPS)


            fragment.photosAdapter.data.add(
                ReportPhotosListModel.TaskItemPhoto(savedPhoto)
            )
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
                application().tasksRepository.insertEntranceResult(it, fragment.entrance, code = code)
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

    private fun changeApartmentInterval(entrance: EntranceModel, from: Int, to: Int) {
        var toApartment = to
        var fromApartment = from
        if (fromApartment > toApartment) {
            toApartment = fromApartment + 1
        }
        if (toApartment < fromApartment) {
            fromApartment = toApartment - 1
        }

        bgScope.launch {
            fragment.allTaskItems.forEach {
                application().tasksRepository.insertEntranceResult(
                    it,
                    entrance,
                    apartmentFrom = fromApartment,
                    apartmentTo = toApartment
                )
            }

            if (entrance.number != fragment.entrance.number) {
                fragment.callback?.onEntranceChanged(entrance)
            }
        }

        fragment.taskItem.entrances.firstOrNull {
            it.number == entrance.number - 1
        }?.let { prevEntrance ->
            if (prevEntrance.endApartments >= fromApartment) {
                changeApartmentInterval(prevEntrance, prevEntrance.startApartments, fromApartment - 1)
            }
        }
        fragment.taskItem.entrances.firstOrNull {
            it.number == entrance.number + 1
        }?.let { nextEntrance ->
            if (nextEntrance.startApartments <= toApartment) {
                changeApartmentInterval(nextEntrance, toApartment + 1, nextEntrance.endApartments)
            }
        }
    }

    fun onRemovePhotoClicked(holder: RecyclerView.ViewHolder) {
        val position = holder.adapterPosition
        if (position >= fragment.photosAdapter.data.size ||
            position < 0
        ) {

            fragment.context?.showError("Невозможно удалить фото.")
            return
        }
        val status =
            File((fragment.photosAdapter.data[holder.adapterPosition] as ReportPhotosListModel.TaskItemPhoto).photo.URI.path).delete()
        if (!status) {
            fragment.context?.showError("Невозможно удалить фото из памяти.")
        }
        val entrancePhotoModel =
            (fragment.photosAdapter.data[holder.adapterPosition] as ReportPhotosListModel.TaskItemPhoto).photo
        bgScope.launch {
            application().tasksRepository.removePhoto(entrancePhotoModel)
        }

        fragment.photosAdapter.data.removeAt(holder.adapterPosition)
        fragment.photosAdapter.notifyItemRemoved(holder.adapterPosition)
        fragment.updateEditable()
    }

    fun onFloorsChanged() {
        val floors = try {
            fragment.floors?.text.toString().toInt()
        } catch (e: java.lang.Exception) {
            return
        }

        bgScope.launch {
            fragment.allTaskItems.forEach {
                application().tasksRepository.insertEntranceResult(it, fragment.entrance, floors = floors)
            }
        }
    }

    fun onApartmentButtonGroupChanged(apartment: Int, buttonGroup: Int) {
        val index = fragment.apartmentAdapter.data.indexOfFirst {
            (it as? ApartmentListModel.Apartment)?.number == apartment
        }
        val item = fragment.apartmentAdapter.data[index] as ApartmentListModel.Apartment
        val newItem = item.copy(buttonGroup = buttonGroup)
        fragment.apartmentAdapter.data[index] = newItem

        bgScope.launch {
            application().tasksRepository.saveApartmentResult(fragment.taskItem, fragment.entrance, newItem)
        }
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
            application().tasksRepository.saveApartmentResult(fragment.taskItem, fragment.entrance, newItem)
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
                ApartmentListModel.Apartment(-2, 1, newItem.state)
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

    fun onLookupChanged() {
        fragment.hasLookup = !fragment.hasLookup
        fragment.lookout?.setSelectButtonActive(fragment.hasLookup)

        bgScope.launch {
            fragment.allTaskItems.forEach {
                application().tasksRepository.insertEntranceResult(
                    it,
                    fragment.entrance,
                    hasLookupPost = fragment.hasLookup
                )
            }

            if (fragment.hasLookup) {
                fragment.apartmentListAddLookout()
            } else {
                fragment.apartmentListRemoveLookout()
            }

            withContext(Dispatchers.Main){
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
                application().tasksRepository.insertEntranceResult(it, fragment.entrance, euroKey = key)
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
                        16 -> if (apartment.state and 32 > 0) apartment.state = apartment.state xor 32
                        32 -> if (apartment.state and 16 > 0) apartment.state = apartment.state xor 16
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

    val bgScope = CancelableScope(Dispatchers.Default)

}