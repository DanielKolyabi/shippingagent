package ru.relabs.kurjercontroller.presentation.activities

import android.content.Context
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.utils.CustomLog

/**
 * Created by ProOrange on 18.03.2019.
 */

interface ErrorButtonsListener {
    fun positiveListener(){}
    fun negativeListener(){}
}

suspend fun Context.showErrorSuspend(
    errorMessage: String,
    listener: ErrorButtonsListener? = null,
    forcePositiveButtonName: String = "Ок",
    forceNegativeButtonName: String = "",
    cancelable: Boolean = false) = withContext(Dispatchers.Main){
    this@showErrorSuspend.showError(errorMessage, listener, forcePositiveButtonName, forceNegativeButtonName, cancelable)
}

fun Context.showError(
    errorMessage: String,
    listener: ErrorButtonsListener? = null,
    forcePositiveButtonName: String = "Ок",
    forceNegativeButtonName: String = "",
    cancelable: Boolean = false
) {
    try {
        val builder = AlertDialog.Builder(this)
            .setMessage(errorMessage)

        if (forcePositiveButtonName.isNotBlank()) {
            builder.setPositiveButton(forcePositiveButtonName) { _, _ -> listener?.positiveListener() }
        }
        if (forceNegativeButtonName.isNotBlank()) {
            builder.setNegativeButton(forceNegativeButtonName) { _, _ -> listener?.negativeListener() }
        }
        builder.setCancelable(cancelable)
        builder.show()
    } catch (e: Throwable) {
        CustomLog.writeToFile(CustomLog.getStacktraceAsString(e))
    }
}

suspend fun Context.showErrorAsync(
    errorMessage: String,
    listener: ErrorButtonsListener? = null,
    forcePositiveButtonName: String = "Ок",
    forceNegativeButtonName: String = "",
    cancelable: Boolean = false
) = withContext(Dispatchers.Main){
    showError(errorMessage, listener, forcePositiveButtonName, forceNegativeButtonName, cancelable)
}