package ru.relabs.kurjercontroller.ui.fragments.entrancesList

/**
 * Created by ProOrange on 18.03.2019.
 */

sealed class EntrancesListModel{
    data class Entrance(val number: Int): EntrancesListModel()
}