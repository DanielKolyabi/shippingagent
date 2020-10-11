package ru.relabs.kurjercontroller.presentation.login

import android.text.TextWatcher
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import ru.relabs.kurjercontroller.BuildConfig
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.presentation.base.tea.renderT
import ru.relabs.kurjercontroller.utils.extensions.renderText
import ru.relabs.kurjercontroller.utils.extensions.visible

/**
 * Created by Daniil Kurchanov on 06.04.2020.
 */
object LoginRenders {

    fun renderLogin(view: EditText, watcher: TextWatcher): LoginRender = renderT(
        { it.login },
        { view.renderText(it.login, watcher) }
    )

    fun renderPassword(view: EditText, watcher: TextWatcher): LoginRender = renderT(
        { it.password },
        { view.renderText(it, watcher) }
    )

    fun renderCheckbox(view: CheckBox): LoginRender = renderT(
        { it.isPasswordRemembered },
        { view.isChecked = it }
    )

    fun renderVersion(view: TextView): LoginRender = renderT(
        { BuildConfig.VERSION_CODE },
        { view.text = view.resources.getString(R.string.app_version_label, it) }
    )

    fun renderLoading(view: View): LoginRender = renderT(
        { it.loaders > 0 },
        { view.visible = it }
    )
}