package ru.relabs.kurjercontroller.domain.mappers.network

import ru.relabs.kurjercontroller.data.models.auth.PasswordResponse

object PasswordMapper {
    fun fromRaw(response: PasswordResponse): String = response.password
}
