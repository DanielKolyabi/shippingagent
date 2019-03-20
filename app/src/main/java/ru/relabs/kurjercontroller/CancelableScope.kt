package ru.relabs.kurjercontroller

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren

/**
 * Created by ProOrange on 20.03.2019.
 */
class CancelableScope(dispatcher: CoroutineDispatcher) : CoroutineScope {
    private val cancelableJob = SupervisorJob()
    override val coroutineContext = dispatcher + cancelableJob

    fun cancel(){
        cancelableJob.cancelChildren()
    }

    fun terminate() {
        cancelableJob.cancel()
    }
}