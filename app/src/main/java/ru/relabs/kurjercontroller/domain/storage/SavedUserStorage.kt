package ru.relabs.kurjercontroller.domain.storage

import ru.relabs.kurjercontroller.data.models.auth.UserLogin
import ru.relabs.kurjercontroller.domain.models.Credentials

class SavedUserStorage(private val appPreferences: AppPreferences) {

    fun saveCredentials(login: UserLogin, password: String) {
        appPreferences.backUpUserCredentials(login, password)
    }

    fun saveCredentials(credentials: Credentials) {
        appPreferences.backUpUserCredentials(credentials.login, credentials.password)
    }

    fun getCredentials(): Credentials? = appPreferences.getBackedUpCredentials()

    fun saveToken(token: String) {
        appPreferences.backUpToken(token)
    }

    fun getToken(): String? = appPreferences.getBackedUpToken()

}

