package ru.relabs.kurjercontroller

import androidx.fragment.app.Fragment
import ru.relabs.kurjercontroller.application.MyApplication
import ru.relabs.kurjercontroller.ui.activities.MainActivity


/**
 * Created by ProOrange on 05.09.2018.
 */


fun application(): MyApplication {
    return MyApplication.instance
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
