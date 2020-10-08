package ru.relabs.kurjercontroller.presentation.activities

import android.app.Activity
import androidx.core.app.ActivityCompat

/**
 * Created by ProOrange on 18.03.2019.
 */
object PermissionHelper {
    fun showPermissionsRequest(activity: Activity, permissionsList: Array<String>, canDenied: Boolean) {
        if (permissionsList.isEmpty()) {
            return
        }
        var msg = "Необходимо разрешить приложению:\n"
        permissionsList.forEach {
            msg += when (it) {
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE -> "Доступ к записи файлов"
                android.Manifest.permission.READ_EXTERNAL_STORAGE -> "Доступ к чтению файлов"
                android.Manifest.permission.ACCESS_FINE_LOCATION -> "Доступ к получению местоположения"
                android.Manifest.permission.REQUEST_INSTALL_PACKAGES -> "Разрешать устанавливать приложения"
                android.Manifest.permission.FOREGROUND_SERVICE -> "Запускать фоновый сервис"
                else -> "Неизвестно"
            } + "\n"
        }

        activity.showError(msg, object : ErrorButtonsListener {
            override fun positiveListener() {
                ActivityCompat.requestPermissions(activity, permissionsList, 1)
            }

            override fun negativeListener() {
                activity.finish()
            }
        }, "Ок", if (canDenied) "Отмена" else "")
    }
}