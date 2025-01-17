package ru.relabs.kurjercontroller.domain.providers

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.media.MediaDrm
import android.os.Build
import android.os.Parcelable
import android.telephony.TelephonyManager
import kotlinx.android.parcel.Parcelize
import ru.relabs.kurjercontroller.utils.CustomLog
import java.util.*

@Parcelize
data class DeviceUniqueID(val id: String): Parcelable

class DeviceUniqueIdProvider(
    val application: Application
){
    private var imei: String? = null

    @SuppressLint("HardwareIds")
    fun get(): DeviceUniqueID {
        val ctxImei = imei
        if(ctxImei == null){
            val telephonyManager = application.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val newImei = try {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                        CustomLog.writeToFile("[IMEI] Generate via DRM")

                        val widevineUUID = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L)
                        val id =
                            MediaDrm(widevineUUID).getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)
                        Base64.getEncoder().encodeToString(id)
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->{
                        CustomLog.writeToFile("[IMEI] Generate via IMEI NEW")
                        telephonyManager.getImei(0) ?: telephonyManager.getImei(1) ?: ""
                    }

                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                        CustomLog.writeToFile("[IMEI] Generate via IMEI OLD")
                        telephonyManager.getDeviceId(0) ?: (telephonyManager.getDeviceId(1) ?: "")
                    }
                    else -> {
                        CustomLog.writeToFile("[IMEI] Generate via deviceId")
                        telephonyManager.deviceId
                    }
                }
            } catch (e: SecurityException) {
                CustomLog.writeToFile("IMEI: Stack \n" + CustomLog.getStacktraceAsString(e))
                ""
            }
            imei = newImei

            CustomLog.writeToFile("[IMEI] Generated $newImei")
            return DeviceUniqueID(newImei)
        }else{
            CustomLog.writeToFile("[IMEI] Get stored IMEI $ctxImei ")
            return DeviceUniqueID(ctxImei)
        }
    }
}