package ru.relabs.kurjercontroller.domain.useCases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.data.models.auth.UserLogin
import ru.relabs.kurjercontroller.data.models.common.EitherE
import ru.relabs.kurjercontroller.domain.models.User
import ru.relabs.kurjercontroller.domain.repositories.DatabaseRepository
import ru.relabs.kurjercontroller.domain.repositories.ControlRepository
import ru.relabs.kurjercontroller.domain.repositories.RadiusRepository
import ru.relabs.kurjercontroller.domain.storage.AppPreferences
import ru.relabs.kurjercontroller.domain.storage.AuthTokenStorage
import ru.relabs.kurjercontroller.domain.storage.CurrentUserStorage
import ru.relabs.kurjercontroller.services.ReportService
import ru.relabs.kurjercontroller.utils.fmap

class LoginUseCase(
    private val controlRepository: ControlRepository,
    private val currentUserStorage: CurrentUserStorage,
    private val databaseRepository: DatabaseRepository,
    private val radiusRepository: RadiusRepository,
    private val authTokenStorage: AuthTokenStorage,
    private val appPreferences: AppPreferences
){

    fun isAutologinEnabled() = appPreferences.getUserAutologinEnabled()

    suspend fun loginOffline(): User? {
        val savedLogin = currentUserStorage.getCurrentUserLogin() ?: return null
        val savedToken = authTokenStorage.getToken() ?: return null

        loginInternal(savedLogin, savedToken, offline = true)
        return User(savedLogin)
    }

    suspend fun login(login: UserLogin, password: String, remember: Boolean): EitherE<User> {
        appPreferences.setUserAutologinEnabled(remember)
        return controlRepository.login(login, password).fmap { (user, token) ->
            loginInternal(user.login, token, offline = false)
            user
        }
    }

    suspend fun login(token: String): EitherE<User> {
        return controlRepository.login(token).fmap { (user, token) ->
            loginInternal(user.login, token, offline = false)
            user
        }
    }

    private suspend fun loginInternal(login: UserLogin, token: String, offline: Boolean) = withContext(Dispatchers.IO){
        val lastUserLogin = currentUserStorage.getCurrentUserLogin()
        if (lastUserLogin != login) {
            databaseRepository.clearTasks()
            radiusRepository.resetData()
        }
        authTokenStorage.saveToken(token)
        currentUserStorage.saveCurrentUserLogin(login)

        radiusRepository.startRemoteUpdating()
        controlRepository.updateDeviceIMEI()
        controlRepository.updatePushToken()
    }

    fun logout() {
        appPreferences.setUserAutologinEnabled(false)
        authTokenStorage.resetToken()
        currentUserStorage.resetCurrentUserLogin()
        ReportService.instance?.stopTaskClosingTimer()
    }
}