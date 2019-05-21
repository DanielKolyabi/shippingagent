package ru.relabs.kurjercontroller.providers

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.BuildConfig
import ru.relabs.kurjercontroller.CustomLog
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.database.AppDatabase
import ru.relabs.kurjercontroller.database.entities.*
import ru.relabs.kurjercontroller.database.models.ApartmentResult
import ru.relabs.kurjercontroller.fileHelpers.PathHelper
import ru.relabs.kurjercontroller.logError
import ru.relabs.kurjercontroller.models.*
import ru.relabs.kurjercontroller.network.DeliveryServerAPI
import ru.relabs.kurjercontroller.network.DeliveryServerAPI.api
import ru.relabs.kurjercontroller.ui.fragments.report.models.ApartmentListModel

/**
 * Created by ProOrange on 11.04.2019.
 */
class TaskRepository(val db: AppDatabase) {
    private var availableEntranceKeys: List<String> = listOf()
    private var availableEntranceEuroKeys: List<String> = listOf()

    suspend fun loadRemoteTasks(token: String): List<TaskModel> = withContext(Dispatchers.IO) {
        return@withContext DeliveryServerAPI.api.getTasks(token, DateTime().toString("yyyy-MM-dd'T'HH:mm:ss")).await()
            .map { it.toModel() }
    }

    private suspend fun taskChangeStatus(task: TaskModel, status: Int): TaskModel = withContext(Dispatchers.IO) {

        val endpoint = when (status) {
            TaskModel.CREATED -> "received"
            TaskModel.EXAMINED -> "examined"
            TaskModel.STARTED -> "accepted"
            TaskModel.COMPLETED -> "completed"
            else -> return@withContext task
        }

        val updatedTask = task.copy(state = status.toSiriusState())
        db.taskDao().update(updatedTask.toEntity())

        db.sendQueryDao().insert(
            SendQueryItemEntity(
                0,
                BuildConfig.API_URL + "/api/v1/controller/tasks/${task.id}/$endpoint?token=" + application().user.getUserCredentials()?.token.orEmpty(),
                ""
            )
        )

        return@withContext updatedTask
    }

    suspend fun receiveTaskStatus(task: TaskModel): TaskModel = taskChangeStatus(task, TaskModel.CREATED)
    suspend fun examineTaskStatus(task: TaskModel): TaskModel = taskChangeStatus(task, TaskModel.EXAMINED)
    suspend fun startTaskStatus(task: TaskModel): TaskModel = taskChangeStatus(task, TaskModel.STARTED)
    suspend fun closeTaskStatus(task: TaskModel): TaskModel = taskChangeStatus(task, TaskModel.COMPLETED)

    suspend fun mergeTasks(newTasks: List<TaskModel>): MergeResult = withContext(Dispatchers.IO) {
        //        Log.d("Merge", "Start " + DateTime.now().millis.toString())
        val result = merge(newTasks) {
            if (it.state.toAndroidState() == TaskModel.CREATED) {
                receiveTaskStatus(it)
            }
        }
//        Log.d("Merge", "End " + DateTime.now().millis.toString())

        return@withContext result
    }

    suspend fun merge(
        newTasks: List<TaskModel>,
        onNewTaskAppear: suspend (task: TaskModel) -> Unit
    ): MergeResult {

        val savedTasksIDs = db.taskDao().all.map { it.id }
        val newTasksIDs = newTasks.map { it.id }
        val appearedTasks = mutableListOf<TaskModel>()
        val result = MergeResult(false, false)

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
            if (!task.filtered) {
                task.taskItems.forEach { item ->
                    db.addressDao().insert(item.address.toEntity())
                    db.taskItemDao().insert(item.toEntity())
                    item.entrances.forEach { entrance ->
                        val entity = entrance.toEntity(item.taskId, item.id)
                        if (db.entranceReportDao().getByNumber(entity.taskItemId, entity.number) != null) {
                            entity.state = EntranceModel.CLOSED
                        }
                        db.entranceDao().insert(entity)
                        if (entity.state == EntranceModel.CREATED) {
                            openedEntrances++
                        }
                    }
                }
                if (openedEntrances <= 0) {
                    closeTaskById(newTaskId.toInt())
                } else {
                    result.isNewTasksAdded = true
                    appearedTasks.add(task)
                }
            } else {
                saveFilters(task)

                result.isNewTasksAdded = true
                appearedTasks.add(task)
            }
        }

        //Задача есть и на сервере и на клиенте (мерж)
        /*
        Если она закрыта | выполнена на сервере - удалить с клиента
        Если итерация > сохранённой | состояние отличается от сохранённого и сохранённое != начато |
         */
        newTasks.filter { it.id in savedTasksIDs }.forEach { task ->
            val savedTask = db.taskDao().getById(task.id) ?: return@forEach
            //Игнорируем задания с фильтрами, т.к. нет возможности на месте получить список TaskItems

            if (task.androidState == TaskModel.CANCELED) {
                if (savedTask.state.toAndroidState() == TaskModel.STARTED) {
                    //Уведомили что задание уже в работе
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
                || (task.state != savedTask.state && savedTask.state.toAndroidState() != TaskModel.STARTED)
            ) {

                db.taskDao().update(task.toEntity())
                db.taskPublisherDao().insertAll(task.publishers.map { it.toEntity() })
                task.taskItems.forEach { taskItem ->
                    db.addressDao().insert(taskItem.address.toEntity())
                    db.taskItemDao().insert(taskItem.toEntity())
                    taskItem.entrances.forEach { entrance ->
                        val entity = entrance.toEntity(taskItem.taskId, taskItem.id)
                        if (db.entranceReportDao().getByNumber(entity.taskItemId, entity.number) != null) {
                            entity.state = EntranceModel.CLOSED
                        }
                        db.entranceDao().insert(entity)
                    }
                }
                result.isTasksChanged = true
            }
        }

        appearedTasks.forEach { onNewTaskAppear(it) }
        return result
    }

    suspend fun saveFilters(task: TaskModel, filters: TaskFiltersModel = task.taskFilters) =
        withContext(Dispatchers.IO) {
            db.filtersDao().insertAll(filters.brigades.map {
                FilterEntity(0, task.id, FilterEntity.BRIGADE_FILTER, it.id, it.name, it.fixed, it.active)
            })
            db.filtersDao().insertAll(filters.publishers.map {
                FilterEntity(0, task.id, FilterEntity.PUBLISHER_FILTER, it.id, it.name, it.fixed, it.active)
            })
            db.filtersDao().insertAll(filters.users.map {
                FilterEntity(0, task.id, FilterEntity.USER_FILTER, it.id, it.name, it.fixed, it.active)
            })
            db.filtersDao().insertAll(filters.districts.map {
                FilterEntity(0, task.id, FilterEntity.DISTRICT_FILTER, it.id, it.name, it.fixed, it.active)
            })
            db.filtersDao().insertAll(filters.regions.map {
                FilterEntity(0, task.id, FilterEntity.REGION_FILTER, it.id, it.name, it.fixed, it.active)
            })
        }

    suspend fun removeReport(db: AppDatabase, report: EntranceReportEntity) = withContext(Dispatchers.IO) {
        db.entranceReportDao().delete(report)
        db.entrancePhotoDao().getEntrancePhoto(report.taskItemId, report.entranceNumber).forEach {
            //Delete photo
            removePhoto(it.toModel(this@TaskRepository))
        }
    }

    suspend fun getTasks(): List<TaskModel> = withContext(Dispatchers.IO) {
        return@withContext db.taskDao().all.map { it.toModel(this@TaskRepository) }
    }

    suspend fun getAddress(addressId: Int): AddressModel? = withContext(Dispatchers.IO) {
        return@withContext db.addressDao().getById(addressId)?.toModel()
    }

    suspend fun getTask(taskId: Int): TaskModel? = withContext(Dispatchers.IO) {
        return@withContext db.taskDao().getById(taskId)?.toModel(this@TaskRepository)
    }

    suspend fun closeAllTasks() = withContext(Dispatchers.IO) {
        db.taskDao().all.forEach { task ->
            closeTaskById(task.id)
        }
    }

    suspend fun closeTaskById(taskId: Int) = withContext(Dispatchers.IO) {
        db.taskPublisherDao().deleteByTaskId(taskId)
        db.filtersDao().deleteByTaskId(taskId)
        db.taskItemDao().getByTaskId(taskId)
            .forEach { taskItem ->
                db.entranceDao().getByTaskItemId(taskItem.taskId, taskItem.taskItemId)
                    .forEach {
                        deleteEntrance(taskItem.taskId, taskItem.taskItemId, it.number)
                    }

                db.addressDao().deleteById(taskItem.addressId)

                db.taskItemDao().delete(taskItem)
            }

        db.taskDao().deleteById(taskId)
    }

    suspend fun deleteEntrance(taskId: Int, taskItemId: Int, entranceNumber: Int) = withContext(Dispatchers.IO) {
        val entity = db.entranceDao().getByNumber(taskId, taskItemId, entranceNumber) ?: return@withContext
        db.entranceDao().delete(entity)
        db.entranceResultDao().deleteByEntrance(entity.taskId, entity.taskItemId, entity.number)
        db.apartmentResultDao().deleteByEntrance(entity.taskId, entity.taskItemId, entity.number)
    }

    suspend fun closeEntrance(taskId: Int, taskItemId: Int, entranceNumber: Int) = withContext(Dispatchers.IO) {
        val entity = db.entranceDao().getByNumber(taskId, taskItemId, entranceNumber) ?: run {
            CustomLog.writeToFile("closeEntrance: Can't find entrance #$entranceNumber in tid: $taskItemId")
            return@withContext
        }
        db.entranceDao().update(entity.copy(state = EntranceModel.CLOSED))

        val taskItem = db.taskItemDao().getByTaskItemId(taskId, taskItemId) ?: run {
            CustomLog.writeToFile("closeEntrance: Can't find taskItem $taskItemId")
            return@withContext
        }
        val task = db.taskDao().getById(taskItem.taskId)?.toModel(this@TaskRepository) ?: run {
            CustomLog.writeToFile("closeEntrance: Can't find task ${taskItem.taskId}")
            return@withContext
        }

        if (task.state.toAndroidState() != TaskModel.STARTED) {
            startTaskStatus(task)
        }
    }

    suspend fun loadTaskFilters(taskId: Int): TaskFiltersModel = withContext(Dispatchers.IO) {
        val filters = db.filtersDao().getByTaskId(taskId).groupBy { it.type }.mapValues { entry ->
            entry.value.map { FilterModel(it.filterId, it.name, it.fixed, it.active, it.type) }.toMutableList()
        }

        return@withContext TaskFiltersModel(
            filters.getOrElse(FilterEntity.PUBLISHER_FILTER, { mutableListOf() }),
            filters.getOrElse(FilterEntity.DISTRICT_FILTER, { mutableListOf() }),
            filters.getOrElse(FilterEntity.REGION_FILTER, { mutableListOf() }),
            filters.getOrElse(FilterEntity.BRIGADE_FILTER, { mutableListOf() }),
            filters.getOrElse(FilterEntity.USER_FILTER, { mutableListOf() })
        )
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
                it.toModel(this@TaskRepository)
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
        floors: Int? = null,
        key: String? = null,
        euroKey: String? = null,
        mailboxType: Int? = null,
        entranceClosed: Boolean? = null
    ) = withContext(Dispatchers.IO) {
        var saved = db.entranceResultDao().getByEntrance(taskItem.taskId, taskItem.id, entrance.number)
        if (saved == null) {
            db.entranceResultDao().insert(
                EntranceResultEntity(
                    0,
                    taskItem.taskId,
                    taskItem.id,
                    entrance.number,
                    hasLookupPost,
                    isDeliveryWrong,
                    description,
                    code,
                    apartmentFrom,
                    apartmentTo,
                    floors,
                    key,
                    euroKey,
                    mailboxType,
                    entranceClosed
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
        if (key != null) saved = saved.copy(key = key)
        if (euroKey != null) saved = saved.copy(euroKey = euroKey)
        if (mailboxType != null) saved = saved.copy(mailboxType = mailboxType)
        if (entranceClosed != null) saved = saved.copy(entranceClosed = entranceClosed)

        db.entranceResultDao().update(saved)
    }

    suspend fun loadEntranceResult(taskItem: TaskItemModel, entrance: EntranceModel): EntranceResultEntity? =
        withContext(Dispatchers.IO) {
            db.entranceResultDao().getByEntrance(taskItem.taskId, taskItem.id, entrance.number)
        }

    suspend fun saveApartmentResult(
        taskItem: TaskItemModel,
        entrance: EntranceModel,
        apartment: ApartmentListModel.Apartment
    ) = withContext(Dispatchers.IO) {
        db.apartmentResultDao().insert(
            ApartmentResultEntity(
                0,
                taskItem.taskId,
                taskItem.id,
                entrance.number,
                apartment.number,
                apartment.buttonGroup,
                apartment.state,
                apartment.description
            )
        )
    }


    suspend fun saveApartmentResults(
        taskItem: TaskItemModel,
        entrance: EntranceModel,
        apartments: List<ApartmentListModel.Apartment>
    ) = withContext(Dispatchers.IO) {
        db.apartmentResultDao().insertAll(
            apartments.map { apartment ->
                ApartmentResultEntity(
                    0,
                    taskItem.taskId,
                    taskItem.id,
                    entrance.number,
                    apartment.number,
                    apartment.buttonGroup,
                    apartment.state,
                    apartment.description
                )
            }
        )
    }

    suspend fun loadEntranceApartments(
        taskItem: TaskItemModel,
        entrance: EntranceModel
    ): List<ApartmentListModel.Apartment> = withContext(Dispatchers.IO) {
        return@withContext db.apartmentResultDao().getByEntrance(taskItem.taskId, taskItem.id, entrance.number)
            .map {
                ApartmentListModel.Apartment(
                    it.apartmentNumber,
                    it.buttonGroup,
                    it.buttonState,
                    it.description
                )
            }
    }

    suspend fun loadEntranceApartment(
        taskItem: TaskItemModel,
        entrance: EntranceModel,
        apartmentNumber: Int
    ): ApartmentListModel.Apartment? = withContext(Dispatchers.IO) {
        val data = db.apartmentResultDao().getByEntranceApartment(
            taskItem.taskId,
            taskItem.id,
            entrance.number,
            apartmentNumber
        ) ?: return@withContext null
        return@withContext ApartmentListModel.Apartment(
            data.apartmentNumber,
            data.buttonGroup,
            data.buttonState,
            data.description
        )

    }


    suspend fun saveTaskReport(
        taskItem: TaskItemModel,
        entrance: EntranceModel,
        publisher: PublisherModel,
        location: GPSCoordinatesModel
    ) =
        withContext(Dispatchers.IO) {
            val entranceResult = loadEntranceResult(taskItem, entrance)
            val apartmentResults = loadEntranceApartments(taskItem, entrance)

            val report = EntranceReportEntity(
                0,
                taskItem.taskId,
                taskItem.id,
                taskItem.address.idnd,
                entrance.number,
                entranceResult?.apartmentFrom ?: entrance.startApartments,
                entranceResult?.apartmentTo ?: entrance.endApartments,
                entranceResult?.floors ?: entrance.floors,
                entranceResult?.description ?: "",
                entranceResult?.code ?: entrance.code,
                entranceResult?.key ?: entrance.key,
                entranceResult?.euroKey ?: entrance.euroKey,
                entranceResult?.isDeliveryWrong ?: false,
                entranceResult?.hasLookupPost ?: false,
                application().user.getUserCredentials()?.token ?: "",
                apartmentResults.map { ApartmentResult(it.number, it.state, it.buttonGroup, it.description) },
                DateTime.now(),
                publisher.id,
                entranceResult?.mailboxType ?: entrance.mailboxType,
                location.lat,
                location.long,
                location.time,
                entranceResult?.entranceClosed ?: false
            )

            db.entranceReportDao().insert(report)
        }

    suspend fun getAvailableEntranceKeys(token: String = "", withRefresh: Boolean = false): List<String> =
        withContext(Dispatchers.IO) {
            if (!withRefresh && availableEntranceKeys.isEmpty()) {
                availableEntranceKeys = db.entranceKeysDao().all.map { it.key }
            }
            if (withRefresh || availableEntranceKeys.isEmpty()) {
                availableEntranceKeys = api.getAvailableEntranceKeys(token).await()
                db.entranceKeysDao().clear()
                db.entranceKeysDao().insertAll(availableEntranceKeys.map { EntranceKeyEntity(0, it) })
            }
            return@withContext availableEntranceKeys
        }

    suspend fun getAvailableEntranceEuroKeys(token: String = "", withRefresh: Boolean = false): List<String> =
        withContext(Dispatchers.IO) {
            if (!withRefresh && availableEntranceEuroKeys.isEmpty()) {
                availableEntranceEuroKeys = db.entranceEuroKeysDao().all.map { it.key }
            }
            if ((withRefresh || availableEntranceEuroKeys.isEmpty()) && token.isNotBlank()) {
                availableEntranceEuroKeys = try {
                    api.getAvailableEntranceEuroKeys(token).await()
                } catch (e: Exception) {
                    e.logError()
                    listOf()
                }
                db.entranceEuroKeysDao().clear()
                db.entranceEuroKeysDao().insertAll(availableEntranceEuroKeys.map { EntranceEuroKeyEntity(0, it) })
            }
            return@withContext availableEntranceEuroKeys
        }


    data class MergeResult(
        var isTasksChanged: Boolean,
        var isNewTasksAdded: Boolean
    )
}