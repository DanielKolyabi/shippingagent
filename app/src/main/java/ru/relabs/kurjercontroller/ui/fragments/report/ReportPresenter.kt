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
import ru.relabs.kurjer.files.ImageUtils
import ru.relabs.kurjercontroller.CancelableScope
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.fileHelpers.PathHelper
import ru.relabs.kurjercontroller.models.EntrancePhotoModel
import ru.relabs.kurjercontroller.ui.activities.showError
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

            fragment.photosAdapter.data.add(
                ReportPhotosListModel.TaskItemPhoto(photoModel)
            )
            fragment.photosAdapter.notifyItemRangeChanged(fragment.photosAdapter.data.size - 1, 2)

            application().tasksRepository.savePhoto(photoModel)
            if (photoMultiMode) {
                requestPhoto()
            }
        }

        return photoFile
    }


    fun onRemovePhotoClicked(holder: RecyclerView.ViewHolder) {val position = holder.adapterPosition
        if (position >= fragment.photosAdapter.data.size ||
            position < 0) {

            fragment.context?.showError("Невозможно удалить фото.")
            return
        }
        val status = File((fragment.photosAdapter.data[holder.adapterPosition] as ReportPhotosListModel.TaskItemPhoto).photo.URI.path).delete()
        if (!status) {
            fragment.context?.showError("Невозможно удалить фото из памяти.")
        }
        val entrancePhotoModel = (fragment.photosAdapter.data[holder.adapterPosition] as ReportPhotosListModel.TaskItemPhoto).photo
        bgScope.launch {
            application().tasksRepository.removePhoto(entrancePhotoModel)
        }

        fragment.photosAdapter.data.removeAt(holder.adapterPosition)
        fragment.photosAdapter.notifyItemRemoved(holder.adapterPosition)
    }

    fun onApartmentButtonGroupChanged(apartment: Int, buttonGroup: Int) {
        val index = fragment.apartmentAdapter.data.indexOfFirst {
            (it as? ApartmentListModel.Apartment)?.number == apartment
        }
        val item = fragment.apartmentAdapter.data[index] as ApartmentListModel.Apartment
        fragment.apartmentAdapter.data[index] = item.copy(buttonGroup = buttonGroup)
    }

    val bgScope = CancelableScope(Dispatchers.Default)

}