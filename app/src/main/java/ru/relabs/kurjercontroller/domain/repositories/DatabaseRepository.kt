package ru.relabs.kurjercontroller.domain.repositories

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.data.database.AppDatabase
import ru.relabs.kurjercontroller.data.database.entities.*
import ru.relabs.kurjercontroller.domain.mappers.database.*
import ru.relabs.kurjercontroller.domain.models.*
import ru.relabs.kurjercontroller.domain.providers.PathsProvider
import ru.relabs.kurjercontroller.domain.storage.AuthTokenStorage
import ru.relabs.kurjercontroller.utils.Either
import ru.relabs.kurjercontroller.utils.Left
import ru.relabs.kurjercontroller.utils.Right
import ru.relabs.kurjercontroller.utils.deleteIfEmpty
import java.io.File

class DatabaseRepository(
    val db: AppDatabase,
    private val authTokenStorage: AuthTokenStorage,
    private val baseUrl: String,
    private val pathsProvider: PathsProvider
) {

    suspend fun getOnlineTask(): Task? = withContext(Dispatchers.IO) {
        db.taskDao().getOnlineTask()?.let { DatabaseTaskMapper.fromEntity(it, db) }
    }

    suspend fun getTasks(): List<Task> = withContext(Dispatchers.IO) {
        db.taskDao().allOpened
            .map { DatabaseTaskMapper.fromEntity(it, db) }
            .filter { it.taskItems.isNotEmpty() }
    }

    suspend fun removeReport(report: EntranceReportEntity) = withContext(Dispatchers.IO) {
        db.entranceReportDao().delete(report)
        db.entrancePhotoDao()
            .getEntrancePhoto(report.taskId, report.taskItemId, report.entranceNumber)
            .forEach { removePhoto(it) }
    }

    suspend fun clearTasks() = withContext(Dispatchers.IO) {
        db.taskDao().all.forEach {
            removeTask(TaskId(it.id))
        }
    }

    private suspend fun removeTaskItem(taskItem: TaskItemEntity) {
        db.entranceDao().getByTaskItemId(taskItem.taskId, taskItem.taskItemId).forEach { entrance ->
            db.entranceResultDao()
                .deleteByEntrance(taskItem.taskId, taskItem.taskItemId, entrance.number)
            db.entrancePhotoDao()
                .getEntrancePhoto(taskItem.taskId, taskItem.taskItemId, entrance.number)
                .forEach { photo ->
                    removePhoto(photo)
                }
            db.apartmentResultDao()
                .deleteByEntrance(taskItem.taskId, taskItem.taskItemId, entrance.number)
            db.entranceReportDao()
                .deleteByEntrance(taskItem.taskId, taskItem.taskItemId, entrance.number)
            db.entranceDao().delete(entrance)
        }
        db.taskItemDao().delete(taskItem)
    }

    suspend fun removePhoto(entrancePhoto: EntrancePhotoEntity) = withContext(Dispatchers.IO) {
        db.entrancePhotoDao().deleteById(entrancePhoto.id)
        if (db.entrancePhotoDao().getByUUID(entrancePhoto.UUID).isEmpty()) {
            deletePhotoFile(entrancePhoto)
        }
    }

    suspend fun deletePhotoFile(entrancePhoto: EntrancePhotoEntity) {
        val file = File(
            entrancePhoto.realPath
                ?: pathsProvider.getEntrancePhotoFileByID(
                    TaskItemId(entrancePhoto.taskItemId),
                    EntranceNumber(entrancePhoto.entranceNumber),
                    entrancePhoto.UUID
                ).path
        )
        val entranceFolder = file.parentFile
        val taskItemFolder = entranceFolder.parentFile
        file.delete()
        entranceFolder.deleteIfEmpty()
        taskItemFolder.deleteIfEmpty()
    }

    private suspend fun removeTask(taskId: TaskId) = withContext(Dispatchers.IO) {
        db.taskPublisherDao().deleteByTaskId(taskId.id)
        db.filtersDao().deleteByTaskId(taskId.id)
        db.taskItemDao().getByTaskId(taskId.id)
            .forEach { taskItem ->
                db.entranceDao().getByTaskItemId(taskItem.taskId, taskItem.taskItemId)
                    .forEach {
                        deleteEntrance(taskItem.taskId, taskItem.taskItemId, it.number)
                    }

                safeDeleteAddress(taskItem.addressId)

                db.taskItemDao().delete(taskItem)
            }

        db.taskDao().deleteById(taskId.id)
    }

    private suspend fun safeDeleteAddress(addressId: Int) {
        if (db.taskItemDao().getByAddressId(addressId).isEmpty()) {
            db.addressDao().deleteById(addressId)
        }
    }

    suspend fun deleteEntrance(taskId: Int, taskItemId: Int, entranceNumber: Int) =
        withContext(Dispatchers.IO) {
            val entity = db.entranceDao().getByNumber(taskId, taskItemId, entranceNumber)
                ?: return@withContext
            db.entranceDao().delete(entity)
            db.entranceResultDao().deleteByEntrance(entity.taskId, entity.taskItemId, entity.number)
            db.apartmentResultDao()
                .deleteByEntrance(entity.taskId, entity.taskItemId, entity.number)
        }

    suspend fun putSendQuery(sendData: SendQueryData): Either<Exception, SendQueryItemEntity> = withContext(Dispatchers.IO) {
        when (val r = mapSendDataToEntity(sendData)) {
            is Right -> {
                val id = db.sendQueryDao().insert(r.value)
                Right(r.value.copy(id = id.toInt()))
            }
            is Left -> r
        }
    }

    private fun mapSendDataToEntity(data: SendQueryData): Either<Exception, SendQueryItemEntity> {
        return when (data) {
            is SendQueryData.TaskAccepted -> getAuthorizedSendQuery(
                "$baseUrl/api/v1/tasks/${data.taskId.id}/accepted"
            )
            is SendQueryData.TaskReceived -> getAuthorizedSendQuery(
                "$baseUrl/api/v1/tasks/${data.taskId.id}/received"
            )
            is SendQueryData.TaskExamined -> getAuthorizedSendQuery(
                "$baseUrl/api/v1/tasks/${data.taskId.id}/examined"
            )
            is SendQueryData.TaskCompleted -> getAuthorizedSendQuery(
                "$baseUrl/api/v1/tasks/${data.taskId.id}/completed"
            )
        }
    }

    private fun getAuthorizedSendQuery(url: String, postData: String = ""): Either<Exception, SendQueryItemEntity> {
        val token = authTokenStorage.getToken() ?: throw RuntimeException("Empty auth token")
        return getSendQuery(url, postData, token)
    }

    private fun getSendQuery(url: String, postData: String, token: String?): Either<Exception, SendQueryItemEntity> = Either.of {
        SendQueryItemEntity(
            0,
            url + "?token=${token ?: ""}",
            postData
        )
    }

    suspend fun closeTaskById(taskId: TaskId, sendClosed: Boolean) = closeTaskById(taskId.id, sendClosed)

    private suspend fun closeTaskById(taskId: Int, sendClosed: Boolean) = withContext(Dispatchers.IO) {
        db.taskPublisherDao().deleteByTaskId(taskId)
        db.filtersDao().deleteByTaskId(taskId)
        db.taskItemDao().getByTaskId(taskId)
            .forEach { taskItem ->
                db.entranceDao().getByTaskItemId(taskItem.taskId, taskItem.taskItemId)
                    .forEach {
                        deleteEntrance(taskItem.taskId, taskItem.taskItemId, it.number)
                    }

                safeDeleteAddress(taskItem.addressId)

                db.taskItemDao().delete(taskItem)
            }

        db.taskDao().deleteById(taskId)

        if(sendClosed){
            putSendQuery(SendQueryData.TaskCompleted(TaskId(taskId)))
        }
    }

    suspend fun merge(newTasks: List<Task>): Flow<MergeResult> = flow {

        val savedTasksIDs = db.taskDao().all.map { it.id }
        val newTasksIDs = newTasks.map { it.id }

        //Задача отсутствует в ответе от сервера (удалено)
        db.taskDao().all.filter { it.id !in newTasksIDs && !it.isOnline }.forEach { task ->
            closeTaskById(task.id, false)
            emit(MergeResult.TaskRemoved(TaskId(task.id)))
            Log.d("merge", "Close task: ${task.id}")
        }

        //Задача не присутствует в сохранённых (новая)
        newTasks.filter { it.id.id !in savedTasksIDs }.forEach { task ->
            if (task.state.state == TaskState.CANCELED) {
                Log.d("merge", "New task ${task.id} passed due 12 status")
                return@forEach
            }
            //Add task
            val newTaskId = db.taskDao().insert(DatabaseTaskMapper.toEntity(task))
            Log.d("merge", "Add task ID: $newTaskId")
            var openedEntrances = 0
            db.taskStorageDao().insertAll(task.storages.map { it.toEntity(task.id) })
            db.taskPublisherDao().insertAll(task.publishers.map { it.toEntity() })
            if (!task.filtered) {
                task.taskItems.forEach { item ->
                    db.addressDao().insert(item.address.toEntity())
                    db.taskItemDao().insert(DatabaseTaskItemMapper.toEntity(item))
                    item.entrances.forEach { entrance ->
                        val entity = DatabaseEntranceMapper.toEntity(entrance, item.taskId, item.id)
                        if (db.entranceReportDao().getByNumber(entity.taskItemId, entity.number) != null) {
                            entity.state = EntranceState.CLOSED.toInt()
                        }
                        db.entranceDao().insert(entity)
                        if (entity.state == EntranceState.CREATED.toInt()) {
                            openedEntrances++
                        }
                    }
                }
                if (openedEntrances <= 0) {
                    closeTaskById(newTaskId.toInt(), false)
                } else {
                    emit(MergeResult.TaskCreated(task))
                    putSendQuery(SendQueryData.TaskReceived(task.id))
                }
            } else {
                saveFilters(task)
                emit(MergeResult.TaskCreated(task))
                putSendQuery(SendQueryData.TaskReceived(task.id))
            }
        }

        //Задача есть и на сервере и на клиенте (мерж)
        /*
        Если она закрыта | выполнена на сервере - удалить с клиента
        Если итерация > сохранённой | состояние отличается от сохранённого и сохранённое != начато |
         */
        newTasks.filter { it.id.id in savedTasksIDs }.forEach { task ->
            val savedTask = db.taskDao().getById(task.id.id) ?: return@forEach
            //Игнорируем задания с фильтрами, т.к. нет возможности на месте получить список TaskItems

            if (task.state.state == TaskState.CANCELED) {
                if (savedTask.state.toTaskState() == TaskState.STARTED) {
                    putSendQuery(SendQueryData.TaskAccepted(task.id))
                } else {
                    closeTaskById(savedTask.id, false)
                    emit(MergeResult.TaskRemoved(task.id))
                }
            } else if (task.state.state == TaskState.COMPLETED) {
                closeTaskById(savedTask.id, true)
                emit(MergeResult.TaskRemoved(task.id))
                return@forEach
            } else if (
                (savedTask.iteration < task.iteration)
                || (task.state.state.toInt() != savedTask.state && savedTask.state != TaskState.STARTED.toInt())
            ) {
                db.taskDao().update(DatabaseTaskMapper.toEntity(task))
                db.taskStorageDao().insertAll(task.storages.map { it.toEntity(task.id) })
                db.taskPublisherDao().insertAll(task.publishers.map { it.toEntity() })

                val currentTaskItems = db.taskItemDao().getByTaskId(task.id.id).toMutableList()

                task.taskItems.forEach { taskItem ->
                    currentTaskItems.removeAll { oldTaskItem -> oldTaskItem.taskItemId == taskItem.id.id && oldTaskItem.taskId == taskItem.taskId.id }

                    db.addressDao().insert(taskItem.address.toEntity())
                    db.taskItemDao().insert(DatabaseTaskItemMapper.toEntity(taskItem))
                    taskItem.entrances.forEach { entrance ->
                        val entity = DatabaseEntranceMapper.toEntity(entrance, taskItem.taskId, taskItem.id)
                        if (db.entranceReportDao().getByNumber(entity.taskItemId, entity.number) != null) {
                            entity.state = EntranceState.CLOSED.toInt()
                        }
                        db.entranceDao().insert(entity)
                    }
                }

                currentTaskItems.forEach {
                    removeTaskItem(it)
                }
                emit(MergeResult.TaskUpdated(task))
            } else {
                //MERGE CLOSE TIMINGS
                task.taskItems.forEach { taskItem ->
                    val savedItem = db.taskItemDao().getByTaskItemId(taskItem.taskId.id, taskItem.id.id)
                    if (savedItem != null && savedItem.closeTime == null && taskItem.closeTime != null) {
                        db.taskItemDao()
                            .insert(savedItem.copy(closeTime = DateTime(taskItem.closeTime), isNew = true))
                    }
                }
                emit(MergeResult.TaskUpdated(task))
            }
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun saveFilters(
        task: Task,
        filters: TaskFilters = task.taskFilters,
        withPlanned: Boolean = task.withPlanned
    ) = withContext(Dispatchers.IO) {
        db.taskDao().getById(task.id.id)?.let {
            db.taskDao().update(it.copy(withPlanned = withPlanned))
        }

        db.filtersDao().deleteByTaskId(task.id.id)

        db.filtersDao().insertAll(filters.brigades.map {
            DatabaseFilterMapper.toEntity(it, task.id, FilterEntity.BRIGADE_FILTER)
        })
        db.filtersDao().insertAll(filters.publishers.map {
            DatabaseFilterMapper.toEntity(it, task.id, FilterEntity.PUBLISHER_FILTER)
        })
        db.filtersDao().insertAll(filters.users.map {
            DatabaseFilterMapper.toEntity(it, task.id, FilterEntity.USER_FILTER)
        })
        db.filtersDao().insertAll(filters.districts.map {
            DatabaseFilterMapper.toEntity(it, task.id, FilterEntity.DISTRICT_FILTER)
        })
        db.filtersDao().insertAll(filters.regions.map {
            DatabaseFilterMapper.toEntity(it, task.id, FilterEntity.REGION_FILTER)
        })
    }

    suspend fun examineTask(task: Task): Task = withContext(Dispatchers.IO) {
        if (task.state.state != TaskState.CREATED) {
            return@withContext task
        }

        db.taskDao().getById(task.id.id)?.let {
            db.taskDao().update(it.copy(state = TaskState.EXAMINED.toInt(), byOtherUser = false))

            putSendQuery(SendQueryData.TaskExamined(task.id))
        }

        task.copy(state = Task.State(TaskState.EXAMINED, false))
    }

    suspend fun createEntranceReport(reportItem: EntranceReportEntity) = withContext(Dispatchers.IO) {
        db.entranceReportDao().insert(reportItem)
    }

    suspend fun getNextSendQuery(): SendQueryItemEntity? = withContext(Dispatchers.IO) {
        db.sendQueryDao().all.firstOrNull()
    }

    suspend fun getNextReportQuery(): EntranceReportEntity? = withContext(Dispatchers.IO) {
        db.entranceReportDao().all.firstOrNull()
    }

    suspend fun getQueryItemsCount(): Int = withContext(Dispatchers.IO) {
        db.entranceReportDao().all.size + db.sendQueryDao().all.size
    }

    suspend fun removeSendQuery(sendQuery: SendQueryItemEntity) = withContext(Dispatchers.IO) {
        db.sendQueryDao().delete(sendQuery)
    }

    suspend fun isMergeNeeded(newTasks: List<Task>): Boolean = withContext(Dispatchers.IO) {
        val savedTasksIDs = db.taskDao().all.map { it.id }
        val newTasksIDs = newTasks.map { it.id.id }

        db.taskDao().all.filter { it.id !in newTasksIDs && !it.isOnline }.forEach {
            return@withContext true
        }
        newTasks.filter { it.id.id !in savedTasksIDs }.forEach {
            return@withContext true
        }
        newTasks.filter { it.id.id in savedTasksIDs }.forEach { task ->
            val savedTask = db.taskDao().getById(task.id.id) ?: return@forEach
            if (task.state.state == TaskState.CANCELED) {
                if (savedTask.state != TaskState.STARTED.toInt()) {
                    return@withContext true
                }
            } else if (task.state.state == TaskState.COMPLETED) {
                return@withContext true
            } else if (
                (savedTask.iteration < task.iteration)
                || (task.state.state.toInt() != savedTask.state && savedTask.state != TaskState.STARTED.toInt())
            ) {
                return@withContext true
            } else {
                task.taskItems.forEach { taskItem ->
                    val savedItem = db.taskItemDao().getByTaskItemId(taskItem.taskId.id, taskItem.id.id)
                    if (savedItem != null && savedItem.closeTime == null && taskItem.closeTime != null) {
                        return@withContext true
                    }
                }
            }
        }
        return@withContext false
    }

    suspend fun getEntranceResult(
        taskItem: TaskItem,
        entrance: Entrance
    ): EntranceResultEntity? = withContext(Dispatchers.IO) {
        db.entranceResultDao().getByEntrance(taskItem.taskId.id, taskItem.id.id, entrance.number.number)
    }

    suspend fun getEntranceApartments(
        taskItem: TaskItem,
        entrance: Entrance
    ): List<ApartmentResult> = withContext(Dispatchers.IO) {
        db.apartmentResultDao()
            .getByEntrance(taskItem.taskId.id, taskItem.id.id, entrance.number.number)
            .map { DatabaseEntranceApartmentsMapper.fromEntity(it) }
    }

    suspend fun closeOutdatedOnlineTask() {
        getOnlineTask()?.let {
            val currentTime = DateTime()
            if (it.endControlDate.plusHours(1) < currentTime.withTimeAtStartOfDay()) {
                closeTaskById(it.id.id, false)
            }
        }
    }

    suspend fun getTaskItems(taskItemId: TaskItemId): List<TaskItem> = withContext(Dispatchers.IO) {
        db.taskItemDao()
            .getAllByTaskItemId(taskItemId.id)
            .map { DatabaseTaskItemMapper.fromEntity(it, db) }
    }

    suspend fun getTask(taskId: TaskId): Task? = withContext(Dispatchers.IO) {
        db.taskDao().getById(taskId.id)?.let { DatabaseTaskMapper.fromEntity(it, db) }
    }

    suspend fun updateTaskItem(item: TaskItem) = withContext(Dispatchers.IO) {
        db.taskItemDao().insert(DatabaseTaskItemMapper.toEntity(item))
    }
}

sealed class SendQueryData {
    data class TaskAccepted(val taskId: TaskId) : SendQueryData()
    data class TaskReceived(val taskId: TaskId) : SendQueryData()
    data class TaskExamined(val taskId: TaskId) : SendQueryData()
    data class TaskCompleted(val taskId: TaskId) : SendQueryData()
}

sealed class MergeResult {
    data class TaskCreated(val task: Task) : MergeResult()
    data class TaskRemoved(val taskId: TaskId) : MergeResult()
    data class TaskUpdated(val task: Task) : MergeResult()
}

