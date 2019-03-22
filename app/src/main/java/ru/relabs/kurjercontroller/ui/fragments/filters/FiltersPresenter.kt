package ru.relabs.kurjercontroller.ui.fragments.filters

import kotlinx.coroutines.Dispatchers
import ru.relabs.kurjercontroller.CancelableScope

/**
 * Created by ProOrange on 18.03.2019.
 */

class FiltersPresenter(val fragment: FiltersFragment){
    val bgScope = CancelableScope(Dispatchers.Default)
}