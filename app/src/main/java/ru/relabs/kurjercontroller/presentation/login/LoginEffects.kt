package ru.relabs.kurjercontroller.presentation.login

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.data.models.common.DomainException
import ru.relabs.kurjercontroller.domain.useCases.LoginResult
import ru.relabs.kurjercontroller.presentation.RootScreen
import ru.relabs.kurjercontroller.presentation.base.tea.CommonMessages
import ru.relabs.kurjercontroller.presentation.base.tea.msgEffect
import ru.relabs.kurjercontroller.utils.Left
import ru.relabs.kurjercontroller.utils.Right

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */
object LoginEffects {

    fun effectInit(): LoginEffect = { c, s ->
        when (val r = c.savedUserStorage.getCredentials()) {
            null -> {}
            else -> {
                messages.send(LoginMessages.msgLoginChanged(r.login))
            }
        }
    }

    fun effectLoginCheck(isNetworkEnabled: Boolean): LoginEffect = { c, s ->
        withContext(Dispatchers.Main) {
            when (c.updateUseCase.isAppUpdated || c.updateUseCase.isUpdateUnavailable) {
                true -> when (isNetworkEnabled) {
                    true -> messages.send(msgEffect(effectLogin()))
                    false -> c.showError(R.string.login_need_network)
                }

                false -> c.showError(R.string.login_need_update)
            }
        }
    }

    private fun effectLogin(): LoginEffect = { c, s ->
        messages.send(LoginMessages.msgAddLoaders(1))
        when (val r = c.loginUseCase.login(s.login, s.password, s.isPasswordRemembered)) {
            is Right -> withContext(Dispatchers.Main) { c.router.replaceScreen(RootScreen.Tasks(true)) }
            is Left -> when (val e = r.value) {
                is DomainException.ApiException -> messages.send(CommonMessages.msgError(r.value))
                else -> withContext(Dispatchers.Main) { c.showOfflineLoginOffer() }
            }
        }
        messages.send(LoginMessages.msgAddLoaders(-1))
    }

    fun effectLoginOffline(): LoginEffect = { c, s ->
        messages.send(LoginMessages.msgAddLoaders(1))
        when (c.loginUseCase.loginOffline(s.login, s.password)) {
            LoginResult.Success -> withContext(Dispatchers.Main) { c.router.replaceScreen(RootScreen.Tasks(false)) }
            LoginResult.Wrong -> withContext(Dispatchers.Main) { c.showError(R.string.wrong_password_error) }
            LoginResult.Error -> withContext(Dispatchers.Main) { c.showError(R.string.login_offline_error) }
        }
        messages.send(LoginMessages.msgAddLoaders(-1))
    }
}