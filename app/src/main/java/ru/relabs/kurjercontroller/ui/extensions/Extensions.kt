package ru.relabs.kurjercontroller.ui.extensions

import android.view.View
import android.widget.Button
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
