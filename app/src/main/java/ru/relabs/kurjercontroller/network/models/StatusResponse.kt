package ru.relabs.kurjercontroller.network.models

data class StatusResponse(
        val status: Boolean,
        val error: ErrorModel?
)
