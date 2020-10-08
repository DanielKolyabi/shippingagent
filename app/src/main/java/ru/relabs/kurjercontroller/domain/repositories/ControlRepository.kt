package ru.relabs.kurjercontroller.domain.repositories

import android.location.Location
import android.net.Uri
import android.webkit.MimeTypeMap
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.joda.time.DateTime
import retrofit2.HttpException
import retrofit2.Response
import ru.relabs.kurjercontroller.data.api.ControlApi
import ru.relabs.kurjercontroller.data.database.AppDatabase
import ru.relabs.kurjercontroller.data.database.entities.*
import ru.relabs.kurjercontroller.data.models.PhotoReportRequest
import ru.relabs.kurjercontroller.data.models.SearchFiltersRequest
import ru.relabs.kurjercontroller.data.models.TaskItemReportRequest
import ru.relabs.kurjercontroller.data.models.auth.UserLogin
import ru.relabs.kurjercontroller.data.models.common.ApiError
import ru.relabs.kurjercontroller.data.models.common.ApiErrorContainer
import ru.relabs.kurjercontroller.data.models.common.DomainException
import ru.relabs.kurjercontroller.data.models.common.EitherE
import ru.relabs.kurjercontroller.domain.mappers.network.FilterMapper
import ru.relabs.kurjercontroller.domain.mappers.network.TaskMapper
import ru.relabs.kurjercontroller.domain.mappers.network.UpdatesMapper
import ru.relabs.kurjercontroller.domain.mappers.network.UserMapper
import ru.relabs.kurjercontroller.domain.models.AppUpdatesInfo
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskFilter
import ru.relabs.kurjercontroller.domain.models.User
import ru.relabs.kurjercontroller.domain.providers.*
import ru.relabs.kurjercontroller.domain.storage.AuthTokenStorage
import ru.relabs.kurjercontroller.fileHelpers.PathHelper
import ru.relabs.kurjercontroller.logError
import ru.relabs.kurjercontroller.utils.Either
import ru.relabs.kurjercontroller.utils.Left
import ru.relabs.kurjercontroller.utils.Right
import ru.relabs.kurjercontroller.utils.debug
import java.io.FileNotFoundException

class ControlRepository(
    private val api: ControlApi,
    private val authTokenStorage: AuthTokenStorage,
    private val deviceIdProvider: DeviceUUIDProvider,
    private val deviceUniqueIdProvider: DeviceUniqueIdProvider,
    private val firebaseTokenProvider: FirebaseTokenProvider,
    private val database: AppDatabase,
    private val networkClient: OkHttpClient,
    private val pathsProvider: PathsProvider
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
        api.getTasks(token, currentTime()).map { TaskMapper.fromRaw(it, deviceId) }
    }

    suspend fun getAppUpdatesInfo(): EitherE<AppUpdatesInfo> = anonymousRequest {
        val updates = UpdatesMapper.fromRaw(api.getUpdateInfo())

        updates
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
        filterType: Int,
        filterValue: String,
        filters: List<TaskFilter>,
        withPlanned: Boolean
    ): EitherE<List<TaskFilter>> = authenticatedRequest { token ->
        api.searchFilters(
            token,
            SearchFiltersRequest(
                filterType,
                filterValue,
                filters
                    .filter { it.isActive() }
                    .map { it.toFilterResponseModel() },
                withPlanned
            )
        ).map { FilterMapper.fromRaw(it) }
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
                e.logError()
            }
        }

        val reportObject = TaskItemReportRequest(
            item.taskId, item.taskItemId, item.idnd, item.entranceNumber,
            item.startAppartaments, item.endAppartaments, item.floors,
            item.description, item.code, item.key,
            item.euroKey, item.isDeliveryWrong, item.hasLookupPost,
            item.token, item.apartmentResult, item.closeTime,
            photosMap, item.publisherId, item.mailboxType,
            item.gpsLat, item.gpsLong, item.gpsTime,
            item.entranceClosed
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
        if (client.code != 200) {
            throw Exception("Wrong response code.")
        }
    }

    private fun currentTime(): String = DateTime().toString("yyyy-MM-dd'T'HH:mm:ss")

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