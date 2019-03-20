package ru.relabs.kurjercontroller.ui.activities

import android.content.Context
import androidx.appcompat.app.AlertDialog
import ru.relabs.kurjercontroller.CustomLog

/**
 * Created by ProOrange on 18.03.2019.
 */

interface ErrorButtonsListener {
    fun positiveListener()
    fun negativeListener()
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