package ru.relabs.kurjercontroller.application

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.models.GPSCoordinatesModel
import ru.relabs.kurjercontroller.providers.MockTaskRepository
import ru.relabs.kurjercontroller.providers.interfaces.ITaskRepository
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router

/**
 * Created by ProOrange on 18.03.2019.
 */

class MyApplication : Application() {
    private var locationManager: LocationManager? = null
    var currentLocation = GPSCoordinatesModel(0.0, 0.0, DateTime(0))
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

    private lateinit var cicerone: Cicerone<Router>
    val router: Router
        get() = cicerone.router
    val navigatorHolder: NavigatorHolder
        get() = cicerone.navigatorHolder

    lateinit var tasksLocalRepository: ITaskRepository

    override fun onCreate() {
        super.onCreate()
        instance = this
        tasksLocalRepository = MockTaskRepository()
        cicerone = Cicerone.create()
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

    fun disableLocationListening() {
        locationManager?.removeUpdates(listener)
    }

    companion object {
        lateinit var instance: MyApplication
    }
}