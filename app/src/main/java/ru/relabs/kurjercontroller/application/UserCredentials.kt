package ru.relabs.kurjercontroller.application

import android.content.Context
import ru.relabs.kurjercontroller.BuildConfig

/**
 * Created by ProOrange on 18.03.2019.
 */
class UserCredentials(val application: MyApplication) {
    var user: UserModel = UserModel.Unauthorized

    fun storeUserCredentials() {
        if (user !is UserModel.Authorized) return
        application.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
            .edit()
            .putString("login", (user as? UserModel.Authorized)?.login)
            .putString("token", (user as? UserModel.Authorized)?.token)
            .apply()
    }

    fun getUserCredentials(): UserModel.Authorized? {
        user.let {
            if (it is UserModel.Authorized) {
                return it
            }
        }
        val login = application.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
            .getString("login", "-unknw")
        val token = application.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
            .getString("token", "-unknw")
        if (token == "-unknw") {
            return null
        }
        return UserModel.Authorized(login = login, token = token)
    }

    fun restoreUserCredentials() {
        application.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
            .edit()
            .remove("login")
            .remove("token")
            .apply()
    }

    fun setUser(user: UserModel.Authorized?) {
        if (user == null) {
            this.user = UserModel.Unauthorized
            return
        }
        this.user = user
    }
}