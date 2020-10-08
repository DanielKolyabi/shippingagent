package ru.relabs.kurjercontroller.domain.mappers.network

import ru.relabs.kurjercontroller.data.models.auth.UserLogin
import ru.relabs.kurjercontroller.data.models.auth.UserResponse
import ru.relabs.kurjercontroller.domain.models.User

object UserMapper {
    fun fromRaw(raw: UserResponse): User = User(
        login = UserLogin(raw.login)
    )
}
