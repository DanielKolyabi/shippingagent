package ru.relabs.kurjercontroller.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.relabs.kurjercontroller.BuildConfig
import ru.relabs.kurjercontroller.ControllApplication
import ru.relabs.kurjercontroller.data.api.ApiProvider
import ru.relabs.kurjercontroller.data.database.AppDatabase
import ru.relabs.kurjercontroller.data.database.migrations.Migrations
import ru.relabs.kurjercontroller.domain.controllers.ServiceEventController
import ru.relabs.kurjercontroller.domain.controllers.TaskEventController
import ru.relabs.kurjercontroller.domain.providers.*
import ru.relabs.kurjercontroller.domain.repositories.ControlRepository
import ru.relabs.kurjercontroller.domain.repositories.DatabaseRepository
import ru.relabs.kurjercontroller.domain.repositories.EntranceMonitoringRepository
import ru.relabs.kurjercontroller.domain.repositories.SettingsRepository
import ru.relabs.kurjercontroller.domain.storage.AppPreferences
import ru.relabs.kurjercontroller.domain.storage.AuthTokenStorage
import ru.relabs.kurjercontroller.domain.storage.CurrentUserStorage
import ru.relabs.kurjercontroller.domain.storage.MapCameraStorage
import ru.relabs.kurjercontroller.domain.useCases.AppUpdateUseCase
import ru.relabs.kurjercontroller.domain.useCases.LoginUseCase
import ru.relabs.kurjercontroller.domain.useCases.OnlineTaskUseCase
import ru.relabs.kurjercontroller.domain.useCases.ReportUseCase
import java.io.File

object Modules {
    val DELIVERY_URL = named("DeliveryUrl")
    val SHARED_PREFERENCES_NAME = named("SharedPrefName")
    val FILES_DIR = named("FilesDir")
    val CACHE_DIR = named("CacheDir")
    val APP_CONTEXT = named("AppContext")
}

val constModule = module {
    single<Context>(Modules.APP_CONTEXT) { androidApplication().applicationContext }
    single<String>(Modules.DELIVERY_URL) { BuildConfig.API_URL }
    single<String>(Modules.SHARED_PREFERENCES_NAME) { BuildConfig.APPLICATION_ID }
}

val navigationModule = module {
    single<Router> { (androidApplication() as ControllApplication).router }
    single<NavigatorHolder> { (androidApplication() as ControllApplication).navigatorHolder }
}

val fileSystemModule = module {
    single<File>(Modules.FILES_DIR) { androidApplication().filesDir }
    single<File>(Modules.CACHE_DIR) { androidApplication().cacheDir }
}

val storagesModule = module {
    single<SharedPreferences> {
        androidApplication().getSharedPreferences(
            get(Modules.SHARED_PREFERENCES_NAME),
            Context.MODE_PRIVATE
        )
    }
    single<AppPreferences> { AppPreferences(get<SharedPreferences>()) }
    single<AuthTokenStorage> { AuthTokenStorage(get<AppPreferences>()) }
    single<CurrentUserStorage> { CurrentUserStorage(get<AppPreferences>()) }
    single<MapCameraStorage> { MapCameraStorage() }

    single<DeviceUUIDProvider> {
        DeviceUUIDProvider(
            get<AppPreferences>()
        )
    }

    single<DeviceUniqueIdProvider> {
        DeviceUniqueIdProvider(androidApplication())
    }

    single<FirebaseTokenProvider> {
        FirebaseTokenProvider(get<AppPreferences>())
    }

    single<LocationProvider> {
        getLocationProvider(androidApplication(), CoroutineScope(Dispatchers.Main))
    }

    single<AppDatabase> {
        Room.databaseBuilder(androidApplication(), AppDatabase::class.java, "deliveryman")
            .addMigrations(*Migrations.getMigrations())
            .fallbackToDestructiveMigration()
            .build()
    }

    single<ApiProvider> {
        ApiProvider(get<String>(Modules.DELIVERY_URL))
    }

    single<PathsProvider> {
        PathsProvider(
            get<File>(Modules.FILES_DIR)
        )
    }
}


val repositoryModule = module {

    single<DatabaseRepository> {
        DatabaseRepository(
            get<AppDatabase>(),
            get<AuthTokenStorage>(),
            get<String>(Modules.DELIVERY_URL),
            get<PathsProvider>()
        )
    }
    single<ControlRepository> {
        ControlRepository(
            get<ApiProvider>().practisApi,
            get<AuthTokenStorage>(),
            get<DeviceUUIDProvider>(),
            get<DeviceUniqueIdProvider>(),
            get<FirebaseTokenProvider>(),
            get<AppDatabase>(),
            get<ApiProvider>().httpClient,
            get<PathsProvider>()
        )
    }
    single<SettingsRepository> {
        SettingsRepository(
            get<ControlRepository>(),
            get<SharedPreferences>()
        )
    }
    single<EntranceMonitoringRepository> {
        EntranceMonitoringRepository(
            get<AppDatabase>(),
            get<DatabaseRepository>(),
            get<SharedPreferences>(),
            get<CurrentUserStorage>()
        )
    }
}
val useCasesModule = module {
    single<LoginUseCase> {
        LoginUseCase(
            get<ControlRepository>(),
            get<CurrentUserStorage>(),
            get<DatabaseRepository>(),
            get<AuthTokenStorage>(),
            get<AppPreferences>(),
            get<SettingsRepository>(),
            get<EntranceMonitoringRepository>()
        )
    }

    single<AppUpdateUseCase> {
        AppUpdateUseCase(
            get<ControlRepository>(),
            get<PathsProvider>()
        )
    }

    single<ReportUseCase> {
        ReportUseCase(
            get<DatabaseRepository>(),
            get<AuthTokenStorage>(),
            get<TaskEventController>(),
            get<PathsProvider>(),
            get<SettingsRepository>(),
            get<EntranceMonitoringRepository>()
        )
    }

    single<OnlineTaskUseCase> {
        OnlineTaskUseCase(
            get<ControlRepository>(),
            get<DatabaseRepository>()
        )
    }
}

val eventControllers = module {
    single<TaskEventController> {
        TaskEventController()
    }
    single<ServiceEventController> {
        ServiceEventController()
    }
}

