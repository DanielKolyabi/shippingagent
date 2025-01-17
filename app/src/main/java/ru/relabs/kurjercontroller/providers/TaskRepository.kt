//package ru.relabs.kurjercontroller.providers
//
//import android.util.Log
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import org.joda.time.DateTime
//import ru.relabs.kurjercontroller.BuildConfig
//import ru.relabs.kurjercontroller.application
//import ru.relabs.kurjercontroller.data.database.AppDatabase
//import ru.relabs.kurjercontroller.data.database.entities.*
//import ru.relabs.kurjercontroller.data.database.models.ApartmentResult
//import ru.relabs.kurjercontroller.domain.mappers.database.DatabaseEntranceMapper
//import ru.relabs.kurjercontroller.domain.models.*
//import ru.relabs.kurjercontroller.fileHelpers.PathHelper
//import ru.relabs.kurjercontroller.presentation.fragmentsOld.report.models.ApartmentListModel
//import ru.relabs.kurjercontroller.utils.CustomLog
//
///**
// * Created by ProOrange on 11.04.2019.
// */
//class TaskRepository(val db: AppDatabase) {
//    private var availableEntranceKeys: List<String> = listOf()
//    private var availableEntranceEuroKeys: List<String> = listOf()
//
//    suspend fun loadRemoteTasks(token: String): List<TaskModel> = withContext(Dispatchers.IO) {
//        return@withContext DeliveryServerAPI.api.getTasks(
//            token,
//            DateTime().toString("yyyy-MM-dd'T'HH:mm:ss")
//        ).await()
//            .map { it.toModel() }
//    }
//
//    private suspend fun taskChangeStatus(task: TaskModel, status: Int): TaskModel =
//        withContext(Dispatchers.IO) {
//
//            val endpoint = when (status) {
//                TaskModel.CREATED -> "received"
//                TaskModel.EXAMINED -> "examined"
//                TaskModel.STARTED -> "accepted"
//                TaskModel.COMPLETED -> "completed"
//                else -> return@withContext task
//            }
//
//            val updatedTask = task.copy(state = status.toSiriusState())
//            db.taskDao().update(updatedTask.toEntity())
//
//            db.sendQueryDao().insert(
//                SendQueryItemEntity(
//                    0,
//                    BuildConfig.API_URL + "/api/v1/controller/tasks/${task.id}/$endpoint?token=" + application().user.getUserCredentials()?.token.orEmpty(),
//                    ""
//                )
//            )
//
//            return@withContext updatedTask
//        }
//
//    suspend fun receiveTaskStatus(task: TaskModel): TaskModel =
//        taskChangeStatus(task, TaskModel.CREATED)
//
//    suspend fun examineTaskStatus(task: TaskModel): TaskModel =
//        taskChangeStatus(task, TaskModel.EXAMINED)
//
//    suspend fun startTaskStatus(task: TaskModel): TaskModel =
//        taskChangeStatus(task, TaskModel.STARTED)
//
//    suspend fun closeTaskStatus(task: TaskModel): TaskModel =
//        taskChangeStatus(task, TaskModel.COMPLETED)
//
//    suspend fun mergeTasks(newTasks: List<TaskModel>): MergeResult = withContext(Dispatchers.IO) {
//        //        Log.d("Merge", "Start " + DateTime.now().millis.toString())
//        val result = merge(newTasks) {
//            if (it.state.toAndroidState() == TaskModel.CREATED) {
//                receiveTaskStatus(it)
//            }
//        }
////        Log.d("Merge", "End " + DateTime.now().millis.toString())
//
//        return@withContext result
//    }
//
//    suspend fun merge(
//        newTasks: List<TaskModel>,
//        onNewTaskAppear: suspend (task: TaskModel) -> Unit
//    ): MergeResult {
//
//        val savedTasksIDs = db.taskDao().all.map { it.id }
//        val newTasksIDs = newTasks.map { it.id }
//        val appearedTasks = mutableListOf<TaskModel>()
//        val result = MergeResult(false, false)
//
//        //Задача отсутствует в ответе от сервера (удалено)
//        db.taskDao().all.filter { it.id !in newTasksIDs && !it.isOnline }.forEach { task ->
//            closeTaskById(task.id)
//            result.isTasksChanged = true
//            Log.d("merge", "Close task: ${task.id}")
//        }
//
//        //Задача не присутствует в сохранённых (новая)
//        newTasks.filter { it.id !in savedTasksIDs }.forEach { task ->
//            if (task.androidState == TaskModel.CANCELED) {
//                Log.d("merge", "New task ${task.id} passed due 12 status")
//                return@forEach
//            }
//            //Add task
//            val newTaskId = db.taskDao().insert(task.toEntity())
//            Log.d("merge", "Add task ID: $newTaskId")
//            var openedEntrances = 0
//            db.taskStorageDao().insertAll(task.storages.map { it.toEntity(task.id) })
//            db.taskPublisherDao().insertAll(task.publishers.map { it.toEntity() })
//            if (!task.filtered) {
//                task.taskItems.forEach { item ->
//                    db.addressDao().insert(item.address.toEntity())
//                    db.taskItemDao().insert(item.toEntity())
//                    item.entrances.forEach { entrance ->
//                        val entity = DatabaseEntranceMapper.toEntity(entrance, item.taskId, item.id)
//                        if (db.entranceReportDao().getByNumber(
//                                entity.taskItemId,
//                                entity.number
//                            ) != null
//                        ) {
//                            entity.state = Entrance.CLOSED
//                        }
//                        db.entranceDao().insert(entity)
//                        if (entity.state == Entrance.CREATED) {
//                            openedEntrances++
//                        }
//                    }
//                }
//                if (openedEntrances <= 0) {
//                    closeTaskById(newTaskId.toInt())
//                } else {
//                    result.isNewTasksAdded = true
//                    appearedTasks.add(task)
//                }
//            } else {
//                saveFilters(task)
//
//                result.isNewTasksAdded = true
//                appearedTasks.add(task)
//            }
//        }
//
//        //Задача есть и на сервере и на клиенте (мерж)
//        /*
//        Если она закрыта | выполнена на сервере - удалить с клиента
//        Если итерация > сохранённой | состояние отличается от сохранённого и сохранённое != начато |
//         */
//        newTasks.filter { it.id in savedTasksIDs }.forEach { task ->
//            val savedTask = db.taskDao().getById(task.id) ?: return@forEach
//            //Игнорируем задания с фильтрами, т.к. нет возможности на месте получить список TaskItems
//
//            if (task.androidState == TaskModel.CANCELED) {
//                if (savedTask.state.toAndroidState() == TaskModel.STARTED) {
//                    //Уведомили что задание уже в работе
//                    db.sendQueryDao().insert(
//                        SendQueryItemEntity(
//                            0,
//                            BuildConfig.API_URL + "/api/v1/controller/tasks/${savedTask.id}/accepted?token=" + application().user.getUserCredentials()?.token.orEmpty(),
//                            ""
//                        )
//                    )
//                } else {
//                    closeTaskById(savedTask.id)
//                }
//            } else if (task.androidState == TaskModel.COMPLETED) {
//                closeTaskById(savedTask.id)
//                return@forEach
//            } else if (
//                (savedTask.iteration < task.iteration)
//                || (task.state != savedTask.state && savedTask.state.toAndroidState() != TaskModel.STARTED)
//            ) {
//                db.taskDao().update(task.toEntity())
//                db.taskStorageDao().insertAll(task.storages.map { it.toEntity(task.id) })
//                db.taskPublisherDao().insertAll(task.publishers.map { it.toEntity() })
//
//                val currentTaskItems = db.taskItemDao().getByTaskId(task.id).toMutableList()
//
//                task.taskItems.forEach { taskItem ->
//                    currentTaskItems.removeAll { oldTaskItem -> oldTaskItem.taskItemId == taskItem.id && oldTaskItem.taskId == taskItem.taskId }
//
//                    db.addressDao().insert(taskItem.address.toEntity())
//                    db.taskItemDao().insert(taskItem.toEntity())
//                    taskItem.entrances.forEach { entrance ->
//                        val entity = DatabaseEntranceMapper.toEntity(entrance, item.taskId, item.id)
//                        if (db.entranceReportDao().getByNumber(
//                                entity.taskItemId,
//                                entity.number
//                            ) != null
//                        ) {
//                            entity.state = Entrance.CLOSED
//                        }
//                        db.entranceDao().insert(entity)
//                    }
//                }
//
//                currentTaskItems.forEach {
//                    removeTaskItem(it)
//                }
//                result.isTasksChanged = true
//            } else {
//                //MERGE CLOSE TIMINGS
//                task.taskItems.forEach { taskItem ->
//                    val savedItem = db.taskItemDao().getByTaskItemId(taskItem.taskId, taskItem.id)
//                    if (savedItem != null && savedItem.closeTime == null && taskItem.closeTime != null) {
//                        db.taskItemDao()
//                            .insert(savedItem.copy(closeTime = taskItem.closeTime, isNew = true))
//                    }
//                }
//            }
//        }
//
//        appearedTasks.forEach { onNewTaskAppear(it) }
//        return result
//    }
//
//    private suspend fun removeTaskItem(taskItem: TaskItemEntity) {
//        db.entranceDao().getByTaskItemId(taskItem.taskId, taskItem.taskItemId).forEach { entrance ->
//            db.entranceResultDao()
//                .deleteByEntrance(taskItem.taskId, taskItem.taskItemId, entrance.number)
//            db.entrancePhotoDao()
//                .getEntrancePhoto(taskItem.taskId, taskItem.taskItemId, entrance.number)
//                .forEach { photo ->
//                    removePhoto(photo.toModel(this))
//                }
//            db.apartmentResultDao()
//                .deleteByEntrance(taskItem.taskId, taskItem.taskItemId, entrance.number)
//            db.entranceReportDao()
//                .deleteByEntrance(taskItem.taskId, taskItem.taskItemId, entrance.number)
//            db.entranceDao().delete(entrance)
//        }
//        db.taskItemDao().delete(taskItem)
//    }
//
//    suspend fun saveFilters(
//        task: TaskModel,
//        filters: TaskFilters = task.taskFilters,
//        withPlanned: Boolean = task.withPlanned
//    ) =        withContext(Dispatchers.IO) {
//            db.taskDao().getById(task.id)?.let {
//                db.taskDao().update(it.copy(withPlanned = withPlanned))
//            }
//
//            db.filtersDao().deleteByTaskId(task.id)
//
//            db.filtersDao().insertAll(filters.brigades.map {
//                FilterEntity(
//                    0,
//                    task.id,
//                    FilterEntity.BRIGADE_FILTER,
//                    it.id,
//                    it.name,
//                    it.fixed,
//                    it.active
//                )
//            })
//            db.filtersDao().insertAll(filters.publishers.map {
//                FilterEntity(
//                    0,
//                    task.id,
//                    FilterEntity.PUBLISHER_FILTER,
//                    it.id,
//                    it.name,
//                    it.fixed,
//                    it.active
//                )
//            })
//            db.filtersDao().insertAll(filters.users.map {
//                FilterEntity(
//                    0,
//                    task.id,
//                    FilterEntity.USER_FILTER,
//                    it.id,
//                    it.name,
//                    it.fixed,
//                    it.active
//                )
//            })
//            db.filtersDao().insertAll(filters.districts.map {
//                FilterEntity(
//                    0,
//                    task.id,
//                    FilterEntity.DISTRICT_FILTER,
//                    it.id,
//                    it.name,
//                    it.fixed,
//                    it.active
//                )
//            })
//            db.filtersDao().insertAll(filters.regions.map {
//                FilterEntity(
//                    0,
//                    task.id,
//                    FilterEntity.REGION_FILTER,
//                    it.id,
//                    it.name,
//                    it.fixed,
//                    it.active
//                )
//            })
//        }
//
//    suspend fun removeReport(db: AppDatabase, report: EntranceReportEntity) =
//        withContext(Dispatchers.IO) {
//            db.entranceReportDao().delete(report)
//            db.entrancePhotoDao()
//                .getEntrancePhoto(report.taskId, report.taskItemId, report.entranceNumber)
//                .forEach {
//                    //Delete photo
//                    removePhoto(it.toModel(this@TaskRepository))
//                }
//        }
//
//    suspend fun getTasks(): List<TaskModel> = withContext(Dispatchers.IO) {
//        return@withContext db.taskDao().all.map { it.toModel(this@TaskRepository) }
//    }
//
//    suspend fun getAddress(addressId: Int): Address? = withContext(Dispatchers.IO) {
//        return@withContext db.addressDao().getById(addressId)?.toModel()
//    }
//
//    suspend fun getTask(taskId: Int): TaskModel? = withContext(Dispatchers.IO) {
//        return@withContext db.taskDao().getById(taskId)?.toModel(this@TaskRepository)
//    }
//
//    suspend fun getTaskItem(taskId: Int, taskItemId: Int): TaskItem? =
//        withContext(Dispatchers.IO) {
//            return@withContext db.taskItemDao().getByTaskItemId(taskId, taskItemId)
//                ?.toModel(this@TaskRepository)
//        }
//
//    suspend fun getTaskItems(taskItemId: Int): List<TaskItem> = withContext(Dispatchers.IO) {
//        return@withContext db.taskItemDao().getAllByTaskItemId(taskItemId)
//            .map { it.toModel(this@TaskRepository) }
//    }
//
//    suspend fun closeAllTasks() = withContext(Dispatchers.IO) {
//        db.taskDao().all.forEach { task ->
//            closeTaskById(task.id)
//        }
//    }
//
//    suspend fun closeTaskById(taskId: Int) = withContext(Dispatchers.IO) {
//        db.taskPublisherDao().deleteByTaskId(taskId)
//        db.filtersDao().deleteByTaskId(taskId)
//        db.taskItemDao().getByTaskId(taskId)
//            .forEach { taskItem ->
//                db.entranceDao().getByTaskItemId(taskItem.taskId, taskItem.taskItemId)
//                    .forEach {
//                        deleteEntrance(taskItem.taskId, taskItem.taskItemId, it.number)
//                    }
//
//                safeDeleteAddress(taskItem.addressId)
//
//                db.taskItemDao().delete(taskItem)
//            }
//
//        db.taskDao().deleteById(taskId)
//    }
//
//    suspend fun safeDeleteAddress(addressId: Int) {
//        if (db.taskItemDao().getByAddressId(addressId).isEmpty()) {
//            db.addressDao().deleteById(addressId)
//        }
//    }
//
//    suspend fun deleteEntrance(taskId: Int, taskItemId: Int, entranceNumber: Int) =
//        withContext(Dispatchers.IO) {
//            val entity = db.entranceDao().getByNumber(taskId, taskItemId, entranceNumber)
//                ?: return@withContext
//            db.entranceDao().delete(entity)
//            db.entranceResultDao().deleteByEntrance(entity.taskId, entity.taskItemId, entity.number)
//            db.apartmentResultDao()
//                .deleteByEntrance(entity.taskId, entity.taskItemId, entity.number)
//        }
//
//    suspend fun closeEntrance(taskId: Int, taskItemId: Int, entranceNumber: Int) =
//        withContext(Dispatchers.IO) {
//            val entity = db.entranceDao().getByNumber(taskId, taskItemId, entranceNumber) ?: run {
//                CustomLog.writeToFile("closeEntrance: Can't find entrance #$entranceNumber in tid: $taskItemId")
//                return@withContext
//            }
//            db.entranceDao().update(entity.copy(state = Entrance.CLOSED))
//
//            val taskItem = db.taskItemDao().getByTaskItemId(taskId, taskItemId) ?: run {
//                CustomLog.writeToFile("closeEntrance: Can't find taskItem $taskItemId")
//                return@withContext
//            }
//            val task = db.taskDao().getById(taskItem.taskId)?.toModel(this@TaskRepository) ?: run {
//                CustomLog.writeToFile("closeEntrance: Can't find task ${taskItem.taskId}")
//                return@withContext
//            }
//
//            if (task.state.toAndroidState() != TaskModel.STARTED) {
//                startTaskStatus(task)
//            }
//        }
//
//    suspend fun loadTaskFilters(taskId: Int): TaskFilters = withContext(Dispatchers.IO) {
//        val filters = db.filtersDao().getByTaskId(taskId).groupBy { it.type }.mapValues { entry ->
//            entry.value.map { TaskFilter(it.filterId, it.name, it.fixed, it.active, it.type) }
//                .toMutableList()
//        }
//
//        return@withContext TaskFilters(
//            filters.getOrElse(FilterEntity.PUBLISHER_FILTER, { mutableListOf() }),
//            filters.getOrElse(FilterEntity.DISTRICT_FILTER, { mutableListOf() }),
//            filters.getOrElse(FilterEntity.REGION_FILTER, { mutableListOf() }),
//            filters.getOrElse(FilterEntity.BRIGADE_FILTER, { mutableListOf() }),
//            filters.getOrElse(FilterEntity.USER_FILTER, { mutableListOf() })
//        )
//    }
//
//    suspend fun removePhoto(entrancePhoto: EntrancePhotoModel) = withContext(Dispatchers.IO) {
//        db.entrancePhotoDao().deleteById(entrancePhoto.id)
//        if (db.entrancePhotoDao().getByUUID(entrancePhoto.uuid).isEmpty()) {
//            PathHelper.deletePhoto(entrancePhoto)
//        }
//    }
//
//    suspend fun savePhoto(entrancePhoto: EntrancePhotoModel): Long = withContext(Dispatchers.IO) {
//        return@withContext db.entrancePhotoDao().insert(entrancePhoto.toEntity())
//    }
//
//    suspend fun loadEntrancePhotos(
//        taskItem: TaskItem,
//        entrance: Entrance
//    ): List<EntrancePhotoModel> = withContext(Dispatchers.IO) {
//        return@withContext db.entrancePhotoDao()
//            .getEntrancePhoto(taskItem.taskId, taskItem.id, entrance.number)
//            .map { it.toModel(this@TaskRepository) }
//    }
//
//    suspend fun insertEntranceResult(
//        taskItem: TaskItem, entrance: Entrance,
//        hasLookupPost: Boolean? = null,
//        isDeliveryWrong: Boolean? = null,
//        description: String? = null,
//        code: String? = null,
//        apartmentFrom: Int? = null,
//        apartmentTo: Int? = null,
//        floors: Int? = null,
//        key: String? = null,
//        euroKey: String? = null,
//        mailboxType: Int? = null,
//        entranceClosed: Boolean? = null
//    ) = withContext(Dispatchers.IO) {
//        var saved =
//            db.entranceResultDao().getByEntrance(taskItem.taskId, taskItem.id, entrance.number)
//        if (saved == null) {
//            db.entranceResultDao().insert(
//                EntranceResultEntity(
//                    0,
//                    taskItem.taskId,
//                    taskItem.id,
//                    entrance.number,
//                    hasLookupPost,
//                    isDeliveryWrong,
//                    description,
//                    code,
//                    apartmentFrom,
//                    apartmentTo,
//                    floors,
//                    key,
//                    euroKey,
//                    mailboxType,
//                    entranceClosed
//                )
//            )
//            return@withContext
//        }
//
//        if (hasLookupPost != null) saved = saved.copy(hasLookupPost = hasLookupPost)
//        if (isDeliveryWrong != null) saved = saved.copy(isDeliveryWrong = isDeliveryWrong)
//        if (description != null) saved = saved.copy(description = description)
//        if (code != null) saved = saved.copy(code = code)
//        if (apartmentFrom != null) saved = saved.copy(apartmentFrom = apartmentFrom)
//        if (apartmentTo != null) saved = saved.copy(apartmentTo = apartmentTo)
//        if (floors != null) saved = saved.copy(floors = floors)
//        if (key != null) saved = saved.copy(key = key)
//        if (euroKey != null) saved = saved.copy(euroKey = euroKey)
//        if (mailboxType != null) saved = saved.copy(mailboxType = mailboxType)
//        if (entranceClosed != null) saved = saved.copy(entranceClosed = entranceClosed)
//
//        db.entranceResultDao().update(saved)
//    }
//
//    suspend fun loadEntranceResult(
//        taskItem: TaskItem,
//        entrance: Entrance
//    ): EntranceResultEntity? =
//        withContext(Dispatchers.IO) {
//            db.entranceResultDao().getByEntrance(taskItem.taskId, taskItem.id, entrance.number)
//        }
//
//    suspend fun saveApartmentResult(
//        taskItem: TaskItem,
//        entrance: Entrance,
//        apartment: ApartmentListModel.Apartment
//    ) = withContext(Dispatchers.IO) {
//        db.apartmentResultDao().insert(
//            ApartmentResultEntity(
//                0,
//                taskItem.taskId,
//                taskItem.id,
//                entrance.number,
//                apartment.number,
//                apartment.buttonGroup,
//                apartment.state,
//                apartment.description
//            )
//        )
//    }
//
//    suspend fun updateTaskItemButtonGroup(
//        taskItem: TaskItem,
//        buttonGroup: Int
//    ) = withContext(Dispatchers.IO) {
//        db.taskItemDao().getByTaskItemId(taskItem.taskId, taskItem.id)?.let {
//            db.taskItemDao().update(it.copy(defaultReportType = buttonGroup))
//        }
//
//        taskItem.entrances.map { it.number }.forEach { ent ->
//            db.apartmentResultDao().getByEntrance(taskItem.taskId, taskItem.id, ent)
//                .forEach { app ->
//                    db.apartmentResultDao().update(app.copy(buttonGroup = buttonGroup))
//                }
//        }
//    }
//
//    suspend fun saveApartmentResults(
//        taskItem: TaskItem,
//        entrance: Entrance,
//        apartments: List<ApartmentListModel.Apartment>
//    ) = withContext(Dispatchers.IO) {
//        db.apartmentResultDao().insertAll(
//            apartments.map { apartment ->
//                ApartmentResultEntity(
//                    0,
//                    taskItem.taskId,
//                    taskItem.id,
//                    entrance.number,
//                    apartment.number,
//                    apartment.buttonGroup,
//                    apartment.state,
//                    apartment.description
//                )
//            }
//        )
//    }
//
//    suspend fun loadEntranceApartments(
//        taskItem: TaskItem,
//        entrance: Entrance
//    ): List<ApartmentListModel.Apartment> = withContext(Dispatchers.IO) {
//        return@withContext db.apartmentResultDao()
//            .getByEntrance(taskItem.taskId, taskItem.id, entrance.number)
//            .map {
//                ApartmentListModel.Apartment(
//                    it.apartmentNumber,
//                    it.buttonGroup,
//                    it.buttonState,
//                    it.description
//                )
//            }
//    }
//
//    suspend fun loadEntranceApartment(
//        taskItem: TaskItem,
//        entrance: Entrance,
//        apartmentNumber: Int
//    ): ApartmentListModel.Apartment? = withContext(Dispatchers.IO) {
//        val data = db.apartmentResultDao().getByEntranceApartment(
//            taskItem.taskId,
//            taskItem.id,
//            entrance.number,
//            apartmentNumber
//        ) ?: return@withContext null
//        return@withContext ApartmentListModel.Apartment(
//            data.apartmentNumber,
//            data.buttonGroup,
//            data.buttonState,
//            data.description
//        )
//
//    }
//
//
//    suspend fun saveTaskReport(
//        taskItem: TaskItem,
//        entrance: Entrance,
//        publisher: TaskPublisher,
//        location: GPSCoordinatesModel
//    ) =
//        withContext(Dispatchers.IO) {
//            val entranceResult = loadEntranceResult(taskItem, entrance)
//            val apartmentResults = loadEntranceApartments(taskItem, entrance)
//
//            val report = EntranceReportEntity(
//                0,
//                taskItem.taskId,
//                taskItem.id,
//                taskItem.address.idnd,
//                entrance.number,
//                entranceResult?.apartmentFrom ?: entrance.startApartments,
//                entranceResult?.apartmentTo ?: entrance.endApartments,
//                entranceResult?.floors ?: entrance.floors,
//                entranceResult?.description ?: "",
//                entranceResult?.code ?: entrance.code,
//                entranceResult?.key ?: entrance.key,
//                entranceResult?.euroKey ?: entrance.euroKey,
//                entranceResult?.isDeliveryWrong ?: false,
//                entranceResult?.hasLookupPost ?: entrance.hasLookout ?: false,
//                application().user.getUserCredentials()?.token ?: "",
//                apartmentResults.map {
//                    ApartmentResult(
//                        it.number,
//                        it.state,
//                        it.buttonGroup,
//                        it.description
//                    )
//                },
//                DateTime.now(),
//                publisher.id,
//                entranceResult?.mailboxType ?: entrance.mailboxType,
//                location.lat,
//                location.long,
//                location.time,
//                entranceResult?.entranceClosed ?: false
//            )
//
//            db.entranceReportDao().insert(report)
//        }
//
//    suspend fun saveTaskItem(taskItem: TaskItem) = withContext(Dispatchers.IO) {
//        db.addressDao().insert(taskItem.address.toEntity())
//        db.taskItemDao().insert(taskItem.toEntity())
//        db.entranceDao()
//            .insertAll(taskItem.entrances.map {
//                val entity = DatabaseEntranceMapper.toEntity(it, taskItem.taskId, taskItem.id)
//            })
//    }
//
//    suspend fun reloadFilteredTaskItems(token: String, task: TaskModel): TaskModel =
//        withContext(Dispatchers.IO) {
//            val data = try {
//                api.getFilteredTaskItems(
//                    token,
//                    FiltersRequest.fromFiltersList(task.taskFilters.all, task.withPlanned)
//                ).await()
//            } catch (e: java.lang.Exception) {
//                e.logError()
//                e.fillInStackTrace()
//                throw e
//            }
//
//            val newTask = data.toModel(task)
//
//            //CLEAR
//            db.taskPublisherDao().deleteByTaskId(task.id)
//            db.taskItemDao().getByTaskId(task.id)
//                .forEach { taskItem ->
//                    db.entranceDao().getByTaskItemId(taskItem.taskId, taskItem.taskItemId)
//                        .forEach {
//                            deleteEntrance(taskItem.taskId, taskItem.taskItemId, it.number)
//                        }
//
//                    safeDeleteAddress(taskItem.addressId)
//
//                    db.taskItemDao().delete(taskItem)
//                }
//
//            //INSERT NEW
//            db.taskPublisherDao().insertAll(newTask.publishers.map { it.toEntity() })
//            db.taskStorageDao().insertAll(newTask.storages.map { it.toEntity(newTask.id) })
//            db.addressDao().insertAll(newTask.taskItems.map { it.address.toEntity() })
//            db.taskItemDao().insertAll(newTask.taskItems.map { it.toEntity() })
//            newTask.taskItems.forEach { taskItem ->
//
//                db.entranceDao()
//                    .insertAll(
//                        taskItem.entrances
//                            .map { it.toEntity(taskId = taskItem.taskId, taskItemId = taskItem.id) }
//                    )
//            }
//
//            return@withContext newTask
//        }
//
//    suspend fun getOnlineTask(): TaskModel? = withContext(Dispatchers.IO) {
//        db.taskDao().getOnlineTask()?.toModel(this@TaskRepository)
//    }
//
//    suspend fun isOnlineTaskExists(): Boolean = withContext(Dispatchers.IO) {
//        db.taskDao().getOnlineTask() != null
//    }
//
//    suspend fun createOnlineTask(filters: TaskFilters, withPlanned: Boolean): TaskModel =
//        withContext(Dispatchers.IO) {
//            db.taskDao().deleteOnlineTask()
//            val currentDate = DateTime()
//            val startDate = currentDate.minusMillis(currentDate.millisOfDay)
//            val entity = TaskEntity(
//                id = -1,
//                filtered = true,
//                userId = -1,
//                description = "Онлайн задание",
//                initiator = "Онлайн",
//                startControlDate = startDate,
//                endControlDate = startDate.plusDays(1),
//                firstExaminedDeviceId = null,
//                iteration = 0,
//                state = TaskModel.STARTED.toSiriusState(),
//                isOnline = true,
//                withPlanned = withPlanned
//            )
//            val taskId = db.taskDao().insert(entity)
//            db.filtersDao().insertAll(filters.all.map {
//                FilterEntity(
//                    id = 0,
//                    taskId = taskId.toInt(),
//                    active = it.active,
//                    fixed = it.fixed,
//                    filterId = it.id,
//                    name = it.name,
//                    type = it.type
//                )
//            })
//
//            return@withContext entity.toModel(this@TaskRepository)
//        }
//
//
//    data class MergeResult(
//        var isTasksChanged: Boolean,
//        var isNewTasksAdded: Boolean
//    )
//}