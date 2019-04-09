package ru.relabs.kurjercontroller.providers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.models.*
import ru.relabs.kurjercontroller.providers.interfaces.ITaskRepository
import kotlin.random.Random

/**
 * Created by ProOrange on 20.03.2019.
 */

class MockTaskRepository : ITaskRepository {
    override suspend fun closeAllTasks() = withContext(Dispatchers.IO){

    }

    private val taskItems = MutableList(100) { i ->
        TaskItemModel(
            i,
            i % 10,
            AddressModel(
                i % 35,
                i % 35,
                "Москва",
                "Проспект Длинного",
                i % 35,
                (i % 35).toString(),
                0.0,
                0.0
            ),
            listOf("note1", "note2"),
            List(Random(0).nextInt(4)) { j ->
                EntranceModel(
                    i * 100 + j,
                    j * 100,
                    j * 100 + 100,
                    listOf("1234", "4321", "3214", "2341"),
                    listOf("1234", "4321", "3214", "2341"),
                    "3345",
                    9,
                    EntranceModel.CREATED
                )
            }
        )
    }
    private val tasks = MutableList(10) { i ->
        TaskModel(
            i,
            1,
            "Курчанов Д.О.",
            "Вечерняя Москва",
            i,
            DateTime(2019, 3, 10 + i % 3, 0, 0, 0),
            DateTime(2019, 3, 11 + i % 3, 0, 0, 0),
            DateTime(2019, 2, 10 + i % 3, 0, 0, 0),
            DateTime(2019, 3, 10 + i % 3, 0, 0, 0),
            listOf(
                "Москва, ул. Пушкина, д. 36 слева ларёк, в ларьке пенёк, в пеньке яйцо, в яйце - материалы",
                "Москва, ул. Колотушкина, д. 36 справа ларёк, в ларьке огонёк, в огоньке яйцо, в яйце - материалы"
            ).joinToString("; "),
            "За этими глаз да глаз, проверить нужно всё!1!",
            taskItems.filter { it.taskId == i },
            null,
            0
        )
    }.apply {
        add(TaskModel(
            15,
            1,
            "Курчанов Д.О.",
            "Задача Фильтров",
            1,
            DateTime(2019, 3, 10 + 1 % 3, 0, 0, 0),
            DateTime(2019, 3, 11 + 1 % 3, 0, 0, 0),
            DateTime(2019, 2, 10 + 1 % 3, 0, 0, 0),
            DateTime(2019, 3, 10 + 1 % 3, 0, 0, 0),
            listOf(
                "Москва, ул. Пушкина, д. 36 слева ларёк, в ларьке пенёк, в пеньке яйцо, в яйце - материалы",
                "Москва, ул. Колотушкина, д. 36 справа ларёк, в ларьке огонёк, в огоньке яйцо, в яйце - материалы"
            ).joinToString("; "),
            "За этими глаз да глаз, проверить нужно всё!1!",
            listOf(),
            TaskFiltersModel(
                mutableListOf(
                    FilterModel(
                        1, "Вечерняя Москва №1", true
                    )
                ),
                mutableListOf(
                    FilterModel(
                        1, "Бригада №1", true
                    )
                ),
                mutableListOf(),
                mutableListOf(),
                mutableListOf()
            ),
            0
        ))
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