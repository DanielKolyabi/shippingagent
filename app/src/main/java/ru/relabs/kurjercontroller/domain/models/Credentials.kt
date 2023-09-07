package ru.relabs.kurjercontroller.domain.models

import ru.relabs.kurjercontroller.data.models.auth.UserLogin

data class Credentials(
    val login: UserLogin,
    val password: String
)