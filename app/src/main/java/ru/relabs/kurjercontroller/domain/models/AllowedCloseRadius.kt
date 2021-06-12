package ru.relabs.kurjercontroller.domain.models

sealed class AllowedCloseRadius(val photoAnyDistance: Boolean, val distance: Int) {
    class NotRequired(distance: Int, photoAnyDistance: Boolean) : AllowedCloseRadius(photoAnyDistance, distance)
    class Required(distance: Int, photoAnyDistance: Boolean) : AllowedCloseRadius(photoAnyDistance, distance)
}