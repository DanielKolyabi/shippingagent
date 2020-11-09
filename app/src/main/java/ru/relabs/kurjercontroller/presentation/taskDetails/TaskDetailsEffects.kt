package ru.relabs.kurjercontroller.presentation.taskDetails

import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.presentation.RootScreen
import ru.relabs.kurjercontroller.presentation.fragmentsOld.yandexMap.base.WRONG_METHOD_OUTLINE_COLOR
import ru.relabs.kurjercontroller.presentation.yandexMap.models.AddressWithColor

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */
object TaskDetailsEffects {

    fun effectNavigateBack(): TaskDetailsEffect = { c, s ->
        withContext(Dispatchers.Main) {
            c.router.exit()
        }
    }

    fun effectNavigateTaskItemDetails(taskItem: TaskItem): TaskDetailsEffect = { c, s ->
        withContext(Dispatchers.Main) {
            c.router.navigateTo(RootScreen.TaskItemDetails(taskItem))
        }
    }

    fun effectExamine(): TaskDetailsEffect = { c, s ->
        messages.send(TaskDetailsMessages.msgAddLoaders(1))
        when (val t = s.task) {
            null -> c.showFatalError("tde:101")
            else -> {
                c.onExamine(c.database.examineTask(t))
                withContext(Dispatchers.Main) {
                    c.router.exit()
                }
            }
        }
        messages.send(TaskDetailsMessages.msgAddLoaders(-1))
    }

    fun effectOpenMap(): TaskDetailsEffect = { c, s ->
        val consumer = c.addressClickedConsumer()
        if (s.task == null || consumer == null) {
            FirebaseCrashlytics.getInstance().log("task or consmer is null")
        } else {
            c.router.navigateTo(
                RootScreen.AddressMap(
                    s.task.taskItems.map {
                        AddressWithColor(
                            it.address,
                            it.placemarkColor,
                            if (it.wrongMethod) WRONG_METHOD_OUTLINE_COLOR else it.placemarkColor
                        )
                    },
                    s.task.taskItems.map { it.deliverymanId },
                    s.task.storages,
                    consumer
                )
            )
        }
    }
}