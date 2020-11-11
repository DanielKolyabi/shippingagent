package ru.relabs.kurjercontroller.domain.storage

class MapCameraStorage {
    private var savedPosition: Pair<Double, Double>? = null
    private var savedZoom: Float? = null

    fun saveCameraSettings(pos: Pair<Double, Double>, zoom: Float) {
        savedPosition = pos
        savedZoom = zoom
    }

    fun resetCameraSettings() {
        savedPosition = null
        savedZoom = null
    }

    fun getCameraPosition(): Pair<Double, Double>? = savedPosition
    fun getCameraZoom(): Float? = savedZoom
}