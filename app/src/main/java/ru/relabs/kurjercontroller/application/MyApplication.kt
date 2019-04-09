package ru.relabs.kurjercontroller.application

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.BuildConfig
import ru.relabs.kurjercontroller.models.GPSCoordinatesModel
import ru.relabs.kurjercontroller.network.DeliveryServerAPI
import ru.relabs.kurjercontroller.providers.MockTaskRepository
import ru.relabs.kurjercontroller.providers.interfaces.ITaskRepository
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import java.util.*

/**
 * Created by ProOrange on 18.03.2019.
 */

class MyApplication : Application() {

    private lateinit var cicerone: Cicerone<Router>
    val router: Router
        get() = cicerone.router
    val navigatorHolder: NavigatorHolder
        get() = cicerone.navigatorHolder

    private var locationManager: LocationManager? = null
    var currentLocation = GPSCoordinatesModel(0.0, 0.0, DateTime(0))
    lateinit var deviceUUID: String
    private val listener = object : LocationListener {
        override fun onLocationChanged(location: Location?) {
            location?.let {
                currentLocation = GPSCoordinatesModel(it.latitude, it.longitude, DateTime(it.time))
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String?) {}
        override fun onProviderDisabled(provider: String?) {}
    }

    val user = UserCredentials(this)

    lateinit var tasksLocalRepository: ITaskRepository

    override fun onCreate() {
        super.onCreate()
        instance = this
        tasksLocalRepository = MockTaskRepository()
        cicerone = Cicerone.create()
        deviceUUID = getOrGenerateDeviceUUID()
    }

    fun enableLocationListening(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30 * 1000, 10f, listener)
        locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30 * 1000, 10f, listener)

        return true
    }

    fun getOrGenerateDeviceUUID(): String {
        val sharedPreferences = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
        var deviceUUID = sharedPreferences.getString(
            "device_uuid", "unknown"
        )

        if (deviceUUID == "unknown") {
            deviceUUID = UUID.randomUUID().toString()
            sharedPreferences.edit()
                .putString("device_uuid", deviceUUID)
                .apply()
        }
        return deviceUUID
    }

    fun disableLocationListening() {
        locationManager?.removeUpdates(listener)
    }

    fun savePushToken(pushToken: String) {
        getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
            .edit()
            .putString("firebase_token", pushToken)
            .apply()

    }

    fun sendPushToken(pushToken: String?) {
        if (user.getUserCredentials() !is UserModel.Authorized) return

        if (pushToken != null) {
            try {
                DeliveryServerAPI.api.sendPushToken((user as UserModel.Authorized).token, pushToken)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            val token = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE).getString("firebase_token", "notoken")
            if (token != "notoken") {
                sendPushToken(token)
                return
            }

            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
                savePushToken(it.token)
                sendPushToken(it.token)
            }
        }
    }

    companion object {
        lateinit var instance: MyApplication
    }
}