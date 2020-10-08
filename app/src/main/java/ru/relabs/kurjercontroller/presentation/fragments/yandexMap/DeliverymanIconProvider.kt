package ru.relabs.kurjercontroller.presentation.fragments.yandexMap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.yandex.runtime.image.ImageProvider
import ru.relabs.kurjercontroller.R
import kotlin.math.max

/**
 * Created by ProOrange on 26.06.2019.
 */
class DeliverymanIconProvider(val context: Context, val text: String) : ImageProvider() {
    override fun getId(): String {
        return "text:${text}"
    }

    override fun getImage(): Bitmap {
        val fontPaint = Paint()
        fontPaint.textSize = 14*context.resources.displayMetrics.density
        fontPaint.style = Paint.Style.FILL
        fontPaint.color = context.resources.getColor(R.color.colorFuchsia)

        val textWidth = fontPaint.measureText(text).toInt()

        val drawable = context.resources.getDrawable(R.drawable.ic_deliveryman)
        val canvasWidth = max(textWidth, drawable.intrinsicWidth)

        val bmp = Bitmap.createBitmap(canvasWidth, drawable.intrinsicHeight+fontPaint.textSize.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val offset = (canvasWidth - drawable.intrinsicWidth) / 2

        drawable.setBounds(offset, fontPaint.textSize.toInt(), drawable.intrinsicWidth + offset, canvas.height)
        drawable.draw(canvas)
        canvas.drawText(text, 0f, fontPaint.textSize, fontPaint)

        return bmp
    }
}
