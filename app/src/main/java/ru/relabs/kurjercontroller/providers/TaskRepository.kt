package ru.relabs.kurjercontroller.providers

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.BuildConfig
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.database.AppDatabase
import ru.relabs.kurjercontroller.database.entities.EntranceResultEntity
import ru.relabs.kurjercontroller.database.entities.SendQueryItemEntity
import ru.relabs.kurjercontroller.fileHelpers.PathHelper
import ru.relabs.kurjercontroller.models.*
import ru.relabs.kurjercontroller.network.DeliveryServerAPI

/**
 * Created by ProOrange on 11.04.2019.
 */
class TaskRepository(val db: AppDatabase) {
    suspend fun loadRemoteTasks(token: String): List<TaskModel> = withContext(Dispatchers.IO) {
        return@withContext DeliveryServerAPI.api.getTasks(token, DateTime().toString("yyyy-MM-dd'T'HH:mm:ss")).await()
            .map { it.toModel() }
    }

    suspend fun examineTask(task: TaskModel): TaskModel = withContext(Dispatchers.IO) {
        if (task.androidState != TaskModel.CREATED) return@withContext task
        val updatedTask = task.copy(state = TaskModel.EXAMINED.toSiriusState())
        db.taskDao().update(updatedTask.toEntity())

        db.sendQueryDao().insert(
            SendQueryItemEntity(
                0,
                BuildConfig.API_URL + "/api/v1/controller/tasks/${task.id}/examined?token=" + application().user.getUserCredentials()?.token.orEmpty(),
                ""
            )
        )

        return@withContext updatedTask
    }

    suspend fun mergeTasks(newTasks: List<TaskModel>): MergeResult = withContext(Dispatchers.IO) {
        return@withContext merge(newTasks) {}
    }

    private suspend fun merge(
        newTasks: List<TaskModel>,
        onNewTaskAppear: (task: TaskModel) -> Unit
    ): MergeResult {
        val result = MergeResult(false, false)
        val savedTasksIDs = db.taskDao().all.map { it.id }
        val newTasksIDs = newTasks.map { it.id }

        //Задача отсутствует в ответе от сервера (удалено)
        db.taskDao().all.filter { it.id !in newTasksIDs }.forEach { task ->
            closeTaskById(task.id)
            result.isTasksChanged = true
            Log.d("merge", "Close task: ${task.id}")
        }

        //Задача не присутствует в сохранённых (новая)
        newTasks.filter { it.id !in savedTasksIDs }.forEach { task ->
            if (task.androidState == TaskModel.CANCELED) {
                Log.d("merge", "New task ${task.id} passed due 12 status")
                return@forEach
            }
            //Add task
            val newTaskId = db.taskDao().insert(task.toEntity())
            Log.d("merge", "Add task ID: $newTaskId")
            var openedEntrances = 0
            db.taskPublisherDao().insertAll(task.publishers.map { it.toEntity() })
            task.taskItems.forEach { item ->
                db.addressDao().insert(item.address.toEntity())
                db.taskItemDao().insert(item.toEntity())
                item.entrances.forEach { entrance ->
                    db.entranceDao().insert(entrance.toEntity(item.id))
                    //TODO: Entrance report state
                    if (entrance.state == EntranceModel.CREATED) {
                        openedEntrances++
                    }
                }
            }
            if (openedEntrances <= 0) {
                closeTaskById(newTaskId.toInt())
            } else {
                result.isNewTasksAdded = true
                onNewTaskAppear(task)
            }
        }

        //Задача есть и на сервере и на клиенте (мерж)
        /*
        Если она закрыта | выполнена на сервере - удалить с клиента
        Если итерация > сохранённой | состояние отличается от сохранённого и сохранённое != начато |
         */
        newTasks.filter { it.id in savedTasksIDs }.forEach { task ->
            val savedTask = db.taskDao().getById(task.id) ?: return@forEach
            if (task.androidState == TaskModel.CANCELED) {
                if (savedTask.state == TaskModel.STARTED) {
                    //Уведомили что задание уже в работе
                    //TODO: Backend!!!
                    db.sendQueryDao().insert(
                        SendQueryItemEntity(
                            0,
                            BuildConfig.API_URL + "/api/v1/controller/tasks/${savedTask.id}/accepted?token=" + application().user.getUserCredentials()?.token.orEmpty(),
                            ""
                        )
                    )
                } else {
                    closeTaskById(savedTask.id)
                }
            } else if (task.androidState == TaskModel.COMPLETED) {
                closeTaskById(savedTask.id)
                return@forEach
            } else if (
                (savedTask.iteration < task.iteration)
                || (task.androidState != savedTask.state && savedTask.state != TaskModel.STARTED)
                || (task.endControlDate != savedTask.endControlDate || task.startControlDate != savedTask.startControlDate)
            ) {

                db.taskDao().update(task.toEntity())
                db.taskPublisherDao().insertAll(task.publishers.map { it.toEntity() })
                task.taskItems.forEach { taskItem ->
                    db.addressDao().insert(taskItem.address.toEntity())
                    db.taskItemDao().insert(taskItem.toEntity())
                    taskItem.entrances.forEach { entrance ->
                        //TODO: Entrance report state
                        db.entranceDao().insert(entrance.toEntity(taskItem.id))
                    }
                }
                result.isTasksChanged = true
            }
        }
        return result
    }

    suspend fun saveTaskToDB(task: TaskModel) = withContext(Dispatchers.IO) {
        db.taskDao().insert(task.toEntity())
        db.taskPublisherDao().insertAll(task.publishers.map { it.toEntity() })
        db.taskItemDao().insertAll(task.taskItems.map { it.toEntity() })
        task.taskItems.forEach { taskItem ->
            db.addressDao().insert(taskItem.address.toEntity())
            db.entranceDao().insertAll(taskItem.entrances.map { it.toEntity(taskItem.id) })
        }
        //TODO: Filters
    }

    suspend fun getTasks(): List<TaskModel> = withContext(Dispatchers.IO) {
        return@withContext db.taskDao().all.map { it.toModel(db) }
    }

    suspend fun getTaskItems(taskId: Int): List<TaskItemModel> = withContext(Dispatchers.IO) {
        return@withContext db.taskItemDao().getByTaskId(taskId).map { it.toModel(db) }
    }

    suspend fun getTask(taskId: Int): TaskModel? = withContext(Dispatchers.IO) {
        return@withContext db.taskDao().getById(taskId)?.toModel(db)
    }

    suspend fun getTaskItem(taskItemId: Int): TaskItemModel? = withContext(Dispatchers.IO) {
        return@withContext db.taskItemDao().getById(taskItemId)?.toModel(db)
    }

    suspend fun closeAllTasks() = withContext(Dispatchers.IO) {
        db.taskDao().all.forEach { task ->
            closeTaskById(task.id)
        }
    }

    suspend fun closeTaskById(taskId: Int) = withContext(Dispatchers.IO) {
        db.taskPublisherDao().getByTaskId(taskId)
            .forEach { db.taskPublisherDao().delete(it) }

        db.taskItemDao().getByTaskId(taskId)
            .forEach { taskItem ->
                db.entranceDao().getByTaskItemId(taskItem.id)
                    .forEach {
                        db.entranceDao().delete(it)
                        db.entranceResultDao().deleteByEntrance(taskItem.id, it.number)
                    }

                db.addressDao().deleteById(taskItem.addressId)

                db.taskItemDao().delete(taskItem)
            }

        db.taskDao().deleteById(taskId)
    }

    suspend fun removePhoto(entrancePhoto: EntrancePhotoModel) = withContext(Dispatchers.IO) {
        db.entrancePhotoDao().deleteById(entrancePhoto.id)
        PathHelper.deletePhoto(entrancePhoto)
    }

    suspend fun savePhoto(entrancePhoto: EntrancePhotoModel): Long = withContext(Dispatchers.IO) {
        return@withContext db.entrancePhotoDao().insert(entrancePhoto.toEntity())
    }

    suspend fun loadEntrancePhotos(taskItem: TaskItemModel, entrance: EntranceModel): List<EntrancePhotoModel> =
        withContext(Dispatchers.IO) {
            return@withContext db.entrancePhotoDao().getEntrancePhoto(taskItem.id, entrance.number).map {
                it.toModel(db)
            }
        }

    suspend fun insertEntranceResult(
        taskItem: TaskItemModel, entrance: EntranceModel,
        hasLookupPost: Boolean? = null,
        isDeliveryWrong: Boolean? = null,
        description: String? = null,
        code: String? = null,
        apartmentFrom: Int? = null,
        apartmentTo: Int? = null,
        floors: Int? = null
    ) = withContext(Dispatchers.IO) {
        var saved = db.entranceResultDao().getByEntrance(taskItem.id, entrance.number)
        if (saved == null) {
            db.entranceResultDao().insert(
                EntranceResultEntity(
                    0,
                    taskItem.id,
                    entrance.number,
                    hasLookupPost,
                    isDeliveryWrong,
                    description,
                    code,
                    apartmentFrom,
                    apartmentTo,
                    floors
                )
            )
            return@withContext
        }

        if (hasLookupPost != null) saved = saved.copy(hasLookupPost = hasLookupPost)
        if (isDeliveryWrong != null) saved = saved.copy(isDeliveryWrong = isDeliveryWrong)
        if (description != null) saved = saved.copy(description = description)
        if (code != null) saved = saved.copy(code = code)
        if (apartmentFrom != null) saved = saved.copy(apartmentFrom = apartmentFrom)
        if (apartmentTo != null) saved = saved.copy(apartmentTo = apartmentTo)
        if (floors != null) saved = saved.copy(floors = floors)

        db.entranceResultDao().update(saved)
    }

    suspend fun loadEntranceResult(taskItem: TaskItemModel, entrance: EntranceModel): EntranceResultEntity? =
        withContext(Dispatchers.IO) {
            db.entranceResultDao().getByEntrance(taskItem.id, entrance.number)
        }

    data class MergeResult(
        var isTasksChanged: Boolean,
        var isNewTasksAdded: Boolean
    )
}