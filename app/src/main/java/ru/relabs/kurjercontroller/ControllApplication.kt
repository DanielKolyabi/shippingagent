package ru.relabs.kurjercontroller

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
}