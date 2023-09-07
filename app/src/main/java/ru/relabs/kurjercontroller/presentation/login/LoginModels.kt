package ru.relabs.kurjercontroller.presentation.login

import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.relabs.kurjercontroller.data.models.auth.UserLogin
import ru.relabs.kurjercontroller.domain.storage.SavedUserStorage
import ru.relabs.kurjercontroller.domain.useCases.AppUpdateUseCase
import ru.relabs.kurjercontroller.presentation.base.tea.ElmEffect
import ru.relabs.kurjercontroller.presentation.base.tea.ElmMessage
import ru.relabs.kurjercontroller.presentation.base.tea.ElmRender
import ru.relabs.kurjercontroller.presentation.base.tea.ErrorContext
import ru.relabs.kurjercontroller.presentation.base.tea.ErrorContextImpl
import ru.relabs.kurjercontroller.presentation.base.tea.RouterContext
import ru.relabs.kurjercontroller.presentation.base.tea.RouterContextMainImpl

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */

data class LoginState(
    val login: UserLogin = UserLogin(""),
    val password: String = "",
    val isPasswordRemembered: Boolean = true,
    val loaders: Int = 0
)

class LoginContext(val errorContext: ErrorContextImpl = ErrorContextImpl()) :
    ErrorContext by errorContext,
    RouterContext by RouterContextMainImpl(),
    KoinComponent {

    val updateUseCase: AppUpdateUseCase by inject()
    val savedUserStorage: SavedUserStorage by inject()

    var showOfflineLoginOffer: () -> Unit = {}
    var showError: (id: Int) -> Unit = {}
}

typealias LoginMessage = ElmMessage<LoginContext, LoginState>
typealias LoginEffect = ElmEffect<LoginContext, LoginState>
typealias LoginRender = ElmRender<LoginState>