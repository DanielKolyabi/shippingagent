package ru.relabs.kurjercontroller.providers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.models.AddressModel
import ru.relabs.kurjercontroller.models.EntranceModel
import ru.relabs.kurjercontroller.models.TaskItemModel
import ru.relabs.kurjercontroller.models.TaskModel
import ru.relabs.kurjercontroller.providers.interfaces.ITaskRepository
import kotlin.random.Random

/**
 * Created by ProOrange on 20.03.2019.
 */

class MockTaskRepository : ITaskRepository {
    private val taskItems = List(100) { i ->
        TaskItemModel(
            i,
            i % 10,
            AddressModel(
                i,
                i,
                "Москва",
                "Проспект Длинного",
                i,
                i.toString(),
                0.0,
                0.0
            ),
            TaskItemModel.CREATED,
            listOf("note1", "note2"),
            List(Random(0).nextInt(4)) { j ->
                EntranceModel(
                    i * 100 + j,
                    j * 100,
                    j * 100 + 100,
                    listOf("1234", "4321", "3214", "2341"),
                    listOf("1234", "4321", "3214", "2341"),
                    "3345"
                )
            }
        )
    }
    private val tasks = List(10) { i ->
        TaskModel(
            i,
            1,
            "Курчанов Д.О.",
            "Вечерняя Москва",
            i,
            DateTime(2019, 3, 10+i%3, 0, 0, 0),
            DateTime(2019, 3, 11+i%3, 0, 0, 0),
            DateTime(2019, 2, 10+i%3, 0, 0, 0),
            DateTime(2019, 3, 10+i%3, 0, 0, 0),
            listOf(
                "Москва, ул. Пушкина, д. 36 слева ларёк, в ларьке пенёк, в пеньке яйцо, в яйце - материалы",
                "Москва, ул. Колотушкина, д. 36 справа ларёк, в ларьке огонёк, в огоньке яйцо, в яйце - материалы"
            ),
            "За этими глаз да глаз, проверить нужно всё!1!",
            taskItems.filter { it.taskId == i }
        )
    }

    override suspend fun getTasks(): List<TaskModel> = withContext(Dispatchers.Main) {
        delay(1000)
        return@withContext tasks
    }

    override suspend fun getTaskItems(taskId: Int): List<TaskItemModel> = withContext(Dispatchers.Main) {
        delay(1000)
        return@withContext taskItems.filter { it.taskId == taskId }
    }

    override suspend fun getTask(taskId: Int): TaskModel = withContext(Dispatchers.Main) {
        delay(1000)
        return@withContext tasks.first { it.id == taskId }
    }

    override suspend fun getTaskItem(taskItemId: Int): TaskItemModel = withContext(Dispatchers.Main) {
        delay(1000)
        return@withContext taskItems.first { it.id == taskItemId }
    }
}