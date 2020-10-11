package ru.relabs.kurjercontroller.presentation.login

import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.data.models.auth.UserLogin
import ru.relabs.kurjercontroller.domain.useCases.AppUpdateUseCase
import ru.relabs.kurjercontroller.presentation.base.tea.*

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */

data class LoginState(
    val login: UserLogin = UserLogin(""),
    val password: String = "",
    val isPasswordRemembered: Boolean = false,
    val loaders: Int = 0
)

class LoginContext(val errorContext: ErrorContextImpl = ErrorContextImpl()) :
    ErrorContext by errorContext,
    RouterContext by RouterContextMainImpl(),
    KoinComponent {

    val updateUseCase: AppUpdateUseCase by inject()

    var showOfflineLoginOffer: () -> Unit = {}
    var showError: (id: Int) -> Unit = {}
}

typealias LoginMessage = ElmMessage<LoginContext, LoginState>
typealias LoginEffect = ElmEffect<LoginContext, LoginState>
typealias LoginRender = ElmRender<LoginState>