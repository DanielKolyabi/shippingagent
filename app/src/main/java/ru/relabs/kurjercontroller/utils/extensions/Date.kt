package ru.relabs.kurjercontroller.utils.extensions

import org.joda.time.DateTime
import java.util.*

fun Date.formatted(format: String = "dd.MM.yyyy"): String {
    return DateTime(this).formatted(format)
}

fun DateTime.formatted(format: String = "dd.MM.yyyy"): String {
    return this.toString(format)
}

fun Date.formattedWithSecs(): String {
    return formatted("dd.MM.yyyy HH:mm:ss")
}