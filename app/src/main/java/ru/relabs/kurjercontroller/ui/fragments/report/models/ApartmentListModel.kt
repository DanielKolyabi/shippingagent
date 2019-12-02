package ru.relabs.kurjercontroller.ui.fragments.report.models

/**
 * Created by ProOrange on 16.04.2019.
 */

sealed class ApartmentListModel {
    data class Apartment(
        val number: Int,
        var buttonGroup: Int = 0,
        var state: Int = 0,
        var description: String = "",
        val colored: Boolean = false
    ): ApartmentListModel()

    data class Lookout(
        var state: Int = 0
    ): ApartmentListModel()

    data class Entrance(
        var state: Int = 0
    ): ApartmentListModel()
}