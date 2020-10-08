package ru.relabs.kurjercontroller.presentation.activities

import android.view.Window
import androidx.appcompat.app.AppCompatActivity

/**
 * Created by ProOrange on 20.03.2019.
 */


fun AppCompatActivity.clearBackStack() {

    val backStackEntryCount = this
        .supportFragmentManager
        .backStackEntryCount

    for (i in 0 until backStackEntryCount) {
        this.supportFragmentManager.popBackStackImmediate()
    }
}

fun AppCompatActivity.hideActionBar() {
    window.requestFeature(Window.FEATURE_ACTION_BAR)
    supportActionBar?.hide()
}