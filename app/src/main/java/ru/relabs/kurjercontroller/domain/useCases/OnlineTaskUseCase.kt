package ru.relabs.kurjercontroller.domain.useCases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskFilters
import ru.relabs.kurjercontroller.domain.repositories.ControlRepository
import ru.relabs.kurjercontroller.domain.repositories.DatabaseRepository
import ru.relabs.kurjercontroller.utils.Either
import ru.relabs.kurjercontroller.utils.Left
import ru.relabs.kurjercontroller.utils.fmap

class OnlineTaskUseCase(
    private val controlRepository: ControlRepository,
    private val databaseRepository: DatabaseRepository
) {
    suspend fun createOnlineTask(filters: TaskFilters, withPlanned: Boolean): Either<Exception, Task> =
        withContext(Dispatchers.IO) {
            val task = databaseRepository.createOnlineTask(filters, withPlanned)
            updateFilteredTaskItems(task)
        }

    suspend fun updateFilteredTaskItems(task: Task): Either<Exception, Task> = withContext(Dispatchers.IO){
        controlRepository.getFilteredTaskItems(task.taskFilters.all, task.withPlanned).fmap {
            val task = Task(
                id = task.id,
                state = task.state,
                startControlDate = task.startControlDate,
                endControlDate = task.endControlDate,
                description = task.description,
                initiator = task.initiator,
                userId = task.userId,
                storages = it.storages,
                taskItems = it.items.map { it.copy(taskId = task.id) },
                publishers = it.publishers.map { it.copy(taskId = task.id) },
                taskFilters = task.taskFilters,
                iteration = task.iteration,
                firstExaminedDeviceId = task.firstExaminedDeviceId,
                filtered = task.filtered,
                isOnline = false,
                withPlanned = false
            )
            databaseRepository.reloadFilteredTaskItems(task)
            task
        }
    }
}