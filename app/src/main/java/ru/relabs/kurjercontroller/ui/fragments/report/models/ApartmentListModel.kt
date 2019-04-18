package ru.relabs.kurjercontroller.ui.fragments.report.models

/**
 * Created by ProOrange on 16.04.2019.
 */

sealed class ApartmentListModel {
    data class Apartment(
        val number: Int,
        val buttonGroup: Int = 0,
        val state: Int = 0
    ): ApartmentListModel()
}