package ru.relabs.kurjercontroller.application

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.StrictMode
import com.instacart.library.truetime.TrueTime
import com.yandex.mapkit.MapKitFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.relabs.kurjercontroller.BuildConfig
import ru.relabs.kurjercontroller.utils.CustomLog
import ru.relabs.kurjercontroller.di.*
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router

/**
 * Created by ProOrange on 18.03.2019.
 */

class ControllApplication : Application() {
    private val scope = CoroutineScope(Dispatchers.Default)

    private lateinit var cicerone: Cicerone<Router>

    val router: Router
        get() = cicerone.router
    val navigatorHolder: NavigatorHolder
        get() = cicerone.navigatorHolder

    override fun onCreate() {
        super.onCreate()

        appContext = this
        MapKitFactory.setApiKey(BuildConfig.YA_KEY)

        cicerone = Cicerone.create()

        launchTrueTimeInit()

        startKoin {
            androidContext(this@ControllApplication)
            modules(
                listOf(
                    constModule,
                    navigationModule,
                    fileSystemModule,
                    eventControllers,
                    storagesModule,
                    repositoryModule,
                    useCasesModule
                )
            )
        }

        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().build())
    }

    private fun launchTrueTimeInit() {
        TrueTime.clearCachedInfo()
        scope.launch(Dispatchers.IO) {
            while (!initTrueTime()) {
                delay(500)
            }
            CustomLog.writeToFile("True Time initialized")
        }
    }

    @SuppressLint("HardwareIds")
    private fun initTrueTime(): Boolean {
        return try {
            TrueTime
                .build()
                .initialize()
            true
        } catch (e: Exception) {
            false
        }
    }
    companion object {
        lateinit var appContext: Context
    }

//
//    val listener = object : LocationCallback() {
//        override fun onLocationResult(location: LocationResult?) {
//            location?.let {
//                currentLocation = GPSCoordinatesModel(
//                    it.lastLocation.latitude,
//                    it.lastLocation.longitude,
//                    DateTime(it.lastLocation.time)
//                )
//            }
//        }
//    }
//
//    val user = UserCredentials(this)
//
//    lateinit var database: AppDatabase
//
//    lateinit var tasksRepository: TaskRepository
//
//    override fun onCreate() {
//        super.onCreate()
//        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().build())
//        instance = this
//        cicerone = Cicerone.create()
//        deviceUUID = getOrGenerateDeviceUUID()
//
//
//        database = Room
//            .databaseBuilder(applicationContext, AppDatabase::class.java, "deliverycontroller")
//            .addMigrations(migration_36_37, migration_37_38, migration_38_39, migration_39_40, migration_40_41, migration_42_43, migration_43_44)
//            .fallbackToDestructiveMigration()
//            .build()
//        tasksRepository = TaskRepository(database)
////        GlobalScope.launch(Dispatchers.IO) {
////            database.clearAllTables()
////        }
//
//        MapKitFactory.setApiKey(BuildConfig.YA_KEY)
//    }
//
//    fun enableLocationListening(time: Long = 30 * 1000, distance: Float = 10f): Boolean {
//        if (ContextCompat.checkSelfPermission(
//                this,
//                android.Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            return false
//        }
//
//        locationManager = FusedLocationProviderClient(applicationContext)
//
//        val req = LocationRequest().apply {
//            fastestInterval = time
//            smallestDisplacement = distance
//            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
//        }
//
//        locationManager?.requestLocationUpdates(req, listener, mainLooper)
//
//        return true
//    }
//
//    fun getOrGenerateDeviceUUID(): String {
//        val sharedPreferences =
//            getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
//
//        return sharedPreferences.getString(
//            "device_uuid", "unknown"
//        ).takeIf { it != "unknown" } ?: run {
//            val uuid = UUID.randomUUID().toString()
//            sharedPreferences.edit()
//                .putString("device_uuid", uuid)
//                .apply()
//            return@run uuid
//        }
//    }
//
//    fun disableLocationListening() {
//        //locationManager?.removeUpdates(listener)
//    }
//
//    fun savePushToken(pushToken: String) {
//        getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
//            .edit()
//            .putString("firebase_token", pushToken)
//            .apply()
//
//    }
//
//    fun sendPushToken(pushToken: String?) {
//        if (user.getUserCredentials() !is UserModel.Authorized) return
//
//        if (pushToken != null) {
//            try {
//                DeliveryServerAPI.api.sendPushToken(
//                    (user.getUserCredentials() as UserModel.Authorized).token,
//                    pushToken
//                )
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        } else {
//            val token =
//                getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE).getString(
//                    "firebase_token",
//                    "notoken"
//                )
//            if (token != "notoken") {
//                sendPushToken(token)
//                return
//            }
//
//            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
//                savePushToken(it.token)
//                sendPushToken(it.token)
//            }
//        }
//    }
//
//    fun requestLocation() {
//        if (ContextCompat.checkSelfPermission(
//                this,
//                android.Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            return
//        }
//
//        locationManager?.lastLocation?.addOnSuccessListener { location ->
//            location?.let {
//                currentLocation = GPSCoordinatesModel(it.latitude, it.longitude, DateTime(it.time))
//            }
//        }
//    }
//
//    companion object {
//        lateinit var instance: MyApplication
//    }
}