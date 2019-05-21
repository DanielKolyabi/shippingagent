package ru.relabs.kurjercontroller.ui.extensions

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import kotlinx.android.synthetic.main.fragment_report.*
import org.joda.time.DateTime
import ru.relabs.kurjercontroller.R
import java.util.*

/**
 * Created by ProOrange on 29.08.2018.
 */

fun View.setVisible(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.GONE
}

fun Button.setSelectButtonActive(active: Boolean) {
    if (active) {
        this.setBackgroundResource(R.drawable.abc_btn_colored_material)
    } else {
        this.setBackgroundResource(R.drawable.abc_btn_default_mtrl_shape)
    }
}

fun Context.hideKeyboard(view: View?){
    (this.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(
        view?.windowToken,
        0
    )
}