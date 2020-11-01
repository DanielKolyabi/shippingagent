//package ru.relabs.kurjercontroller.presentation.fragmentsOld.report
//
//import kotlinx.android.synthetic.main.fragment_report_pager.*
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import ru.relabs.kurjercontroller.utils.CancelableScope
//import ru.relabs.kurjercontroller.activity
//import ru.relabs.kurjercontroller.ControllApplication
//import ru.relabs.kurjercontroller.domain.models.Entrance
//import ru.relabs.kurjercontroller.domain.models.TaskItem
//import ru.relabs.kurjercontroller.domain.models.TaskModel
//import ru.relabs.kurjercontroller.presentation.fragmentsOld.report.adapters.ReportPagerAdapter
//
///**
// * Created by ProOrange on 18.03.2019.
// */
//
//class ReportPagerPresenter(val fragment: ReportPagerFragment) {
//    val bgScope = CancelableScope(Dispatchers.Default)
//
////    fun getSelectedTask(): Pair<TaskModel, TaskItem> {
//////        val selectedTaskItem = fragment.taskItems.first {
//////            it.id == fragment.selectedTask.second && it.taskId == fragment.selectedTask.first
//////        }
//////        val selectedTask = fragment.tasks.first {
//////            it.id == selectedTaskItem.taskId
//////        }
//////        return Pair(selectedTask, selectedTaskItem)
////    }
//
//    fun updatePagerAdapter() {
////        val manager = fragment.fragmentManager ?: return
////
////        val selectedTask = getSelectedTask()
////        fragment.pagerAdapter = ReportPagerAdapter(
////            selectedTask.first,
////            selectedTask.second,
////            { task, taskItem, entrance ->
////                onEntranceClosed(task, taskItem, entrance)
////            },
////            {
////                fragment.taskItems.toList()
////            },
////            manager
////        )
////        fragment.view_pager?.adapter = fragment.pagerAdapter
////        fragment.view_pager?.currentItem = 0
//    }
//
//    fun onEntranceClosedRemote(taskId: Int, taskItemId: Int, entranceNumber: Int) {
//        val task = fragment.tasks.firstOrNull { it.id == taskId }
//        val taskItem = task?.taskItems?.firstOrNull { it.id == taskItemId }
//        val entrance = taskItem?.entrances?.firstOrNull { it.number == entranceNumber }
//        if(task != null && taskItem != null && entrance != null){
//
//            onEntranceClosed(task, taskItem, entrance, false)
//        }
//    }
//
//    private fun onEntranceClosed(
//        task: TaskModel,
//        taskItem: TaskItem,
//        entrance: Entrance,
//        useDatabase: Boolean = true
//    ) {
//        val location = application().currentLocation
//        bgScope.launch {
//            var shouldRefreshUI = true
//
//            if (useDatabase) {
//                application().tasksRepository.saveTaskReport(
//                    taskItem,
//                    entrance,
//                    task.publishers.firstOrNull { it.id == taskItem.publisherId } ?: task.publishers.first(),
//                    location
//                )
//
//                application().tasksRepository.closeEntrance(taskItem.taskId, taskItem.id, entrance.number)
//            }
//
//            val idx = fragment.taskItems.indexOfFirst {
//                it.id == taskItem.id && it.taskId == task.id
//            }
//            if (idx < 0) {
//                return@launch
//            }
//            val entranceIdx = fragment.taskItems[idx].entrances.indexOf(entrance)
//            if (entranceIdx < 0) {
//                return@launch
//            }
//
//            fragment.taskItems[idx].entrances[entranceIdx].state = Entrance.CLOSED
//
//            if (fragment.taskItems[idx].entrances.none { it.state == Entrance.CREATED }) {
//
//                fragment.taskItems.removeAt(idx)
//                fragment.tasks.removeAt(idx)
//
//                val taskItem = fragment.taskItems.firstOrNull()
//                if (taskItem == null) {
//                    shouldRefreshUI = false
//                    withContext(Dispatchers.Main) {
//                        application().router.exit()
//                    }
//                } else {
//                    fragment.selectedTask = Pair(taskItem.taskId, taskItem.id)
//                }
//            }
//
//            if (shouldRefreshUI) {
//                withContext(Dispatchers.Main) {
//                    fragment.updateTasks()
//                    updatePagerAdapter()
//                }
//            }
//        }
//    }
//
//    fun onTaskChanged(taskNumber: Int) {
////        fragment.selectedTask = Pair(fragment.taskItems[taskNumber].taskId, fragment.taskItems[taskNumber].id)
////        fragment.updateTasks()
////        updatePagerAdapter()
//    }
//
//    fun initTasks(taskIds: IntArray?, taskItemIds: List<TaskItemIdWithTaskId>?){
////        bgScope.launch {
////            loadTasks(taskIds)
////            loadTaskItems(taskItemIds)
////
////            withContext(Dispatchers.Main){
////                fragment.updateTasks()
////                updatePagerAdapter()
////                fragment.activity()?.changeTitle(fragment.getTitle())
////            }
////        }
//    }
//
////    suspend fun loadTasks(ids: IntArray?) {
////        ids ?: return
////        fragment.tasks.addAll(ids.toList().mapNotNull {
////            application().tasksRepository.getTask(it)
////        })
////    }
////
////    suspend fun loadTaskItems(ids: List<TaskItemIdWithTaskId>?) {
////        ids ?: return
////        fragment.taskItems.addAll(ids.mapNotNull {
////            val item = application().tasksRepository.getTaskItem(it.taskId, it.taskItemId)
////            item
////        })
////
////        removeNewFromTasks()
////    }
////
////    suspend fun removeNewFromTasks(){
////        fragment.taskItems.filter { it.isNew }.forEach {
////            ControllApplication.instance.database.taskItemDao().insert(
////                it.copy(isNew = false).toEntity()
////            )
////        }
////    }
//}