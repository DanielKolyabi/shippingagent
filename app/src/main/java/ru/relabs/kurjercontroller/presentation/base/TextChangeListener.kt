package ru.relabs.kurjercontroller.presentation.base

import android.text.Editable
import android.text.TextWatcher

/**
 * Created by Daniil Kurchanov on 27.01.2020.
 */
class TextChangeListener(val onChange: (String) -> Unit): TextWatcher {
    override fun afterTextChanged(s: Editable?) {    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (s == null) {
            return
        }

        onChange(s.toString())
    }
}