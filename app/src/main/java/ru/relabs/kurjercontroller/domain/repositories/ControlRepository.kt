package ru.relabs.kurjercontroller.domain.repositories

import android.location.Location
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.joda.time.DateTime
import retrofit2.HttpException
import retrofit2.Response
import ru.relabs.kurjercontroller.data.api.ControlApi
import ru.relabs.kurjercontroller.data.database.AppDatabase
import ru.relabs.kurjercontroller.data.database.entities.EntranceEuroKeyEntity
import ru.relabs.kurjercontroller.data.database.entities.EntranceKeyEntity
import ru.relabs.kurjercontroller.data.database.entities.EntrancePhotoEntity
import ru.relabs.kurjercontroller.data.database.entities.EntranceReportEntity
import ru.relabs.kurjercontroller.data.database.entities.SendQueryItemEntity
import ru.relabs.kurjercontroller.data.models.FiltersRequest
import ru.relabs.kurjercontroller.data.models.PhotoReportRequest
import ru.relabs.kurjercontroller.data.models.SearchFiltersRequest
import ru.relabs.kurjercontroller.data.models.TaskItemReportRequest
import ru.relabs.kurjercontroller.data.models.auth.UserLogin
import ru.relabs.kurjercontroller.data.models.common.ApiError
import ru.relabs.kurjercontroller.data.models.common.ApiErrorContainer
import ru.relabs.kurjercontroller.data.models.common.DomainException
import ru.relabs.kurjercontroller.data.models.common.EitherE
import ru.relabs.kurjercontroller.data.models.tasks.ApartmentResultRequest
import ru.relabs.kurjercontroller.data.models.tasks.FilterResponse
import ru.relabs.kurjercontroller.domain.mappers.FilterTypeMapper
import ru.relabs.kurjercontroller.domain.mappers.SettingsMapper
import ru.relabs.kurjercontroller.domain.mappers.UserLocationMapper
import ru.relabs.kurjercontroller.domain.mappers.database.DatabaseEntrancePhotoMapper
import ru.relabs.kurjercontroller.domain.mappers.network.FilterMapper
import ru.relabs.kurjercontroller.domain.mappers.network.FilteredTasksCountMapper
import ru.relabs.kurjercontroller.domain.mappers.network.FilteredTasksDataMapper
import ru.relabs.kurjercontroller.domain.mappers.network.PasswordMapper
import ru.relabs.kurjercontroller.domain.mappers.network.TaskMapper
import ru.relabs.kurjercontroller.domain.mappers.network.UpdatesMapper
import ru.relabs.kurjercontroller.domain.mappers.network.UserMapper
import ru.relabs.kurjercontroller.domain.models.AppSettings
import ru.relabs.kurjercontroller.domain.models.AppUpdatesInfo
import ru.relabs.kurjercontroller.domain.models.FilterType
import ru.relabs.kurjercontroller.domain.models.FilteredTasksCount
import ru.relabs.kurjercontroller.domain.models.FilteredTasksData
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskFilter
import ru.relabs.kurjercontroller.domain.models.User
import ru.relabs.kurjercontroller.domain.models.UserLocation
import ru.relabs.kurjercontroller.domain.models.getFile
import ru.relabs.kurjercontroller.domain.providers.DeviceUUIDProvider
import ru.relabs.kurjercontroller.domain.providers.DeviceUniqueIdProvider
import ru.relabs.kurjercontroller.domain.providers.FirebaseToken
import ru.relabs.kurjercontroller.domain.providers.FirebaseTokenProvider
import ru.relabs.kurjercontroller.domain.providers.PathsProvider
import ru.relabs.kurjercontroller.domain.storage.AuthTokenStorage
import ru.relabs.kurjercontroller.domain.storage.CurrentUserStorage
import ru.relabs.kurjercontroller.domain.storage.SavedUserStorage
import ru.relabs.kurjercontroller.utils.Either
import ru.relabs.kurjercontroller.utils.Left
import ru.relabs.kurjercontroller.utils.Right
import ru.relabs.kurjercontroller.utils.bind
import ru.relabs.kurjercontroller.utils.debug
import ru.relabs.kurjercontroller.utils.fmap
import ru.relabs.kurjercontroller.utils.log
import java.io.FileNotFoundException

class ControlRepository(
    private val api: ControlApi,
    private val authTokenStorage: AuthTokenStorage,
    private val deviceIdProvider: DeviceUUIDProvider,
    private val deviceUniqueIdProvider: DeviceUniqueIdProvider,
    private val firebaseTokenProvider: FirebaseTokenProvider,
    private val database: AppDatabase,
    private val networkClient: OkHttpClient,
    private val pathsProvider: PathsProvider,
    private val savedUserStorage: SavedUserStorage,
    private val currentUserStorage: CurrentUserStorage,

    ) {
    private var availableEntranceKeys: List<String> = listOf()
    private var availableEntranceEuroKeys: List<String> = listOf()

    fun isAuthenticated(): Boolean = authTokenStorage.getToken() != null

    suspend fun login(login: UserLogin, password: String): EitherE<Pair<User, String>> = anonymousRequest {
        val r = api.login(
            login.login,
            password,
            deviceIdProvider.getOrGenerateDeviceUUID().id,
            currentTime()
        )
        val user = UserMapper.fromRaw(r.user)

        user to r.token
    }

    suspend fun login(token: String): EitherE<Pair<User, String>> = anonymousRequest {
        val r = api.loginByToken(
            token,
            deviceIdProvider.getOrGenerateDeviceUUID().id,
            currentTime()
        )
        val user = UserMapper.fromRaw(r.user)

        user to token
    }

    suspend fun getRemoteTasks(): EitherE<List<Task>> = authenticatedRequest { token ->
        val deviceId = deviceIdProvider.getOrGenerateDeviceUUID()
        val tasks = api.getTasks(token, currentTime())
        tasks.map { TaskMapper.fromRaw(it, deviceId) }
    }

    suspend fun getAppUpdatesInfo(): EitherE<AppUpdatesInfo> = anonymousRequest {
        val updates = UpdatesMapper.fromRaw(api.getUpdateInfo())

        updates
    }

    suspend fun getAppSettings(): EitherE<AppSettings> = authenticatedRequest { token ->
        SettingsMapper.fromRaw(api.getSettings(token))
    }

    suspend fun updatePushToken(): EitherE<Boolean> = authenticatedRequest { token ->
        when (val t = firebaseTokenProvider.get()) {
            is Right -> when (val r = updatePushToken(t.value)) {
                is Right -> r.value
                is Left -> throw r.value
            }

            is Left -> {
                throw t.value
            }
        }
    }

    suspend fun getUserPosition(id: Int): EitherE<List<UserLocation>> = authenticatedRequest { token ->
        UserLocationMapper.fromRaw(api.requestUserPosition(id))
    }

    suspend fun updateDeviceIMEI(): EitherE<Boolean> = authenticatedRequest { token ->
        api.sendDeviceImei(token, deviceUniqueIdProvider.get().id)
        true
    }

    suspend fun updatePushToken(firebaseToken: FirebaseToken): EitherE<Boolean> = authenticatedRequest { token ->
        firebaseTokenProvider.set(firebaseToken)
        api.sendPushToken(token, firebaseToken.token)
        true
    }

    suspend fun updateLocation(location: Location): EitherE<Boolean> = authenticatedRequest { token ->
        api.sendGPS(
            token,
            location.latitude,
            location.longitude,
            currentTime()
        )
        true
    }

    suspend fun searchFilters(
        filterType: FilterType,
        filterValue: String,
        filters: List<TaskFilter>,
        withPlanned: Boolean
    ): EitherE<List<TaskFilter>> = authenticatedRequest { token ->
        api.searchFilters(
            token,
            SearchFiltersRequest(
                FilterTypeMapper.toInt(filterType),
                filterValue,
                filters
                    .filter { it.isActive() }
                    .map { it.toFilterResponseModel() },
                withPlanned
            )
        ).map { FilterMapper.fromRaw(it) }
    }

    suspend fun refreshAvailableKeys(): EitherE<Unit> {
        return getAvailableEntranceKeys(true)
            .bind { getAvailableEntranceEuroKeys(true) }
            .fmap { Unit }
    }

    suspend fun getAvailableEntranceKeys(withRefresh: Boolean = false): EitherE<List<String>> = authenticatedRequest { token ->
        if (!withRefresh && availableEntranceKeys.isEmpty()) {
            availableEntranceKeys = database.entranceKeysDao().all.map { it.key }
        }
        if (withRefresh || availableEntranceKeys.isEmpty()) {
            availableEntranceKeys = api.getAvailableEntranceKeys(token)
            database.entranceKeysDao().clear()
            database.entranceKeysDao()
                .insertAll(availableEntranceKeys.map { EntranceKeyEntity(0, it) })
        }

        availableEntranceKeys
    }

    suspend fun getAvailableEntranceEuroKeys(withRefresh: Boolean = false): EitherE<List<String>> =
        authenticatedRequest { token ->
            if (!withRefresh && availableEntranceEuroKeys.isEmpty()) {
                availableEntranceEuroKeys = database.entranceEuroKeysDao().all.map { it.key }
            }
            if ((withRefresh || availableEntranceEuroKeys.isEmpty()) && token.isNotBlank()) {
                availableEntranceEuroKeys = api.getAvailableEntranceEuroKeys(token)
                database.entranceEuroKeysDao().clear()
                database.entranceEuroKeysDao()
                    .insertAll(availableEntranceEuroKeys.map { EntranceEuroKeyEntity(0, it) })
            }
            availableEntranceEuroKeys
        }

    suspend fun countFilteredTasks(filters: List<TaskFilter>, withPlanned: Boolean): EitherE<FilteredTasksCount> =
        authenticatedRequest { token ->
            val activeFilters = filters.filter { it.isActive() }
            val req = FiltersRequest(
                activeFilters.map { FilterResponse(it.id, it.name, it.fixed, FilterTypeMapper.toInt(it.type)) },
                withPlanned
            )
            FilteredTasksCountMapper.fromRaw(api.countFilteredTasks(token, req))
        }

    suspend fun getFilteredTaskItems(filters: List<TaskFilter>, withPlanned: Boolean): EitherE<FilteredTasksData> =
        authenticatedRequest { token ->
            val activeFilters = filters.filter { it.isActive() }
            val req = FiltersRequest(
                activeFilters.map { FilterResponse(it.id, it.name, it.fixed, FilterTypeMapper.toInt(it.type)) },
                withPlanned
            )

            FilteredTasksDataMapper.fromRaw(api.getFilteredTaskItems(token, req))
        }

    suspend fun getIsOnlineAvailable(): EitherE<Boolean> = authenticatedRequest { token ->
        api.hasOnlineAccess(token).status
    }

    //Reports
    suspend fun sendReport(item: EntranceReportEntity): Either<Exception, Unit> = Either.of {
        val photosMap = mutableMapOf<String, PhotoReportRequest>()
        val photoParts = mutableListOf<MultipartBody.Part>()
        val photos = database.entrancePhotoDao().getEntrancePhoto(item.taskId, item.taskItemId, item.entranceNumber)

        var imgCount = 0
        photos.forEachIndexed { i, photo ->
            try {
                photoParts.add(
                    photoEntityToPart("img_$imgCount", item, photo)
                )
                photosMap["img_$imgCount"] = PhotoReportRequest("", photo.gps)
                imgCount++
            } catch (e: Throwable) {
                e.fillInStackTrace().log()
            }
        }

        val reportObject = TaskItemReportRequest(
            item.taskId, item.taskItemId, item.idnd, item.entranceNumber,
            item.startAppartaments, item.endAppartaments, item.floors,
            item.description, item.code, item.key, item.euroKey,
            item.isDeliveryWrong, item.hasLookupPost, item.token,
            item.apartmentResult.map { ApartmentResultRequest(it.number.number, it.state, it.buttonGroup, it.description) },
            item.closeTime, photosMap, item.publisherId, item.mailboxType,
            item.gpsLat, item.gpsLong, item.gpsTime, item.entranceClosed,
            item.closeDistance, item.allowedDistance, item.radiusRequired,
            item.isStacked
        )

        api.sendTaskReport(
            item.taskItemId,
            item.token,
            reportObject,
            photoParts
        )
    }

    private fun photoEntityToPart(
        partName: String,
        reportEnt: EntranceReportEntity,
        photoEnt: EntrancePhotoEntity
    ): MultipartBody.Part {
        val photoFile = DatabaseEntrancePhotoMapper.fromEntity(photoEnt).getFile(pathsProvider)
        if (!photoFile.exists()) {
            throw FileNotFoundException(photoFile.path)
        }
        val extension = Uri.fromFile(photoFile).toString().split(".").last()
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)

        val requestFile =
            RequestBody.run { photoFile.asRequestBody(MediaType.run { "image/jpeg".toMediaType() }) }

        return MultipartBody.Part.createFormData(partName, photoFile.name, requestFile)
    }

    suspend fun sendQuery(item: SendQueryItemEntity): Either<java.lang.Exception, Unit> = Either.of {
        val postDataBuilder = FormBody.Builder()
        item.post_data.split("&").forEach {
            it.split("=")
                .let {
                    val left = it.getOrNull(0)
                    val right = it.getOrNull(1)
                    if (left != null && right != null) {
                        left to right
                    } else {
                        null
                    }
                }
                ?.let {
                    postDataBuilder.add(it.first, it.second)
                }
        }
        val request = Request.Builder()
            .url(item.url)
            .post(postDataBuilder.build())
            .build()
        val client = networkClient.newCall(request).execute()
        if (client.code != 200 && client.code != 401) {
            throw Exception("Wrong response code.")
        }
    }

    private fun currentTime(): String = DateTime().toString("yyyy-MM-dd'T'HH:mm:ss")

    suspend fun updateSavedData() {
        val token = authTokenStorage.getToken() ?: return
        if (savedUserStorage.getCredentials() != null && savedUserStorage.getToken() != null) return
        try {
            savedUserStorage.saveToken(token)
            savedUserStorage.saveCredentials(
                currentUserStorage.getCurrentUserLogin() ?: UserLogin(""),
                PasswordMapper.fromRaw(api.getPassword(token))
            )
        } catch (e: Exception) {
            Log.d("ControlRepository", e.message ?: "")
        }
    }

    private suspend inline fun <T> authenticatedRequest(crossinline block: suspend (token: String) -> T): EitherE<T> {
        return authTokenStorage.getToken()?.let { token -> anonymousRequest { block(token) } }
            ?: Left(DomainException.ApiException(ApiError(401, "Empty token", null)))
    }

    private suspend inline fun <T> anonymousRequest(crossinline block: suspend () -> T): EitherE<T> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                Right(block())
            } catch (e: CancellationException) {
                debug("CancellationException $e")
                Left(DomainException.CanceledException)
            } catch (e: HttpException) {
                debug("HttpException $e")
                if (e.code() == 401) {
                    Left(DomainException.ApiException(ApiError(401, "Unauthorized", null)))
                } else {
                    mapApiException(e)?.let { Left(it) } ?: Left(DomainException.UnknownException)
                }
            } catch (e: Exception) {
                debug("UnknownException $e")
//            FirebaseCrashlytics.getInstance().recordException(e)
                Left(DomainException.UnknownException)
            }
        }

    private fun mapApiException(httpException: HttpException): DomainException.ApiException? {
        return parseErrorBody(httpException.response())?.let { DomainException.ApiException(it) }
    }

    private fun parseErrorBody(response: Response<*>?): ApiError? {
        return try {
            Gson().fromJson(response?.errorBody()?.string(), ApiErrorContainer::class.java)?.error
        } catch (e: Exception) {
            debug("Can't parse HTTP error", e)
            return null
        }
    }
}