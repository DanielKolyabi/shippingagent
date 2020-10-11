package ru.relabs.kurjercontroller.utils.extensions

import org.joda.time.DateTime
import java.util.*

fun Date.formatted(format: String = "dd.MM.yyyy"): String {
    return DateTime(this).toString(format)
}

fun Date.formattedWithSecs(): String {
    return formatted("dd.MM.yyyy HH:mm:ss")
}