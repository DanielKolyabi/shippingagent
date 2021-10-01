package ru.relabs.kurjercontroller.presentation.base.tea

import android.view.View
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.data.models.common.DomainException
import ru.relabs.kurjercontroller.domain.storage.AuthTokenStorage
import ru.relabs.kurjercontroller.domain.storage.CurrentUserStorage
import ru.relabs.kurjercontroller.domain.useCases.LoginUseCase
import ru.relabs.kurjercontroller.presentation.RootScreen
import ru.relabs.kurjercontroller.utils.extensions.showSnackbar

interface ErrorContext {
    var handleError: (DomainException) -> Unit
}

interface RouterContext {
    val router: Router
    val loginUseCase: LoginUseCase
}

class RouterContextMainImpl : RouterContext, KoinComponent {
    override val router: Router by inject()
    override val loginUseCase: LoginUseCase by inject()
}

class ErrorContextImpl : ErrorContext {

    fun attach(view: View) {
        handleError = { e ->
            view.post {
                when (e) {
                    is DomainException.ApiException -> showSnackbar(view, e.error.message)
                    is DomainException.UnknownException -> showSnackbar(
                        view, view.resources.getString(R.string.unknown_network_error)
                    )
                }
            }
        }
    }

    fun detach() {
        handleError = {}
    }

    override var handleError: (DomainException) -> Unit = {}
}


object CommonMessages {

    fun <C, S> msgError(error: DomainException): ElmMessage<C, S> where C : ErrorContext, C : RouterContext {
        return when (error) {
            is DomainException.ApiException -> when (error.error.code) {
                401 -> msgEffect { c, _ ->
                    c.loginUseCase.logout()
                    withContext(Dispatchers.Main) {
                        c.router.newRootScreen(RootScreen.Login())
                    }
                }
                else -> msgEffect { c, _ -> c.handleError(error) }
            }
            is DomainException.CanceledException -> msgEffect { c, _ -> c.handleError(error) }
            is DomainException.UnknownException -> msgEffect { c, _ -> c.handleError(error) }
        }
    }
}