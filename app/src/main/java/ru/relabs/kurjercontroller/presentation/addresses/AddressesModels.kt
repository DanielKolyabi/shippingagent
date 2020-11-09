package ru.relabs.kurjercontroller.presentation.addresses

import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.relabs.kurjercontroller.domain.controllers.TaskEventController
import ru.relabs.kurjercontroller.domain.models.Address
import ru.relabs.kurjercontroller.domain.models.Task
import ru.relabs.kurjercontroller.domain.models.TaskItem
import ru.relabs.kurjercontroller.domain.providers.PathsProvider
import ru.relabs.kurjercontroller.domain.repositories.DatabaseRepository
import ru.relabs.kurjercontroller.presentation.base.tea.*
import ru.relabs.kurjercontroller.presentation.taskDetails.TaskDetailsFragment
import java.io.File

/**
 * Created by Daniil Kurchanov on 02.04.2020.
 */

data class AddressesState(
    val loaders: Int = 0,
    val tasks: List<Task> = emptyList(),
    val sorting: AddressesSortingMethod = AddressesSortingMethod.STANDARD,
    val searchFilter: String = "",
    val exits: Int = 0,
    val selectedListAddress: Address? = null
)

class AddressesContext(val errorContext: ErrorContextImpl = ErrorContextImpl()) :
    ErrorContext by errorContext,
    RouterContext by RouterContextMainImpl(),
    KoinComponent {

    val databaseRepository: DatabaseRepository by inject()
    val taskEventController: TaskEventController by inject()
    val pathsProvider: PathsProvider by inject()

    var showImagePreview: (File) -> Unit = {}
    var showSnackbar: (msgRes: Int) -> Unit = {}
    var addressClickedConsumer: () -> AddressesFragment? = { null }
}

typealias AddressesMessage = ElmMessage<AddressesContext, AddressesState>
typealias AddressesEffect = ElmEffect<AddressesContext, AddressesState>
typealias AddressesRender = ElmRender<AddressesState>

enum class AddressesSortingMethod{
    STANDARD, ALPHABETIC, CLOSE_TIME
}