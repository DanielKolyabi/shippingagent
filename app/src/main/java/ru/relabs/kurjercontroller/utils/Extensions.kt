package ru.relabs.kurjercontroller.utils

import androidx.fragment.app.Fragment
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.selects.select
import ru.relabs.kurjercontroller.ControllApplication
import ru.relabs.kurjercontroller.presentation.host.HostActivity
import java.io.File


/**
 * Created by ProOrange on 05.09.2018.
 */


fun application(): ControllApplication {
    return ControllApplication.appContext as ControllApplication
}

fun Fragment.activity(): HostActivity? {
    return this.context as? HostActivity
}

fun Throwable.log() {
    this.printStackTrace()

    //FirebaseCrashlytics.getInstance().recordException(this)
    val stacktrace = CustomLog.getStacktraceAsString(this)
    CustomLog.writeToFile(stacktrace)
}

suspend fun <T> tryOrLogAsync(block: suspend () -> T) {
    try {
        block()
    } catch (e: Exception) {
        e.log()
    }
}

fun <T> tryOrLog(block: () -> T) {
    try {
        block()
    } catch (e: Exception) {
        e.log()
    }
}

suspend fun <E : Job> Iterable<E>.joinFirst(): E = select {
    for (job in this@joinFirst) {
        job.onJoin { job }
    }
}

suspend fun <E : Deferred<R>, R> Iterable<E>.awaitFirst(): R = joinFirst().getCompleted()

fun File.deleteIfEmpty() {
    if (this.isDirectory && this.listFiles().isEmpty()) {
        this.deleteRecursively()
    }
}