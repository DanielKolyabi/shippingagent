package ru.relabs.kurjercontroller.application

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.StrictMode
import androidx.core.content.ContextCompat
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.firebase.iid.FirebaseInstanceId
import com.yandex.mapkit.MapKitFactory
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.BuildConfig
import ru.relabs.kurjercontroller.database.AppDatabase
import ru.relabs.kurjercontroller.models.GPSCoordinatesModel
import ru.relabs.kurjercontroller.network.DeliveryServerAPI
import ru.relabs.kurjercontroller.providers.TaskRepository
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

    var lastRequiredAppVersion = 0

    var locationManager: FusedLocationProviderClient? = null
    var currentLocation = GPSCoordinatesModel(0.0, 0.0, DateTime(0))
    lateinit var deviceUUID: String

    val listener = object : LocationCallback() {
        override fun onLocationResult(location: LocationResult?) {
            location?.let {
                currentLocation = GPSCoordinatesModel(
                    it.lastLocation.latitude,
                    it.lastLocation.longitude,
                    DateTime(it.lastLocation.time)
                )
            }
        }
    }

    val user = UserCredentials(this)

    lateinit var database: AppDatabase

    lateinit var tasksRepository: TaskRepository

    override fun onCreate() {
        super.onCreate()
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().build())
        instance = this
        cicerone = Cicerone.create()
        deviceUUID = getOrGenerateDeviceUUID()

        val migration_36_37 = object : Migration(36, 37) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE task_items ADD COLUMN is_new INTEGER NOT NULL DEFAULT 0")
            }
        }
        val migration_37_38 = object : Migration(37, 38) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE task_items ADD COLUMN wrong_method INTEGER NOT NULL DEFAULT 0")
            }
        }
        val migration_38_39 = object : Migration(38, 39) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE tasks ADD COLUMN with_planned INTEGER NOT NULL DEFAULT 0")
            }
        }
        val migration_39_40 = object : Migration(39, 40) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE task_items ADD COLUMN button_name TEXT NOT NULL DEFAULT ''")
            }
        }
        val migration_40_41 = object : Migration(40, 41) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE task_items ADD COLUMN required_apartments TEXT NOT NULL DEFAULT ''")
            }
        }


        database = Room
            .databaseBuilder(applicationContext, AppDatabase::class.java, "deliverycontroller")
            .addMigrations(migration_36_37, migration_37_38, migration_38_39, migration_39_40, migration_40_41)
            .build()
        tasksRepository = TaskRepository(database)
//        GlobalScope.launch(Dispatchers.IO) {
//            database.clearAllTables()
//        }

        MapKitFactory.setApiKey(BuildConfig.YA_KEY)
    }

    fun enableLocationListening(time: Long = 30 * 1000, distance: Float = 10f): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        locationManager = FusedLocationProviderClient(applicationContext)

        val req = LocationRequest().apply {
            fastestInterval = time
            smallestDisplacement = distance
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }

        locationManager?.requestLocationUpdates(req, listener, mainLooper)

        return true
    }

    fun getOrGenerateDeviceUUID(): String {
        val sharedPreferences =
            getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)

        return sharedPreferences.getString(
            "device_uuid", "unknown"
        ).takeIf { it != "unknown" } ?: run {
            val uuid = UUID.randomUUID().toString()
            sharedPreferences.edit()
                .putString("device_uuid", uuid)
                .apply()
            return@run uuid
        }
    }

    fun disableLocationListening() {
        //locationManager?.removeUpdates(listener)
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
                DeliveryServerAPI.api.sendPushToken(
                    (user.getUserCredentials() as UserModel.Authorized).token,
                    pushToken
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            val token =
                getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE).getString(
                    "firebase_token",
                    "notoken"
                )
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

    fun requestLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        locationManager?.lastLocation?.addOnSuccessListener { location ->
            location?.let {
                currentLocation = GPSCoordinatesModel(it.latitude, it.longitude, DateTime(it.time))
            }
        }
    }

    companion object {
        lateinit var instance: MyApplication
    }
}