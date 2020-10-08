package ru.relabs.kurjercontroller.domain.mappers.network

import android.net.Uri
import ru.relabs.kurjercontroller.data.models.UpdatesResponse
import ru.relabs.kurjercontroller.domain.models.AppUpdate
import ru.relabs.kurjercontroller.domain.models.AppUpdatesInfo

object UpdatesMapper {
    fun fromRaw(raw: UpdatesResponse): AppUpdatesInfo = AppUpdatesInfo(
        required = raw.required?.let {
            AppUpdate(
                version = it.version,
                url = Uri.parse(it.url),
                isRequired = true
            )
        },
        optional = raw.optional?.let {
            AppUpdate(
                version = it.version,
                url = Uri.parse(it.url),
                isRequired = false
            )
        }
    )
}