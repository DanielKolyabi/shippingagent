package ru.relabs.kurjercontroller.network

import android.content.Context
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.net.ConnectivityManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.provider.Settings
import android.util.Log
import android.webkit.MimeTypeMap
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.database.entities.EntrancePhotoEntity
import ru.relabs.kurjercontroller.database.entities.EntranceReportEntity
import ru.relabs.kurjercontroller.fileHelpers.PathHelper
import ru.relabs.kurjercontroller.logError
import ru.relabs.kurjercontroller.network.DeliveryServerAPI.api
import ru.relabs.kurjercontroller.network.models.PhotoReportModel
import ru.relabs.kurjercontroller.network.models.TaskItemReportModel
import ru.relabs.kurjercontroller.ui.activities.MainActivity
import ru.relabs.kurjercontroller.ui.activities.REQUEST_LOCATION
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL


/**
 * Created by ProOrange on 05.09.2018.
 */

object NetworkHelper {
    private fun isWifiEnabled(context: Context?): Boolean {
        context ?: return false
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            ?: return false
        return wifiManager.isWifiEnabled
    }

    fun isAirplaneModeEnabled(context: Context?): Boolean {
        context ?: return false
        return Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
    }

    private fun isWifiConnected(context: Context?): Boolean {
        context ?: return false
        val wifiManager =
            context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
        return wifiManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected
    }

    private fun isMobileDataEnabled(context: Context?): Boolean {
        context ?: return false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return Settings.Secure.getInt(context.contentResolver, "mobile_data", 0) == 1 || cm.getNetworkInfo(
            ConnectivityManager.TYPE_MOBILE
        ).isConnectedOrConnecting
    }

    fun isNetworkEnabled(context: Context?): Boolean {
        return ((isWifiEnabled(context) && isWifiConnected(context)) || isMobileDataEnabled(context)) && !isAirplaneModeEnabled(context)
    }


    fun isNetworkAvailable(context: Context?): Boolean {
        context ?: return false
        val status =
            (context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)?.activeNetworkInfo?.isConnectedOrConnecting
        return (status ?: false) && !isAirplaneModeEnabled(context)
    }

    fun loadUpdateFile(url: URL, onDownloadUpdate: (current: Int, total: Int) -> Unit): File {
        val file = PathHelper.getUpdateFile()
        val connection = url.openConnection() as HttpURLConnection
        val stream = url.openStream()
        val fos = FileOutputStream(file)
        try {
            val fileSize = connection.contentLength


            val b = ByteArray(2048)

            var read = stream.read(b)
            var total = read
            while (read != -1) {
                fos.write(b, 0, read)
                read = stream.read(b)
                total += read
                Log.d("loader", "$total/$fileSize")
                onDownloadUpdate(total, fileSize)
            }
        } catch (e: Exception) {
            throw e
        } finally {
            stream.close()
            connection.disconnect()
            fos.close()
        }


        return file
    }


    suspend fun sendReport(data: EntranceReportEntity, photos: List<EntrancePhotoEntity>): Boolean {

        val photosMap = mutableMapOf<String, PhotoReportModel>()
        val photoParts = mutableListOf<MultipartBody.Part>()

        var imgCount = 0
        photos.forEachIndexed { i, photo ->
            try {
                photoParts.add(photoEntityToPart("img_$imgCount", data, photo))
                photosMap["img_$imgCount"] = PhotoReportModel("", photo.gps)
                imgCount++
            } catch (e: Throwable) {
                e.logError()
            }
        }

        val reportObject = TaskItemReportModel(
            data.taskId, data.taskItemId, data.idnd, data.entranceNumber,
            data.startAppartaments, data.endAppartaments, data.floors,
            data.description, data.code, data.key,
            data.euroKey, data.isDeliveryWrong, data.hasLookupPost,
            data.token, data.apartmentResult, data.closeTime,
            photosMap, data.publisherId, data.mailboxType,
            data.gpsLat, data.gpsLong, data.gpsTime,
            data.entranceClosed
        )

        return api.sendTaskReport(data.taskItemId, data.token, reportObject, photoParts).await().status
    }

    private fun photoEntityToPart(
        partName: String,
        reportEnt: EntranceReportEntity,
        photoEnt: EntrancePhotoEntity
    ): MultipartBody.Part {
        val photoFile = PathHelper.getEntrancePhotoFileByID(
            reportEnt.taskItemId,
            photoEnt.entranceNumber,
            photoEnt.UUID
        )
        if (!photoFile.exists()) {
            throw FileNotFoundException(photoFile.path)
        }
        val extension = Uri.fromFile(photoFile).toString().split(".").last()
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)

        val requestFile = RequestBody.create(
            MediaType.parse(mime),
            photoFile
        )
        return MultipartBody.Part.createFormData(partName, photoFile.name, requestFile)
    }

    fun isGPSEnabled(context: Context?): Boolean{
        return (context?.getSystemService(Context.LOCATION_SERVICE) as? LocationManager)?.isProviderEnabled(GPS_PROVIDER) ?: false
    }

    fun displayLocationSettingsRequest(context: Context, activity: MainActivity) {
        val googleApiClient = GoogleApiClient.Builder(context)
            .addApi(LocationServices.API).build()
        googleApiClient.connect()

        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 30000
        locationRequest.fastestInterval = 15000
        locationRequest.smallestDisplacement = 10f

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        val result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback { result ->
            val status = result.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> Log.i("NetworkHelper", "All location settings are satisfied.")
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    Log.i("NetworkHelper", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ")
                    status.startResolutionForResult(activity, REQUEST_LOCATION)
                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> Log.i("NetworkHelper", "Location settings are inadequate, and cannot be fixed here. Dialog not created.")
            }
        }
    }

}