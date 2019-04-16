package ru.relabs.kurjercontroller.providers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.BuildConfig
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.application.UserModel
import ru.relabs.kurjercontroller.database.AppDatabase
import ru.relabs.kurjercontroller.database.entities.SendQueryItemEntity
import ru.relabs.kurjercontroller.models.EntrancePhotoModel
import ru.relabs.kurjercontroller.models.TaskItemModel
import ru.relabs.kurjercontroller.models.TaskModel
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
        if (task.state != TaskModel.CREATED) return@withContext task
        val updatedTask = task.copy(state = TaskModel.EXAMINED)
        db.taskDao().update(updatedTask.toEntity())

        db.sendQueryDao().insert(SendQueryItemEntity(
            0,
            BuildConfig.API_URL + "/api/v1/controller/tasks/${task.id}/examined?token=" + application().user.getUserCredentials()?.token.orEmpty(),
            ""
        ))

        return@withContext updatedTask
    }

    suspend fun mergeTasks(newTasks: List<TaskModel>) = withContext(Dispatchers.IO) {
        //TODO: Make merge strategy
        closeAllTasks()
        newTasks.forEach { saveTaskToDB(it) }
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
            db.taskPublisherDao().getByTaskId(task.id)
                .forEach { db.taskPublisherDao().delete(it) }

            db.taskItemDao().getByTaskId(task.id)
                .forEach { taskItem ->
                    db.entranceDao().getByTaskItemId(taskItem.id)
                        .forEach {
                            db.entranceDao().delete(it)
                        }

                    db.addressDao().deleteById(taskItem.addressId)

                    db.taskItemDao().delete(taskItem)
                }

            db.taskDao().delete(task)
        }
    }

    suspend fun removePhoto(entrancePhoto: EntrancePhotoModel) = withContext(Dispatchers.IO){
        db.entrancePhotoDao().deleteById(entrancePhoto.id)
    }

    suspend fun savePhoto(entrancePhoto: EntrancePhotoModel) = withContext(Dispatchers.IO){
        db.entrancePhotoDao().insert(entrancePhoto.toEntity())
    }
}