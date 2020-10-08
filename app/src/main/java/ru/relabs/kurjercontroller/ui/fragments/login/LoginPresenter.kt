package ru.relabs.kurjercontroller.ui.fragments.login

import android.content.Context
import android.util.Log
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import retrofit2.HttpException
import ru.relabs.kurjercontroller.BuildConfig
import ru.relabs.kurjercontroller.utils.CancelableScope
import ru.relabs.kurjercontroller.activity
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.application.UserModel
import ru.relabs.kurjercontroller.domain.models.GPSCoordinatesModel
import ru.relabs.kurjercontroller.network.DeliveryServerAPI.api
import ru.relabs.kurjercontroller.utils.NetworkHelper
import ru.relabs.kurjercontroller.data.modelsOld.ErrorUtils
import ru.relabs.kurjercontroller.ui.activities.ErrorButtonsListener
import ru.relabs.kurjercontroller.ui.activities.showError
import ru.relabs.kurjercontroller.ui.activities.showErrorSuspend
import ru.relabs.kurjercontroller.ui.fragments.TaskListScreen

/**
 * Created by ProOrange on 18.03.2019.
 */

const val INVALID_TOKEN_ERROR_CODE = 4

class LoginPresenter(val fragment: LoginFragment) {
    val bgScope = CancelableScope(Dispatchers.Default)
    private var isPasswordRemembered = false
    private var authByToken = false

    fun setRememberPasswordEnabled(enabled: Boolean) {
        isPasswordRemembered = enabled
        fragment.setRememberPasswordEnabled(enabled)
    }

    fun resetAuthByToken() {
        setRememberPasswordEnabled(false)
        authByToken = false
    }

    fun onRememberPasswordClick() {
        setRememberPasswordEnabled(!isPasswordRemembered)
    }

    fun onLoginClick(login: String, pwd: String) = bgScope.launch {
        if (!NetworkHelper.isNetworkAvailable(fragment.context)) {
            showOfflineLoginOffer()
            return@launch
        }

        bgScope.launch(Dispatchers.Default) {
            fragment.setLoginButtonLoading(true)

            val sharedPref = application().getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)

            try {
                val time = DateTime().toString("yyyy-MM-dd'T'HH:mm:ss")

                val response = if (!authByToken)
                    api.login(login, pwd, application().deviceUUID, time).await()
                else
                    api.loginByToken(pwd, application().deviceUUID, time).await()

                if (response.error != null) {
                    var errMessage = response.error.message
                    if (response.error.code == INVALID_TOKEN_ERROR_CODE) {
                        errMessage = "Введите заново пароль"
                    }
                    fragment.activity()?.showErrorSuspend("Ошибка №${response.error.code}\n$errMessage")
                    return@launch
                }

                application().user.setUser(UserModel.Authorized(response.user.login, response.token))
                if (isPasswordRemembered) {
                    application().user.storeUserCredentials()
                } else {
                    application().user.restoreUserCredentials()
                }


                application().currentLocation = GPSCoordinatesModel(0.0, 0.0, DateTime(0))
                application().sendPushToken(null)
                application().tasksRepository.getAvailableEntranceKeys(response.token, true)
                application().tasksRepository.getAvailableEntranceEuroKeys(response.token, true)

                if (sharedPref.getString("last_login", "") != response.user.login) {
                    Log.d(
                        "login",
                        "Clear local database. User changed. Last login ${sharedPref.getString(
                            "last_login",
                            ""
                        )}. New login ${response.user.login}"
                    )
                    application().tasksRepository.closeAllTasks()
                }
                sharedPref.edit().putString("last_login", response.user.login).apply()

                withContext(Dispatchers.Main) {
                    application().router.replaceScreen(TaskListScreen(true))
                }

            } catch (e: HttpException) {
                e.printStackTrace()

                if (e.code() == 502) {
                    showOfflineLoginOffer()
                    fragment.setLoginButtonLoading(false)
                    return@launch
                }

                val err = ErrorUtils.getError(e)
                fragment.activity()?.showErrorSuspend("Ошибка №${err.code}.\n${err.message}")
            } catch (e: Exception) {
                e.printStackTrace()
                showOfflineLoginOffer()
            }

            fragment.setLoginButtonLoading(false)
        }
    }

    fun loginOffline(): Boolean {
        val user = application().user.getUserCredentials()
        user ?: return false
        application().user.setUser(user)
        return true
    }


    fun loadUserCredentials() {
        val credentials = application().user.getUserCredentials()
        credentials ?: return
        fragment.login_input.setText(credentials.login)
        fragment.password_input.setText(credentials.token)
        authByToken = true
        fragment.setRememberPasswordEnabled(true)
        fragment.shouldResetRememberOnInput = true
    }

    suspend fun showOfflineLoginOffer() = withContext(Dispatchers.Main) {
        fragment.activity?.showErrorSuspend(
            "Нет ответа от сервера.",
            object : ErrorButtonsListener {
                override fun negativeListener() {
                    val status = loginOffline()
                    if (!status) {
                        fragment.activity?.showError("Невозможно войти оффлайн. Необходима авторизация через сервер.")
                        return
                    }
                    application().router.replaceScreen(TaskListScreen(false))
                }

                override fun positiveListener() {}
            },
            "Ок", "Войти Оффлайн"
        )
    }

}