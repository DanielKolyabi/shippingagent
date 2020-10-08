package ru.relabs.kurjercontroller

import androidx.fragment.app.Fragment
import ru.relabs.kurjercontroller.application.ControllApplication
import ru.relabs.kurjercontroller.ui.activities.MainActivity
import ru.relabs.kurjercontroller.utils.CustomLog


/**
 * Created by ProOrange on 05.09.2018.
 */


fun application(): ControllApplication {
    return ControllApplication.appContext as ControllApplication
}

fun Fragment.activity(): MainActivity? {
    return this.context as? MainActivity
}

fun Throwable.logError() {
    this.printStackTrace()

    val stacktrace = CustomLog.getStacktraceAsString(this)
    CustomLog.writeToFile(stacktrace)
}

inline fun <T> MutableList<T>?.orEmpty(): MutableList<T> = this ?: mutableListOf()
