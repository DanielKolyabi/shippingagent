package ru.relabs.kurjercontroller.domain.mappers.network

import android.net.Uri
import ru.relabs.kurjercontroller.data.models.UpdatesResponse
import ru.relabs.kurjercontroller.domain.models.AppUpdate
import ru.relabs.kurjercontroller.domain.models.AppUpdatesInfo

object UpdatesMapper {
    fun fromRaw(raw: UpdatesResponse): AppUpdatesInfo = AppUpdatesInfo(
        required = when (val uri = raw.required?.url) {
            null -> null
            else -> AppUpdate(
                version = raw.required.version,
                url = Uri.parse(uri),
                isRequired = true
            )
        },
        optional = when (val uri = raw.optional?.url) {
            null -> null
            else -> AppUpdate(
                version = raw.optional.version,
                url = Uri.parse(uri),
                isRequired = false
            )
        }
    )
}