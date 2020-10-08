package ru.relabs.kurjercontroller.domain.repositories

import android.graphics.BitmapFactory
import android.location.Location
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
import ru.relabs.kurjercontroller.data.database.entities.SendQueryItemEntity
import ru.relabs.kurjercontroller.data.models.PhotoReportRequest
import ru.relabs.kurjercontroller.data.models.TaskItemReportRequest
import ru.relabs.kurjercontroller.data.models.auth.UserLogin
import ru.relabs.kurjercontroller.data.models.common.ApiError
import ru.relabs.kurjercontroller.data.models.common.ApiErrorContainer
import ru.relabs.kurjercontroller.data.models.common.DomainException
import ru.relabs.kurjercontroller.data.models.common.EitherE
import ru.relabs.kurjercontroller.domain.mappers.network.*
import ru.relabs.kurjercontroller.domain.models.*
import ru.relabs.kurjercontroller.domain.providers.*
import ru.relabs.kurjercontroller.domain.storage.AuthTokenStorage
import ru.relabs.kurjercontroller.utils.*
import java.io.FileNotFoundException
import java.net.URL
import java.util.*

class ControlRepository(
    private val controlApi: ControlApi,
    private val authTokenStorage: AuthTokenStorage,
    private val deviceIdProvider: DeviceUUIDProvider,
    private val deviceUniqueIdProvider: DeviceUniqueIdProvider,
    private val firebaseTokenProvider: FirebaseTokenProvider,
    private val database: AppDatabase,
    private val networkClient: OkHttpClient,
    private val pathsProvider: PathsProvider
) {
    fun isAuthenticated(): Boolean = authTokenStorage.getToken() != null

    suspend fun login(login: UserLogin, password: String): EitherE<Pair<User, String>> = anonymousRequest {
        val r = controlApi.login(
            login.login,
            password,
            deviceIdProvider.getOrGenerateDeviceUUID().id,
            currentTime()
        )
        val user = UserMapper.fromRaw(r.user)

        user to r.token
    }

    suspend fun login(token: String): EitherE<Pair<User, String>> = anonymousRequest {
        val r = controlApi.loginByToken(
            token,
            deviceIdProvider.getOrGenerateDeviceUUID().id,
            currentTime()
        )
        val user = UserMapper.fromRaw(r.user)

        user to token
    }

    suspend fun getTasks(): EitherE<List<Task>> = authenticatedRequest { token ->
        val deviceId = deviceIdProvider.getOrGenerateDeviceUUID()
        controlApi.getTasks(
            token,
            currentTime()
        ).map {
            TaskMapper.fromRaw(it, deviceId)
        }
    }

    suspend fun getAppUpdatesInfo(): EitherE<AppUpdatesInfo> = anonymousRequest {
        val updates = UpdatesMapper.fromRaw(controlApi.getUpdateInfo())

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
        controlApi.sendPushToken(token, firebaseToken.token)
        true
    }

    suspend fun updateDeviceIMEI(): EitherE<Boolean> = authenticatedRequest { token ->
        controlApi.sendDeviceImei(token, deviceUniqueIdProvider.get().id)
        true
    }

    suspend fun updateLocation(location: Location): EitherE<Boolean> = authenticatedRequest { token ->
        controlApi.sendGPS(
            token,
            location.latitude,
            location.longitude,
            currentTime()
        )
        true
    }

    //Reports
    suspend fun sendReport(item: ReportQueryItemEntity): Either<Exception, Unit> = Either.of {
        val photosMap = mutableMapOf<String, PhotoReportRequest>()
        val photoParts = mutableListOf<MultipartBody.Part>()
        val photos = database.photosDao().getByTaskItemId(item.taskItemId)

        var imgCount = 0
        photos.forEachIndexed { i, photo ->
            try {
                photoParts.add(photoEntityToPart("img_$imgCount", item, photo))
                photosMap["img_$imgCount"] =
                    PhotoReportRequest("", photo.gps, photo.entranceNumber)
                imgCount++
            } catch (e: Throwable) {
                e.fillInStackTrace().log()
            }
        }

        val reportObject = TaskItemReportRequest(
            item.taskId, item.taskItemId, item.imageFolderId,
            item.gps, item.closeTime, item.userDescription, item.entrances, photosMap,
            item.batteryLevel, item.closeDistance, item.allowedDistance, item.radiusRequired
        )

        controlApi.sendTaskReport(
            item.taskItemId,
            reportObject,
            photoParts,
            item.token
        )
    }

    private fun photoEntityToPart(partName: String, reportEnt: ReportQueryItemEntity, photoEnt: TaskItemPhotoEntity): MultipartBody.Part {
        val photoFile = pathsProvider.getTaskItemPhotoFileByID(
            reportEnt.taskItemId,
            UUID.fromString(photoEnt.UUID)
        )
        if (!photoFile.exists()) {
            throw FileNotFoundException(photoFile.path)
        }

        val request =
            RequestBody.run { photoFile.asRequestBody(MediaType.run { "image/jpeg".toMediaType() }) }

        return MultipartBody.Part.createFormData(partName, photoFile.name, request)
    }

    //Pauses
    suspend fun getLastPauseTimes(): EitherE<PauseTimes> = authenticatedRequest { token ->
        PauseMapper.fromRaw(controlApi.getLastPauseTimes(token))
    }

    suspend fun isPauseAllowed(pauseType: PauseType): EitherE<Boolean> = authenticatedRequest { token ->
        controlApi.isPauseAllowed(token, pauseType.ordinal).status
    }

    suspend fun getPauseDurations(): EitherE<PauseDurations> = anonymousRequest {
        PauseMapper.fromRaw(controlApi.getPauseDurations())
    }

    suspend fun getAllowedCloseRadius(): EitherE<AllowedCloseRadius> = authenticatedRequest { token ->
        RadiusMapper.fromRaw(controlApi.getRadius(token))
    }

    suspend fun loadTaskMap(task: Task): Either<Exception, Unit> = Either.of {
        val url = URL(task.rastMapUrl)
        val bmp = BitmapFactory.decodeStream(url.openStream())
        val mapFile = pathsProvider.getTaskRasterizeMapFile(task)
        ImageUtils.saveImage(bmp, mapFile)
        bmp.recycle()
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

    //Could be sended in other user session
    suspend fun startPause(pauseType: PauseType, token: String, startTime: Long): EitherE<Boolean> = anonymousRequest {
        controlApi.startPause(token, pauseType.toInt(), startTime)
        true
    }

    suspend fun stopPause(pauseType: PauseType, token: String, stopTime: Long): EitherE<Boolean> = anonymousRequest {
        controlApi.stopPause(token, pauseType.toInt(), stopTime)
        true
    }

    private fun currentTime(): String = DateTime().toString("yyyy-MM-dd'T'HH:mm:ss")

    private suspend inline fun <T> authenticatedRequest(crossinline block: suspend (token: String) -> T): EitherE<T> {
        return authTokenStorage.getToken()?.let { token -> anonymousRequest { block(token) } }
            ?: Left(DomainException.ApiException(ApiError(401, "Empty token", null)))
    }

    private suspend inline fun <T> anonymousRequest(crossinline block: suspend () -> T): EitherE<T> = withContext(Dispatchers.IO) {
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