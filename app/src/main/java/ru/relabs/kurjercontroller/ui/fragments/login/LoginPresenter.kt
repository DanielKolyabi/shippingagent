package ru.relabs.kurjercontroller.ui.fragments.login

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.CancelableScope
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.network.NetworkHelper
import ru.relabs.kurjercontroller.ui.activities.ErrorButtonsListener
import ru.relabs.kurjercontroller.ui.activities.showError
import ru.relabs.kurjercontroller.ui.fragments.TaskListScreen

/**
 * Created by ProOrange on 18.03.2019.
 */

class LoginPresenter(val fragment: LoginFragment) {
    val bgScope = CancelableScope(Dispatchers.Default)
    private var isPasswordRemembered = false
    private var authByToken = false

    private fun setRememberPasswordEnabled(enabled: Boolean) {
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
        withContext(Dispatchers.Main) {
            application().router.replaceScreen(TaskListScreen())
        }
    }

    fun loginOffline(): Boolean {
        val user = application().user.getUserCredentials()
        user ?: return false
        application().user.setUser(user)
        return true
    }


    suspend fun showOfflineLoginOffer() = withContext(Dispatchers.Main) {
        fragment.activity?.showError(
            "Нет ответа от сервера.",
            object : ErrorButtonsListener {
                override fun negativeListener() {
                    val status = loginOffline()
                    if (!status) {
                        fragment.activity?.showError("Невозможно войти оффлайн. Необходима авторизация через сервер.")
                        return
                    }
                    application().router.replaceScreen(TaskListScreen())
                }

                override fun positiveListener() {}
            },
            "Ок", "Войти Оффлайн"
        )
    }

}