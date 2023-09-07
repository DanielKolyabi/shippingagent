package ru.relabs.kurjercontroller.domain.useCases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.data.models.auth.UserLogin
import ru.relabs.kurjercontroller.data.models.common.EitherE
import ru.relabs.kurjercontroller.domain.models.User
import ru.relabs.kurjercontroller.domain.repositories.ControlRepository
import ru.relabs.kurjercontroller.domain.repositories.DatabaseRepository
import ru.relabs.kurjercontroller.domain.repositories.EntranceMonitoringRepository
import ru.relabs.kurjercontroller.domain.repositories.SettingsRepository
import ru.relabs.kurjercontroller.domain.storage.AppPreferences
import ru.relabs.kurjercontroller.domain.storage.AuthTokenStorage
import ru.relabs.kurjercontroller.domain.storage.CurrentUserStorage
import ru.relabs.kurjercontroller.domain.storage.SavedUserStorage
import ru.relabs.kurjercontroller.utils.CustomLog
import ru.relabs.kurjercontroller.utils.fmap

class LoginUseCase(
    private val controlRepository: ControlRepository,
    private val currentUserStorage: CurrentUserStorage,
    private val databaseRepository: DatabaseRepository,
    private val authTokenStorage: AuthTokenStorage,
    private val appPreferences: AppPreferences,
    private val settingsRepository: SettingsRepository,
    private val entranceMonitoringRepository: EntranceMonitoringRepository,
    private val savedUserStorage: SavedUserStorage
) {

    fun isAutologinEnabled() = appPreferences.getUserAutologinEnabled()

    suspend fun loginOffline(): User? {
        val savedCredentials = savedUserStorage.getCredentials() ?: return null
        val savedToken = savedUserStorage.getToken() ?: return null

        loginInternal(savedCredentials.login, savedCredentials.password, savedToken, offline = true)
        return User(savedCredentials.login)
    }

    suspend fun login(login: UserLogin, password: String, remember: Boolean): EitherE<User> {
        appPreferences.setUserAutologinEnabled(remember)
        return controlRepository.login(login, password).fmap { (user, token) ->
            loginInternal(user.login, token, token, offline = false)
            user
        }
    }


    private suspend fun loginInternal(login: UserLogin, password: String, token: String, offline: Boolean) = withContext(Dispatchers.IO) {
        val lastUserLogin = savedUserStorage.getCredentials()?.login
        if (lastUserLogin != login) {
            databaseRepository.clearTasks()
            settingsRepository.resetData()
        }
        settingsRepository.startRemoteUpdating()
        authTokenStorage.saveToken(token)
        currentUserStorage.saveCurrentUserLogin(login)
        savedUserStorage.saveCredentials(login, password)
        savedUserStorage.saveToken(token)
        CustomLog.writeToFile("[IMEI] Update after login")
        controlRepository.updateDeviceIMEI()
        controlRepository.updatePushToken()
        entranceMonitoringRepository.getClosedEntrancesCount()
    }

    fun logout() {
        appPreferences.setUserAutologinEnabled(false)
        authTokenStorage.resetToken()
        currentUserStorage.resetCurrentUserLogin()
    }
}