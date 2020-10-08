package ru.relabs.kurjercontroller.presentation.fragments.yandexMap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import com.yandex.runtime.image.ImageProvider
import ru.relabs.kurjercontroller.R

/**
 * Created by ProOrange on 06.06.2019.
 */
class ColoredIconProvider(val context: Context, val color: Int) : ImageProvider() {
    override fun getId(): String {
        return "colored:${color}"
    }

    override fun getImage(): Bitmap {
        val drawable = context.resources.getDrawable(R.drawable.house_map_icon)
        val filter = PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY)
        drawable.colorFilter = filter
        val bmp = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bmp
    }
}
